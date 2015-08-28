/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.v4.xdr.entry4;
import org.dcache.nfs.v4.xdr.dirlist4;
import org.dcache.nfs.v4.xdr.verifier4;
import org.dcache.nfs.v4.xdr.component4;
import org.dcache.nfs.v4.xdr.nfs_cookie4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.READDIR4resok;
import org.dcache.nfs.v4.xdr.READDIR4res;
import org.dcache.nfs.ChimeraNFSException;

import org.dcache.nfs.InodeCacheEntry;
import org.dcache.nfs.status.BadCookieException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.status.NotDirException;
import org.dcache.nfs.status.NotSameException;
import org.dcache.nfs.status.TooSmallException;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.utils.Bytes;
import org.dcache.utils.GuavaCacheMXBean;
import org.dcache.utils.GuavaCacheMXBeanImpl;
import org.dcache.xdr.OncRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationREADDIR extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(OperationREADDIR.class);


    // needed to calculate replay size for READDIR4
    /*
     * RFS4_MINLEN_ENTRY4: XDR-encoded size of smallest possible dirent.
     *   This is used to return NFS4ERR_TOOSMALL when clients specify
     *   maxcount that isn't large enough to hold the smallest possible
     *   XDR encoded dirent.
     *
     *       sizeof cookie (8 bytes) +
     *       sizeof name_len (4 bytes) +
     *       sizeof smallest (padded) name (4 bytes) +
     *       sizeof bitmap4_len (12 bytes) +   NOTE: we always encode len=2 bm4
     *       sizeof attrlist4_len (4 bytes) +
     *       sizeof next boolean (4 bytes)
     *
     * RFS4_MINLEN_RDDIR4: XDR-encoded size of READDIR op reply containing
     * the smallest possible entry4 (assumes no attrs requested).
     *   sizeof nfsstat4 (4 bytes) +
     *   sizeof verifier4 (8 bytes) +
     *   sizeof entsecond_to_ry4list bool (4 bytes) +
     *   sizeof entry4   (36 bytes) +
     *   sizeof eof bool  (4 bytes)
     *
     * RFS4_MINLEN_RDDIR_BUF: minimum length of buffer server will provide to
     *   VOP_READDIR.  Its value is the size of the maximum possible dirent
     *   for solaris.  The DIRENT64_RECLEN macro returns the size of dirent
     *   required for a given name length.  MAXNAMELEN is the maximum
     *   filename length allowed in Solaris.  The first two DIRENT64_RECLEN()
     *   macros are to allow for . and .. entries -- just a minor tweak to try
     *   and guarantee that buffer we give to VOP_READDIR will be large enough
     *   to hold ., .., and the largest possible solaris dirent64.
     */

    private static final int ENTRY4_SIZE = 36;
    private static final int DIRLIST4_SIZE = 4 + nfs4_prot.NFS4_VERIFIER_SIZE + 4 + ENTRY4_SIZE + 4;
    private static final int READDIR4RESOK_SIZE = DIRLIST4_SIZE + ENTRY4_SIZE;

    /**
     * The valid cookie values are zero, or >=3
     */
    private static final long COOKIE_OFFSET = 3;

    private static final Cache<InodeCacheEntry<verifier4>,List<DirectoryEntry>> _dlCache =
            CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .softValues()
            .maximumSize(512)
            .recordStats()
            .build();

    private static final GuavaCacheMXBean CACHE_MXBEAN =
            new GuavaCacheMXBeanImpl("READDIR4", _dlCache);

	OperationREADDIR(nfs_argop4 args) {
		super(args, nfs_opnum4.OP_READDIR);
	}


    /*
     * to simulate snapshot-like list following trick is used:
     *
     * 1. for each mew readdir(plus) ( cookie == 0 ) generate new cookie
     * verifier 2. list result stored in timed Map, where verifier used as a key
     * 3. remove cached entry as soon as list sent
     *
     */
    @Override
    public void process(final CompoundContext context, nfs_resop4 result) throws ChimeraNFSException, IOException, OncRpcException {

        final READDIR4res res = result.opreaddir;

        final Inode dir = context.currentInode();

        List<DirectoryEntry> dirList;
        verifier4 verifier;
        long startValue = _args.opreaddir.cookie.value;


        /*
         * For fresh readdir requests, cookie == 0, generate a new verifier and
         * check cache for an existing result.
         *
         * For requests with cookie != 0 provided verifier used for cache
         * lookup.
         */

        /*
         * we have to fake cookie values, while '0' and '1' is reserved so we
         * start with 3
         */
        if (startValue == 1 ||  startValue == 2) {
            throw new BadCookieException("bad cookie : " + startValue);
        }

        if (startValue != 0) {

            verifier = _args.opreaddir.cookieverf;
            dirList = _dlCache.getIfPresent(new InodeCacheEntry<>(dir, verifier));
            if (dirList == null) {
                /*
                 * We do not have cached snapshot for this verifier - tell
                 * the client to start over.
                 */
                throw new NotSameException("readdir verifier expired");
            }

            // while client sends to us last cookie, we have to continue from the next one
            ++startValue;

            // the cookie==1,2 is reserved
            if ((startValue > dirList.size() + COOKIE_OFFSET) || (startValue < COOKIE_OFFSET)) {
                throw new BadCookieException("bad cookie : " + startValue + " " + dirList.size());
            }
        } else {
            Stat stat = context.getFs().getattr(dir);

            if (stat.type() != Stat.Type.DIRECTORY) {
                throw new NotDirException();
            }

            verifier = generateDirectoryVerifier(stat);
            startValue = COOKIE_OFFSET;
            InodeCacheEntry<verifier4> cacheKey = new InodeCacheEntry<>(dir, verifier);
            try {
                dirList = _dlCache.get(cacheKey, () -> context.getFs().list(dir));
            } catch (ExecutionException e) {
                Throwables.propagateIfInstanceOf(e.getCause(), ChimeraNFSException.class);
                throw new NfsIoException(e.getMessage());
            }
        }

        if (_args.opreaddir.maxcount.value < READDIR4RESOK_SIZE) {
            throw new TooSmallException("maxcount too small");
        }

        res.resok4 = new READDIR4resok();
        res.resok4.reply = new dirlist4();

        res.resok4.cookieverf = verifier;

        int currcount = READDIR4RESOK_SIZE;
        int dircount = 0;
        res.resok4.reply.entries = new entry4();
        entry4 currentEntry = res.resok4.reply.entries;
        entry4 lastEntry = null;

        /*
         * hope to send all entries at once. if it's not the case, eof flag will
         * be set to false
         */
        res.resok4.reply.eof = true;
        int fcount = 0;
        for (long i = startValue; i < dirList.size() + COOKIE_OFFSET; i++) { // chimera have . and ..

            DirectoryEntry le = dirList.get((int) (i - COOKIE_OFFSET));
            String name = le.getName();

            // skip . and .. while nfsv4 do not care about them
            if (name.equals(".")) {
                continue;
            }
            if (name.equals("..")) {
                continue;
            }

            fcount++;

            Inode ei = le.getInode();

            currentEntry.name = new component4(name);
            // keep offset
            currentEntry.cookie = new nfs_cookie4(i);

            // TODO: catch here error from getattr and reply 'fattr4_rdattr_error' to the client
            currentEntry.attrs = OperationGETATTR.getAttributes(_args.opreaddir.attr_request, context.getFs(), ei, le.getStat(), context);
            currentEntry.nextentry = null;

            // check if writing this entry exceeds the count limit
            int newSize = ENTRY4_SIZE + name.length() + currentEntry.name.value.length + currentEntry.attrs.attr_vals.value.length;
            int newDirSize = name.length() + 4; // name + sizeof(long)
            if ((currcount + newSize > _args.opreaddir.maxcount.value) || (dircount + newDirSize > _args.opreaddir.dircount.value)) {

                res.resok4.reply.eof = false;

                _log.debug("Sending {} entries ({} bytes from {}, dircount = {} from {} ) cookie = {} total {}",
                            i - startValue, currcount,
                            _args.opreaddir.maxcount.value,
                            dircount,
                            _args.opreaddir.dircount.value,
                            startValue, dirList.size());
                break;
            }
            dircount += newDirSize;
            currcount += newSize;

            lastEntry = currentEntry;
            if (i + 1 < dirList.size() + COOKIE_OFFSET) {
                currentEntry.nextentry = new entry4();
                currentEntry = currentEntry.nextentry;
            }

        }

        // empty directory
        if (lastEntry == null) {
            res.resok4.reply.entries = null;
        } else {
            lastEntry.nextentry = null;
        }

        res.status = nfsstat.NFS_OK;
        _log.debug("Sending {} entries ({} bytes from {}, dircount = {} from {} ) cookie = {} total {} EOF={}",
                    fcount, currcount,
                    _args.opreaddir.maxcount.value,
                    startValue,
                    _args.opreaddir.dircount.value,
                    dirList.size(), res.resok4.reply.eof);
    }

    /**
     * Generate a {@link verifier4} for a directory.
     *
     * @param dir
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private verifier4 generateDirectoryVerifier(Stat stat) throws IllegalArgumentException, IOException {
        byte[] verifier = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];
        Bytes.putLong(verifier, 0, stat.getGeneration());
        return new verifier4(verifier);
    }
}
