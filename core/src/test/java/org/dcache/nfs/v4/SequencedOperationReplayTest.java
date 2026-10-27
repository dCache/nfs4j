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
import static org.dcache.nfs.v4.NfsTestUtils.executeWithStatus;
import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_BOTH;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_ACCESS_READ;
import static org.dcache.nfs.v4.xdr.nfs4_prot.OPEN4_SHARE_DENY_NONE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.security.auth.Subject;

import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.DelayException;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.v4.nlm.SimpleLm;
import org.dcache.nfs.v4.xdr.COMPOUND4args;
import org.dcache.nfs.v4.xdr.COMPOUND4res;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.nfs_lock_type4;
import org.dcache.nfs.v4.xdr.seqid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.vfs.DummyVFS;
import org.dcache.nfs.vfs.ForwardingFileSystem;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.junit.Before;
import org.junit.Test;

/**
 * NFSv4.0 sequenced operations must answer a retransmission of the last request of an open- or lock-owner with the
 * cached reply, see rfc7530#section-9.1.7.
 */
public class SequencedOperationReplayTest {

    private static final byte[] LOCK_OWNER = "lock-owner".getBytes(StandardCharsets.UTF_8);

    private NFSv4StateHandler sh;
    private LockManager lm;
    private DummyVFS vfs;
    private Inode fsRoot;
    private nfs_fh4 rootFh;
    private NFS4Client client;

    @Before
    public void setUp() throws IOException {
        sh = new NFSv4StateHandler();
        lm = new SimpleLm();
        vfs = new DummyVFS();
        fsRoot = vfs.getRootInode();
        rootFh = new nfs_fh4(fsRoot.toNfsHandle());
        client = createV40Client(sh);
        client.setConfirmed();
    }

