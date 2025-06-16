package org.dcache.nfs.v4;

import static org.dcache.nfs.v4.NfsTestUtils.generateRpcCall;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.NoFileHandleException;
import org.dcache.nfs.status.RestoreFhException;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.vfs.Inode;
import org.junit.Before;
import org.junit.Test;

public class CompoundContextTest {

    private CompoundContext context;

    @Before
    public void setUp() {
        context = new CompoundContextBuilder()
                .withMinorversion(1)
                .withCall(generateRpcCall())
                .build();
    }

    @Test(expected = NoFileHandleException.class)
    public void testNoCurrentFileHandle() throws ChimeraNFSException {
        context.currentInode();
    }

    @Test
    public void testCurrentFileHandle() throws ChimeraNFSException {
        Inode inode = mock(Inode.class);
        context.currentInode(inode);

        assertSame(inode, context.currentInode());
    }

    @Test(expected = BadStateidException.class)
    public void testNoCurrentStateid() throws ChimeraNFSException {
        context.currentStateid();
    }

    @Test
    public void testCurrentStateid() throws ChimeraNFSException {
        stateid4 stateid = mock(stateid4.class);
        context.currentStateid(stateid);

        assertSame(stateid, context.currentStateid());
    }

    @Test(expected = NoFileHandleException.class)
    public void testNoSavedFileHandle() throws ChimeraNFSException {
        context.savedInode();
    }

    @Test(expected = NoFileHandleException.class)
    public void testNoCurrentFileHandleOnSave() throws ChimeraNFSException {
        context.saveCurrentInode();
    }

    @Test
    public void testSavedFileHandle() throws ChimeraNFSException {
        Inode inode = mock(Inode.class);
        context.currentInode(inode);
        context.saveCurrentInode();

        assertSame(inode, context.savedInode());
    }

    @Test(expected = RestoreFhException.class)
    public void testReastireCurrentFileHandleNoSaved() throws ChimeraNFSException {
        context.restoreSavedInode();
    }

    @Test
    public void testRestoreCurrentFileHandle() throws ChimeraNFSException {
        Inode inode = mock(Inode.class);
        context.currentInode(inode);
        context.saveCurrentInode();

        context.clearCurrentInode();
        context.restoreSavedInode();

        assertSame(inode, context.currentInode());
    }

    @Test
    public void testRestoreCurrentStateid() throws ChimeraNFSException {
        Inode inode = mock(Inode.class);
        stateid4 stateid = mock(stateid4.class);

        context.currentInode(inode);
        context.currentStateid(stateid);
        context.saveCurrentInode();

        context.clearCurrentInode();
        context.currentStateid(null);

        context.restoreSavedInode();

        assertSame(inode, context.currentInode());
    }

}
