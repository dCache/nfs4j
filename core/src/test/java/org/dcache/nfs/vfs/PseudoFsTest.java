/*
 * Copyright (c) 2009 - 2019 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.vfs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import javax.security.auth.Subject;

import com.google.common.primitives.Longs;
import org.dcache.auth.Subjects;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.status.AccessException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.PermException;
import org.dcache.nfs.status.RoFsException;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcAuthType;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;
import org.dcache.oncrpc4j.rpc.gss.RpcAuthGss;
import org.dcache.oncrpc4j.rpc.gss.RpcGssService;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.junit.Assert.*;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import org.junit.Before;

/**
 *
 * @author tigran
 */
public class PseudoFsTest {

    private final static InetSocketAddress localAddress = new InetSocketAddress(0);
    private VirtualFileSystem vfs;
    private ExportFile mockedExportFile;
    private FsExport mockedExport;
    private RpcTransport mockedTransport;
    private RpcCall mockedRpc;
    private RpcAuth mockedAuth;
    private PseudoFs pseudoFs;
    private Inode fsRoot;

    @Before
    public void setUp() throws IOException {

        vfs = new DummyVFS();
        fsRoot = vfs.getRootInode();

        mockedExportFile = mock(ExportFile.class);
        mockedTransport = mock(RpcTransport.class);
        mockedRpc = mock(RpcCall.class);
        mockedAuth = mock(RpcAuth.class);
        mockedExport = mock(FsExport.class);
        given(mockedExport.getAnonUid()).willReturn(FsExport.DEFAULT_ANON_UID);
        given(mockedExport.getAnonGid()).willReturn(FsExport.DEFAULT_ANON_GID);

    }