    @Test
    public void testOpenReplayed() throws Exception {
        Inode file = vfs.create(fsRoot, Stat.Type.REGULAR, "file", new Subject(), 0644);

        COMPOUND4args open = new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpen("file", 1, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .withGetfh()
                .build();

        COMPOUND4res res = execute(context(vfs), open);
        COMPOUND4res replay = execute(context(vfs), open);

        assertEquals(res.resarray.get(1).opopen.resok4.stateid, replay.resarray.get(1).opopen.resok4.stateid);
        // the current file handle must be restored by the replay
        assertArrayEquals(file.toNfsHandle(), replay.resarray.get(2).opgetfh.resok4.object.value);
    }

    @Test
    public void testOpenFailureReplayed() throws Exception {
        COMPOUND4args open = new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpen("file", 1, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .build();

        executeWithStatus(context(vfs), open, nfsstat.NFSERR_NOENT);

        // the file appears, but the retransmission must get the reply of the original request
        vfs.create(fsRoot, Stat.Type.REGULAR, "file", new Subject(), 0644);
        executeWithStatus(context(vfs), open, nfsstat.NFSERR_NOENT);

        // and the seqid is consumed
        execute(context(vfs), new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpen("file", 2, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .build());
    }

    @Test
    public void testDelayFollowedByNextSeqid() throws Exception {
        VirtualFileSystem delayOnce = delayOnCreate();

        execute(context(vfs), openCreate("file1", 1, OPEN4_SHARE_ACCESS_READ));
        executeWithStatus(context(delayOnce), openCreate("file2", 2, OPEN4_SHARE_ACCESS_READ),
                nfsstat.NFSERR_DELAY);

        // linux client advances the seqid on NFS4ERR_DELAY
        execute(context(delayOnce), openCreate("file2", 3, OPEN4_SHARE_ACCESS_READ));
    }

    @Test
    public void testDelayRetriedWithSameSeqid() throws Exception {
        VirtualFileSystem delayOnce = delayOnCreate();

        execute(context(vfs), openCreate("file1", 1, OPEN4_SHARE_ACCESS_READ));
        executeWithStatus(context(delayOnce), openCreate("file2", 2, OPEN4_SHARE_ACCESS_READ),
                nfsstat.NFSERR_DELAY);

        // macOS client retries with the same seqid
        execute(context(delayOnce), openCreate("file2", 2, OPEN4_SHARE_ACCESS_READ));
    }

    @Test
    public void testOpenConfirmReplayed() throws Exception {
        stateid4 openStateid = open("file", 1, OPEN4_SHARE_ACCESS_READ);

        COMPOUND4args confirm = new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withOpenConfirm(openStateid, 2)
                .build();

        COMPOUND4res res = execute(context(vfs), confirm);
        COMPOUND4res replay = execute(context(vfs), confirm);

        assertEquals(res.resarray.get(1).opopen_confirm.resok4.open_stateid,
                replay.resarray.get(1).opopen_confirm.resok4.open_stateid);
    }

    @Test
    public void testOpenDowngradeReplayed() throws Exception {
        // downgrade is allowed only to a previously seen share access
        confirm("file", open("file", 1, OPEN4_SHARE_ACCESS_READ), 2);

        COMPOUND4args upgrade = new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpen("file", 3, client.getId(), OPEN4_SHARE_ACCESS_BOTH)
                .build();
        upgrade.argarray[1].opopen.share_access = new uint32_t(OPEN4_SHARE_ACCESS_BOTH);
        stateid4 openStateid = execute(context(vfs), upgrade).resarray.get(1).opopen.resok4.stateid;

        COMPOUND4args downgrade = new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withOpenDowngrade(openStateid, 4, OPEN4_SHARE_ACCESS_READ, OPEN4_SHARE_DENY_NONE)
                .build();

        COMPOUND4res res = execute(context(vfs), downgrade);
        // the retransmission carries the stateid that was already bumped by the original request
        COMPOUND4res replay = execute(context(vfs), downgrade);

        assertEquals(res.resarray.get(1).opopen_downgrade.resok4.open_stateid,
                replay.resarray.get(1).opopen_downgrade.resok4.open_stateid);
    }

    @Test
    public void testLockAndUnlockReplayed() throws Exception {
        stateid4 openStateid = confirm("file", open("file", 1, OPEN4_SHARE_ACCESS_READ), 2);

        COMPOUND4args lockNewOwner = new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withLock(nfs_lock_type4.READ_LT, 0, 10, openStateid, 3, client.getId(), LOCK_OWNER, 1)
                .build();

        COMPOUND4res res = execute(context(vfs), lockNewOwner);
        COMPOUND4res replay = execute(context(vfs), lockNewOwner);

        stateid4 lockStateid = res.resarray.get(1).oplock.resok4.lock_stateid;
        assertEquals(lockStateid, replay.resarray.get(1).oplock.resok4.lock_stateid);

        COMPOUND4args lockExistingOwner = new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withLock(nfs_lock_type4.READ_LT, 20, 10, lockStateid, 2)
                .build();

        res = execute(context(vfs), lockExistingOwner);
        replay = execute(context(vfs), lockExistingOwner);

        lockStateid = res.resarray.get(1).oplock.resok4.lock_stateid;
        assertEquals(lockStateid, replay.resarray.get(1).oplock.resok4.lock_stateid);

        COMPOUND4args unlock = new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withLocku(nfs_lock_type4.READ_LT, lockStateid, 3, 0, 30)
                .build();

        res = execute(context(vfs), unlock);
        replay = execute(context(vfs), unlock);

        assertEquals(res.resarray.get(1).oplocku.lock_stateid, replay.resarray.get(1).oplocku.lock_stateid);
    }

    @Test
    public void testLockBadLockSeqidReleasesOpenOwner() throws Exception {
        stateid4 openStateid = confirm("file", open("file", 1, OPEN4_SHARE_ACCESS_READ), 2);

        execute(context(vfs), new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withLock(nfs_lock_type4.READ_LT, 0, 10, openStateid, 3, client.getId(), LOCK_OWNER, 1)
                .build());

        // existing lock-owner with out of order lock seqid
        executeWithStatus(context(vfs), new CompoundBuilder()
                .withPutfh(fileFh("file"))
                .withLock(nfs_lock_type4.READ_LT, 20, 10, openStateid, 4, client.getId(), LOCK_OWNER, 5)
                .build(), nfsstat.NFSERR_BAD_SEQID);

        // the open-owner is neither blocked nor its seqid consumed
        COMPOUND4res res = execute(context(vfs), new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpen("file", 4, client.getId(), OPEN4_SHARE_ACCESS_READ)
                .build());
        assertEquals(nfsstat.NFS_OK, res.resarray.get(1).opopen.status);
    }

    @Test(expected = DelayException.class)
    public void testDuplicateInProgressDelayed() throws Exception {
        StateOwner owner = client.getOrCreateOwner("owner".getBytes(StandardCharsets.UTF_8), new seqid4(1));

        context(vfs).acquireSequence(owner, new seqid4(1));
        // a retransmission arrives while the original request is still executing
        context(vfs).acquireSequence(owner, new seqid4(1));
    }

    private CompoundContext context(VirtualFileSystem fs) throws IOException {
        return new CompoundContextBuilder()
                .withStateHandler(sh)
                .withLockManager(lm)
                .withFs(fs)
                .withMinorversion(0)
                .withCall(generateRpcCall())
                .build();
    }

    private COMPOUND4args openCreate(String path, int seqid, int access) {
        return new CompoundBuilder()
                .withPutfh(rootFh)
                .withOpenCreate(path, seqid, client.getId(), access)
                .build();
    }

    private stateid4 open(String path, int seqid, int access) throws IOException {
        COMPOUND4res res = execute(context(vfs), openCreate(path, seqid, access));
        return res.resarray.get(1).opopen.resok4.stateid;
    }

    private stateid4 confirm(String path, stateid4 stateid, int seqid) throws IOException {
        COMPOUND4res res = execute(context(vfs), new CompoundBuilder()
                .withPutfh(fileFh(path))
                .withOpenConfirm(stateid, seqid)
                .build());
        return res.resarray.get(1).opopen_confirm.resok4.open_stateid;
    }

    private nfs_fh4 fileFh(String path) throws IOException {
        return new nfs_fh4(vfs.lookup(fsRoot, path).toNfsHandle());
    }

    private VirtualFileSystem delayOnCreate() {
        return new ForwardingFileSystem() {
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
    }
}
