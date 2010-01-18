package org.dcache.chimera.nfs.v4.mover;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.IOHimeraFsException;
import org.dcache.chimera.nfs.v4.AbstractNFSv4Operation;
import org.dcache.chimera.nfs.v4.CompoundContext;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.WRITE4res;
import org.dcache.chimera.nfs.v4.xdr.WRITE4resok;
import org.dcache.chimera.nfs.v4.xdr.count4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.stable_how4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.verifier4;

public class DSOperationWRITE extends AbstractNFSv4Operation {

    private static final Logger _log = Logger.getLogger(DSOperationWRITE.class.getName());

    private final File _poolRoot = new File("/tmp/pNFS");


    public DSOperationWRITE(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_WRITE);
    }

    @Override
    public boolean process(CompoundContext context) {

        WRITE4res res = new WRITE4res();

        try {

            long offset = _args.opwrite.offset.value.value;
            int count = _args.opwrite.data.remaining();

            IOWriteFile out = new IOWriteFile(_poolRoot, context.currentInode().toString(), context.currentInode().stat().getSize() == 0);

            int bytesWritten = out.write(_args.opwrite.data, offset, count);

            if( bytesWritten < 0 ) {
                throw new IOHimeraFsException("IO not allowd");
            }

            res.status = nfsstat4.NFS4_OK;
            res.resok4 = new WRITE4resok();
            res.resok4.count = new count4( new uint32_t(bytesWritten) );
            res.resok4.committed = stable_how4.FILE_SYNC4;
            res.resok4.writeverf = new verifier4();
            res.resok4.writeverf.value = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

            context.currentInode().setSize(out.size());
            _log.log( Level.FINER,
                    "MOVER: {0}@{1} written, {2} requested. New File size {3}",
                    new Object[] { bytesWritten, offset, _args.opwrite.data, out.size() });
            out.close();

        }catch(IOHimeraFsException hioe) {
            res.status = nfsstat4.NFS4ERR_IO;
        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(IOException ioe) {
            _log.log(Level.SEVERE, "WRITE: ", ioe);
            res.status = nfsstat4.NFS4ERR_IO;
        }catch(Exception e) {
            _log.log(Level.SEVERE, "WRITE: ", e);
            res.status = nfsstat4.NFS4ERR_IO;
        }

       _result.opwrite = res;

        context.processedOperations().add(_result);
        return res.status == nfsstat4.NFS4_OK;

    }

    private static class IOWriteFile {

        private final RandomAccessFile _out;
        private final FileChannel _fc;

        public IOWriteFile(File root, String path, boolean truncate) throws IOException {

            File ioFile = new File(root, path);

            _out = new RandomAccessFile(ioFile, "rw");
            _fc = _out.getChannel();
            if( truncate ) {
                _log.log( Level.FINE, "truncate file {0}", ioFile.getPath() );
                _fc.truncate(0);
            }
        }

        public int write(ByteBuffer data, long off, long len) throws IOException {
            data.rewind();
            return _fc.write(data, off);
        }


        public void close() throws IOException {
            _fc.close();
            _out.close();
        }

        public long size() throws IOException {
            return _fc.size();
        }

    }

}
