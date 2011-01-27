/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.dcache.chimera.nfs.v4.mover;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSOperationREAD extends AbstractNFSv4Operation {

        private static final Logger _log = LoggerFactory.getLogger(DSOperationREAD.class);

	private final File _base;

	public DSOperationREAD(nfs_argop4 args, File base) {
		super(args, nfs_opnum4.OP_READ);
                _base = base;
	}

	@Override
	public boolean process(CompoundContext context) {
        READ4res res = new READ4res();

        try {

            Stat inodeStat = context.currentInode().statCache();
            boolean eof = false;

            long offset = _args.opread.offset.value.value;
            int count = _args.opread.count.value.value;

           ByteBuffer bb = ByteBuffer.allocateDirect(count);

	    	IOReadFile in = new IOReadFile(_base, context.currentInode().toString(), context.currentInode().stat().getSize());

	    	int bytesReaded = in.read(bb, offset, count);
	    	if( bytesReaded < 0 ) {
	    	    eof = true;
	    	    bytesReaded = 0;
	    	}

            res.status = nfsstat4.NFS4_OK;
            res.resok4 = new READ4resok();
            res.resok4.data = bb;

            if( offset + bytesReaded == inodeStat.getSize() ) {
                eof = true;
            }
            res.resok4.eof = eof;

            in.close();
            _log.debug("MOVER: {}@{} readed, {} requested.",
                    new Object[] { bytesReaded, offset, _args.opread.count.value.value });

        }catch(IOHimeraFsException hioe) {
            _log.error("READ : ", hioe);
            res.status = nfsstat4.NFS4ERR_IO;
        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
        }catch(ChimeraFsException hfe) {
            res.status = nfsstat4.NFS4ERR_NOFILEHANDLE;
        }catch(IOException ioe) {
            _log.error("READ : ", ioe);
    		res.status = nfsstat4.NFS4ERR_IO;
    	}catch(Exception e) {
            _log.error("READ : ", e);
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
            _log.debug("MOVER: {} : filesize set to {}", new Object[] { path,  size});
	    	_in = new RandomAccessFile(ioFile, "rw");
	    	_in.setLength(size);

	    	_fc = _in.getChannel();
    	}

        public int read(ByteBuffer bb, long off, long len) throws IOException {
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
