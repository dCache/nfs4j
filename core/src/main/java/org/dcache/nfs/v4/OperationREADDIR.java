/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
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
package org.dcache.nfs.v4;

import java.io.IOException;
import java.util.Iterator;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.entry4;
import org.dcache.nfs.v4.xdr.dirlist4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.component4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.READDIR4resok;
import org.dcache.nfs.v4.xdr.READDIR4res;
import org.dcache.nfs.ChimeraNFSException;

import org.dcache.nfs.status.BadCookieException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationREADDIR extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationREADDIR.class);

    /**
     * Smallest possible entry size.
     * 8 (cookie) + 4 (name len) + 4 (smallest padded name) + 4 (bitmap len, always '2')
     *  + 2x4 (bitmap) + 4 (attr len) + 4 (boolean has next)
     */
    private static final int ENTRY4_SIZE = 36;

    /**
     * Minimal readdir reply size for an empty directory.
     * 8 (verifier) + 4 (bool has entry) + 4 (bool eol)
     */
    private static final int READDIR4RESOK_SIZE = 16;

    /**
     * The valid cookie values are zero, or >=3
     */
    private static final long COOKIE_OFFSET = 3;

    OperationREADDIR(nfs_argop4 args) {
        super(args, nfs_opnum4.OP_READDIR);
    }

    @Override
    public void process(final CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        final READDIR4res res = result.opreaddir;

        final Inode dir = context.currentInode();

        DirectoryStream directoryStream;
        verifier4 verifier =_args.opreaddir.cookieverf;
        long startValue = _args.opreaddir.cookie.value;

        /*
         * we have to fake cookie values, while '0' and '1' is reserved so we
         * start with 3
         */
        if (startValue == 1 ||  startValue == 2) {
            throw new BadCookieException("bad cookie : " + startValue);
        }

        Stat stat = context.getFs().getattr(dir);
        if (stat.type() != Stat.Type.DIRECTORY) {
            throw new NotDirException();
        }

        if (startValue != 0) {
            // all cookies are shifted by OFFSET
            startValue -= COOKIE_OFFSET;
        }

        directoryStream = context.getFs().list(dir, verifier.value, startValue);
        Iterator<DirectoryEntry> dirList = directoryStream.iterator();
        if (_args.opreaddir.maxcount.value < READDIR4RESOK_SIZE) {
            throw new TooSmallException("maxcount too small");
        }

        res.status = nfsstat.NFS_OK;
        res.resok4 = new READDIR4resok();
        res.resok4.reply = new dirlist4();
        res.resok4.reply.eof = true;
        res.resok4.cookieverf = new verifier4(directoryStream.getVerifier());

        int currcount = READDIR4RESOK_SIZE;
        int dircount = 0;
        entry4 lastEntry = null;

        int fcount = 0;
        while (dirList.hasNext()) {

            DirectoryEntry le = dirList.next();
            String name = le.getName();

            // skip . and .. while nfsv4 do not care about them
            if (name.equals(".")) {
                continue;
            }
            if (name.equals("..")) {
                continue;
            }

            Inode ei = le.getInode();

            entry4 currentEntry = new entry4();
            currentEntry.name = new component4(name);
            // shift all cookies by OFFSET, as 1 and 2 are reserved
            currentEntry.cookie = new nfs_cookie4(le.getCookie() + COOKIE_OFFSET);

            // TODO: catch here error from getattr and reply 'fattr4_rdattr_error' to the client
            currentEntry.attrs = OperationGETATTR.getAttributes(_args.opreaddir.attr_request, context.getFs(), ei, le.getStat(), context);

            // check if writing this entry exceeds the count limit
            int newSize = ENTRY4_SIZE + name.length() + currentEntry.name.value.length + currentEntry.attrs.attr_vals.value.length;
            int newDirSize = name.length() + 4; // name + sizeof(long)
            if ((currcount + newSize > _args.opreaddir.maxcount.value) || (dircount + newDirSize > _args.opreaddir.dircount.value)) {
                if (lastEntry == null) {
                    //corner case - means we didnt have enough space to
                    //write even a single entry.
                    throw new TooSmallException("can't send even a single entry");
                }
                res.resok4.reply.eof = false;
                break;
            }
            fcount++;
            dircount += newDirSize;
            currcount += newSize;

            if (lastEntry == null) {
                res.resok4.reply.entries = currentEntry;
            } else {
                lastEntry.nextentry = currentEntry;
            }
            lastEntry = currentEntry;
        }

        _log.debug("Sending {} entries ({} bytes from {}, dircount = {}) cookie = {} EOF={}",
                fcount,
                currcount,
                _args.opreaddir.maxcount.value,
                _args.opreaddir.dircount.value,
                startValue,
                res.resok4.reply.eof);
    }
}
