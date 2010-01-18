package org.dcache.chimera.nfs.v4.mover;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.chimera.IOHimeraFsException;
import org.dcache.chimera.nfs.v4.AbstractNFSv4Operation;
import org.dcache.chimera.nfs.v4.CompoundContext;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.xdr.READ4res;
import org.dcache.chimera.nfs.v4.xdr.READ4resok;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.posix.Stat;

public class DSOperationREAD extends AbstractNFSv4Operation {

	private static final Logger _log = Logger.getLogger(DSOperationREAD.class.getName());

	private final File _poolRoot = new File("/tmp/pNFS");

	public DSOperationREAD(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_READ);
	}

	@Override
	public boolean process(CompoundContext context) {
        READ4res res = new READ4res();

        try {

            Stat inodeStat = context.currentInode().statCache();
            boolean eof = false;

            long offset = _args.opread.offset.value.value;
            int count = _args.opread.count.value.value;

            byte[] buf = new byte[count];

	    	IOReadFile in = new IOReadFile(_poolRoot, context.currentInode().toString(), context.currentInode().stat().getSize());

	    	int bytesReaded = in.read(buf, offset, count);
	    	if( bytesReaded < 0 ) {
	    	    eof = true;
	    	    bytesReaded = 0;
	    	}

            res.status = nfsstat4.NFS4_OK;
            res.resok4 = new READ4resok();
            res.resok4.data = new byte[bytesReaded];
            System.arraycopy(buf, 0, res.resok4.data, 0, bytesReaded);

            if( offset + bytesReaded == inodeStat.getSize() ) {
                eof = true;
            }
            res.resok4.eof = eof;

            in.close();
            _log.log( Level.FINER,
                    "MOVER: {0}@{1} readed, {2} requested.",
                    new Object[] { bytesReaded, offset, _args.opread.count.value.value });

        }catch(IOHimeraFsException hioe) {
            _log.log(Level.SEVERE, "READ : ", hioe);
            res.status = nfsstat4.NFS4ERR_IO;
        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(ChimeraFsException hfe) {
            res.status = nfsstat4.NFS4ERR_NOFILEHANDLE;
        }catch(IOException ioe) {
            _log.log(Level.SEVERE, "READ : ", ioe);
    		res.status = nfsstat4.NFS4ERR_IO;
    	}catch(Exception e) {
            _log.log(Level.SEVERE, "READ : ", e);
    		res.status = nfsstat4.NFS4ERR_IO;
    	}

       _result.opread = res;

            context.processedOperations().add(_result);
            return res.status == nfsstat4.NFS4_OK;
	}


    private static class IOReadFile {

    	private final RandomAccessFile _in;
    	private final FileChannel _fc;


    	public IOReadFile(File root, String path, long size) throws IOException {

	    	File ioFile = new File(root, path);

	    	if( !ioFile.exists() && ! ioFile.createNewFile()) {
	    	    throw new IOException(path + " does't exist and failed to create");
	    	}

	    	/*
	    	 * while file size can be modified in namespace adjust file size to expected one.
	    	 */
            _log.log(Level.FINE, "MOVER: {0} : filesize set to {1}", new Object[] { path,  size});
	    	_in = new RandomAccessFile(ioFile, "rw");
	    	_in.setLength(size);

	    	_fc = _in.getChannel();
    	}


	    public int read(byte[] b, long off, long len) throws IOException {

	    	ByteBuffer bb = ByteBuffer.wrap(b, 0, (int)len);
	    	bb.rewind();

	    	return _fc.read(bb, off);

	    }

	    public void close() throws IOException {
	    	_fc.close();
	    	_in.close();
	    }

	    public long size() throws IOException {
	    	return _fc.size();
	    }
    }


}