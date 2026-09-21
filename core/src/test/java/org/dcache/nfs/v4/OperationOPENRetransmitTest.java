/*
 * Copyright (c) 2026 Deutsches Elektronen-Synchroton,
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

import static org.dcache.nfs.v4.NfsTestUtils.createV40Client;
import static org.dcache.nfs.v4.NfsTestUtils.execute;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.security.auth.Subject;

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.DummyVFS;
import org.dcache.nfs.vfs.ForwardingFileSystem;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

/**
 * NFSv4.0 requires the server to keep the last request and response for every open-owner to answer retransmissions
 * (rfc7530 9.1.7): a request repeating the last seqid must be replied with the cached response instead of
 * NFS4ERR_BAD_SEQID, and error responses like NFS4ERR_DELAY must not consume a sequence id, as the client retries
 * them with the same seqid. The macOS NFS client retransmits after lost or delayed replies, e.g. when the virtual
 * file system answers with NFS4ERR_DELAY while a backend operation is pending.
 */
public class OperationOPENRetransmitTest {

    private NFSv4StateHandler sh;
    private DummyVFS vfs;
    private Inode fsRoot;
    private NFS4Client client;

    @Before
    public void setUp() throws IOException {
        sh = new NFSv4StateHandler();
        vfs = new DummyVFS();
        fsRoot = vfs.getRootInode();
        client = createV40Client(sh);
        client.setConfirmed();
    }

    private CompoundContext context(VirtualFileSystem fs) throws IOException {
        return new CompoundContextBuilder()
                .withStateHandler(sh)
                .withFs(fs)
                .withMinorversion(0)
                .withCall(generateRpcCall())
                .build();
    }

    private COMPOUND4args open(String path, int seq) {
        return new CompoundBuilder()
                .withPutfh(new nfs_fh4(fsRoot.toNfsHandle()))
                .withOpen(path, seq, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .build();
    }

    private COMPOUND4args openCreate(String path, int seq) {
        return new CompoundBuilder()
                .withPutfh(new nfs_fh4(fsRoot.toNfsHandle()))
                .withOpenCreate(path, seq, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .build();
    }

    @Test
    public void testRetransmitOfOpenRepliedWithCachedResponse() throws Exception {

        vfs.create(fsRoot, Stat.Type.REGULAR, "file", new Subject(), 0644);

        COMPOUND4res res = execute(context(vfs), open("file", 1));
        stateid4 stateid = res.resarray.get(1).opopen.resok4.stateid;

        // reply lost, client retransmits the same request with the same seqid
        COMPOUND4res retransmit = execute(context(vfs), open("file", 1));

        assertEquals("retransmission must be replied with the cached response",
                nfsstat.NFS_OK, retransmit.status);
        assertEquals("retransmission must return the stateid of the original reply",
                stateid, retransmit.resarray.get(1).opopen.resok4.stateid);
    }

    @Test
    public void testSequenceNotConsumedByDelay() throws Exception {

        VirtualFileSystem delayOnce = new ForwardingFileSystem() {
            private boolean delayed;

            @Override
            protected VirtualFileSystem delegate() {
                return vfs;
            }

            @Override
            public Inode create(Inode parent, Stat.Type type, String path, Subject subject, int mode)
                    throws IOException {
                if (!delayed) {
                    delayed = true;
                    throw new DelayException("backend operation pending");
                }
                return super.create(parent, type, path, subject, mode);
            }
        };

        // establish the open-owner
        execute(context(vfs), openCreate("file1", 1));

        // next open is answered with NFS4ERR_DELAY after the seqid was already checked
        try {
            execute(context(delayOnce), openCreate("file2", 2));
            fail("expected NFS4ERR_DELAY");
        } catch (DelayException e) {
            // expected
        }

        // NFS4ERR_DELAY must not consume the seqid, the client retries with the same one
        COMPOUND4res retry = execute(context(delayOnce), openCreate("file2", 2));
        assertEquals("retry after NFS4ERR_DELAY must not fail with NFS4ERR_BAD_SEQID",
                nfsstat.NFS_OK, retry.status);
    }
}