    @Test(expected = AccessException.class)
    public void testRootSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .notTrusted()
                .withoutAcl()
                .withSec(FsExport.Sec.NONE)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.create(fsRoot, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 0644);
    }

    @Test
    public void testNoRootSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.NONE)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.create(fsRoot, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 0644);
    }

    @Test(expected = AccessException.class)
    public void testAllSquash() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        given(mockedExport.ioMode()).willReturn(FsExport.IO.RW);
        given(mockedExport.isTrusted()).willReturn(true);
        given(mockedExport.checkAcls()).willReturn(false);
        given(mockedExport.hasAllSquash()).willReturn(true);
        given(mockedExport.getSec()).willReturn(FsExport.Sec.NONE);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .allSquash()
                .withoutAcl()
                .withSec(FsExport.Sec.NONE)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.create(fsRoot, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 0644);
    }

    @Test(expected = PermException.class)
    public void testAuthFlavorTooWeak() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.KRB5)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(fsRoot);
    }

    @Test
    public void testAuthFlavorMatch() throws IOException {

        RpcAuthGss mockedAuthGss = mock(RpcAuthGss.class);

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuthGss.getSubject()).willReturn(Subjects.ROOT);
        given(mockedAuthGss.type()).willReturn(RpcAuthType.RPCGSS_SEC);
        given(mockedAuthGss.getService()).willReturn(RpcGssService.RPC_GSS_SVC_NONE);

        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuthGss);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.KRB5)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(fsRoot);
    }

    @Test
    public void testAuthFlavorStrong() throws IOException {

        RpcAuthGss mockedAuthGss = mock(RpcAuthGss.class);

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuthGss.getSubject()).willReturn(Subjects.ROOT);
        given(mockedAuthGss.type()).willReturn(RpcAuthType.RPCGSS_SEC);
        given(mockedAuthGss.getService()).willReturn(RpcGssService.RPC_GSS_SVC_INTEGRITY);

        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuthGss);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.KRB5)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(fsRoot);
    }

    @Test
    public void testAllRoot() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withAllRoot()
                .withSec(FsExport.Sec.NONE)
                .build("/");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willReturn(Stream.of(export));

        Subject subject = Subjects.of(17, 17);

        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode fileInode = pseudoFs.create(parent, Stat.Type.REGULAR, "aFile", Subjects.ROOT, 0644);
        Stat stat = pseudoFs.getattr(fileInode);

        assertEquals("file's owner no propagated", 17, stat.getUid());
        assertEquals("file's group no propagated", 17, stat.getGid());
    }

    @Test
    public void testReaddirWithSingleExport() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);
        vfs.mkdir(fsRoot, "bar", Subjects.ROOT, 0755);
        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withAllRoot()
                .withSec(FsExport.Sec.NONE)
                .build("/foo");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode pseudoRoot = pseudoFs.getRootInode();

        DirectoryStream directoryStream = pseudoFs.list(pseudoRoot, DirectoryStream.ZERO_VERIFIER, 0);
        assertThat("Empty directory listing", directoryStream, not(emptyIterable()));

        long size = 0;
        for (DirectoryEntry e : directoryStream) {
            assertEquals("Unexpected directory entry", "foo", e.getName());
            size++;
        }
        assertEquals("Unexpected number of directory entries", 1, size);
    }

    @Test(expected = NoEntException.class)
    public void testLookupOfUnexportedDirectory() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);
        vfs.mkdir(fsRoot, "bar", Subjects.ROOT, 0755);
        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withAllRoot()
                .withSec(FsExport.Sec.NONE)
                .build("/foo");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode pseudoRoot = pseudoFs.getRootInode();

        pseudoFs.lookup(pseudoRoot, "bar");
    }

    @Test
    public void testReaddirBehind() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);
        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withAllRoot()
                .withSec(FsExport.Sec.NONE)
                .build("/foo");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode pseudoRoot = pseudoFs.getRootInode();

        DirectoryStream directoryStream = pseudoFs.list(pseudoRoot, DirectoryStream.ZERO_VERIFIER, 0);
        assertThat("Empty directory listing", directoryStream, not(emptyIterable()));


        long lastCookie = 0;
        for(DirectoryEntry e: directoryStream) {
            lastCookie = e.getCookie();
        }

        directoryStream = pseudoFs.list(pseudoRoot, DirectoryStream.ZERO_VERIFIER, lastCookie);
        assertThat("Entry beyond last cookie", directoryStream, is(emptyIterable()));
    }

    @Test(expected = AccessException.class)
    public void testAccessDenyOnNoExport() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.getRootInode();
    }

    @Test(expected = AccessException.class)
    public void testAccessDenyOnHighjackedInode() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Inode inode = new Inode(
                new FileHandle.FileHandleBuilder()
                    .setExportIdx(1)
                    .build(Longs.toByteArray(1L))
            );

        given(mockedExportFile.getExport(1, localAddress.getAddress())).willReturn(null);

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        pseudoFs.getattr(inode);
    }

    @Test(expected = RoFsException.class)
    public void testRejectPseudofsModification() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);
        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.NONE)
                .build("/foo");

        given(mockedExportFile.getExport(fsRoot.exportIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode pseudoRoot = pseudoFs.getRootInode();

        pseudoFs.mkdir(pseudoRoot, "bar", Subjects.ROOT, 0755);
    }

    @Test(expected = AccessException.class)
    public void testRejectRoExportModification() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.ROOT);
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        vfs.mkdir(fsRoot, "foo", Subjects.ROOT, 0755);
        FsExport export = new FsExport.FsExportBuilder()
                .ro()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/foo");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);
        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode exportDir = pseudoFs.lookup(pseudoRoot, "foo");

        pseudoFs.mkdir(exportDir, "bar", Subjects.ROOT, 0755);
    }

    @Test
    public void testAllowOwnerReadXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(1, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        Inode file = vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);
        vfs.setXattr(file, "xattr1", "value1".getBytes(StandardCharsets.UTF_8),
                VirtualFileSystem.SetXattrMode.CREATE);
        FsExport export = new FsExport.FsExportBuilder()
                .ro()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");

        pseudoFs.getXattr(inode, "xattr1");
    }

    @Test(expected = AccessException.class)
    public void testRejectReadXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(17, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .ro()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");
        pseudoFs.getXattr(inode, "xattr1");
    }

    @Test
    public void testAllowOwnerListXattrs() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(1, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .ro()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");

        pseudoFs.listXattrs(inode);
    }

    @Test(expected = AccessException.class)
    public void testRejectListXattrs() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(17, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .ro()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");
        pseudoFs.listXattrs(inode);
    }

    @Test
    public void testAllowOwnerRemoveXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(1, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");

        pseudoFs.removeXattr(inode, "xattr1");
    }

    @Test(expected = AccessException.class)
    public void testRejectRemoveXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(17, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");
        pseudoFs.removeXattr(inode, "xattr1");
    }

    @Test
    public void testAllowOwnerWriteXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(1, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");

        pseudoFs.setXattr(inode, "xattr1", "value1".getBytes(StandardCharsets.UTF_8), VirtualFileSystem.SetXattrMode.CREATE);
    }

    @Test(expected = AccessException.class)
    public void testRejectWriteXattr() throws IOException {

        given(mockedTransport.getRemoteSocketAddress()).willReturn(localAddress);
        given(mockedAuth.getSubject()).willReturn(Subjects.of(1, 1));
        given(mockedAuth.type()).willReturn(RpcAuthType.UNIX);
        given(mockedRpc.getTransport()).willReturn(mockedTransport);
        given(mockedRpc.getCredential()).willReturn(mockedAuth);

        Subject subject = Subjects.of(17, 17);
        Inode parent = vfs.mkdir(fsRoot, "dir", subject, 0755);
        vfs.create(parent, Stat.Type.REGULAR, "aFile", subject, 0600);

        FsExport export = new FsExport.FsExportBuilder()
                .rw()
                .trusted()
                .withoutAcl()
                .withSec(FsExport.Sec.SYS)
                .build("/dir");

        given(mockedExportFile.getExport(export.getIndex(), localAddress.getAddress())).willReturn(export);
        given(mockedExportFile.exports(localAddress.getAddress())).willAnswer(x -> Stream.of(export));

        pseudoFs = new PseudoFs(vfs, mockedRpc, mockedExportFile);

        Inode pseudoRoot = pseudoFs.getRootInode();
        Inode dir = pseudoFs.lookup(pseudoRoot, "dir");
        Inode inode = pseudoFs.lookup(dir, "aFile");
        pseudoFs.setXattr(inode, "xattr1", "value1".getBytes(StandardCharsets.UTF_8), VirtualFileSystem.SetXattrMode.CREATE);
    }

}
