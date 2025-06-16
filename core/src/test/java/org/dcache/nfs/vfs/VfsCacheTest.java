package org.dcache.nfs.vfs;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;

import javax.security.auth.Subject;

import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.junit.Before;
import org.junit.Test;

public class VfsCacheTest {

    private Subject subject = new Subject();

    private VirtualFileSystem vfs;
    private VfsCache vfsCache;

    private Inode root;

    @Before
    public void setUp() throws Exception {
        vfs = spy(new DummyVFS());
        root = vfs.getRootInode();

        VfsCacheConfig cacheConfig = new VfsCacheConfig();
        cacheConfig.setMaxEntries(5);
        cacheConfig.setReaddirMaxEntries(5);
        cacheConfig.setLifeTime(1);
        cacheConfig.setReaddirLifeTime(1);
        cacheConfig.setFsStatLifeTime(1);
        vfsCache = new VfsCache(vfs, cacheConfig);
    }

    @Test
    public void shouldReadThroughOnEmptyCache() throws IOException {

        vfsCache.getattr(root);
        verify(vfs).getattr(root);
    }

    @Test
    public void shouldReadThroughOnEmptyLookupCache() throws IOException {

        createFile(root, "foo");
        vfsCache.lookup(root, "foo");
        verify(vfs).lookup(root, "foo");
    }

    @Test
    public void shouldUseLookupCache() throws IOException {

        createFile(root, "foo");
        vfsCache.lookup(root, "foo");
        vfsCache.lookup(root, "foo");
        verify(vfs, times(1)).lookup(root, "foo");
    }

    @Test
    public void shouldUseCachedValue() throws IOException {

        vfsCache.getattr(root);
        vfsCache.getattr(root);
        verify(vfs, times(1)).getattr(root);
    }

    @Test
    public void shouldWriteThrough() throws IOException {

        Stat s = new Stat();
        vfsCache.setattr(root, s);

        verify(vfs).setattr(root, s);
    }

    @Test
    public void shouldInvalidateCacheOnWrite() throws IOException {

        Stat s = new Stat();

        vfsCache.getattr(root);
        vfsCache.setattr(root, s);
        vfsCache.getattr(root);
        verify(vfs, times(2)).getattr(root);
    }

    @Test
    public void shouldInvalidateOwnCacheEntryOnly() throws IOException {

        Stat s = new Stat();

        Inode foo = createFile(root, "foo");
        Inode bar = createFile(root, "bar");

        vfsCache.getattr(foo);
        vfsCache.setattr(bar, s);
        vfsCache.getattr(foo);
        verify(vfs, times(1)).getattr(foo);
    }

    @Test
    public void shouldInvalidateCacheOnCreate() throws IOException {

        vfsCache.getattr(root);
        vfsCache.create(root, Stat.Type.REGULAR, "foo", subject, 0640);
        vfsCache.getattr(root);
        verify(vfs, times(2)).getattr(root);
    }

    @Test
    public void shouldInvalidateCacheOnMkDir() throws IOException {

        vfsCache.getattr(root);
        vfsCache.mkdir(root, "foo", new Subject(), 0600);
        vfsCache.getattr(root);
        verify(vfs, times(2)).getattr(root);
    }

    @Test
    public void shouldInvalidateCacheOnMove() throws IOException {

        Inode src = createDir(root, "dirOne");
        Inode dst = createDir(root, "dirTwo");
        createFile(src, "foo");

        vfsCache.getattr(src);
        vfsCache.getattr(dst);

        vfsCache.move(src, "foo", dst, "bar");

        vfsCache.getattr(src);
        vfsCache.getattr(dst);

        verify(vfs, times(2)).getattr(src);
        verify(vfs, times(2)).getattr(dst);
    }

    @Test
    public void shouldInvalidateCacheOnRemove() throws IOException {

        createFile(root, "foo");
        vfsCache.getattr(root);
        vfsCache.remove(root, "foo");
        vfsCache.getattr(root);

        verify(vfs, times(2)).getattr(root);
    }

    @Test
    public void shouldInvalidateCacheOnSymlink() throws IOException {

        Inode foo = createFile(root, "foo");
        vfsCache.getattr(root);
        vfsCache.getattr(foo);
        vfsCache.symlink(root, "bar", "foo", subject, 0640);
        vfsCache.getattr(root);
        vfsCache.getattr(foo);

        verify(vfs, times(2)).getattr(root);
        verify(vfs, times(1)).getattr(foo);
    }

    @Test
    public void shouldInvalidateCacheOnLink() throws IOException {

        Inode src = createDir(root, "dirOne");
        Inode dst = createDir(root, "dirTwo");
        Inode file = createFile(src, "foo");

        vfsCache.getattr(src);
        vfsCache.getattr(dst);
        vfsCache.getattr(file);

        vfsCache.link(dst, file, "bar", subject);

        vfsCache.getattr(src);
        vfsCache.getattr(dst);
        vfsCache.getattr(file);

        verify(vfs, times(1)).getattr(src);
        verify(vfs, times(2)).getattr(dst);
        verify(vfs, times(2)).getattr(file);
    }

    @Test
    public void shouldInvalidateCacheOnXattrSet() throws IOException {

        Inode file = createFile(root, "foo");

        vfsCache.getattr(file);

        vfsCache.setXattr(file, "attr1", new byte[] {0x01}, VirtualFileSystem.SetXattrMode.CREATE);
        vfsCache.getattr(file);

        verify(vfs, times(2)).getattr(file);
    }

    @Test
    public void shouldInvalidateCacheOnXattrRemove() throws IOException {

        Inode file = createFile(root, "foo");

        vfsCache.setXattr(file, "attr1", new byte[] {0x01}, VirtualFileSystem.SetXattrMode.CREATE);
        vfsCache.getattr(file);
        vfsCache.removeXattr(file, "attr1");
        vfsCache.getattr(file);

        verify(vfs, times(2)).getattr(file);
    }

    @Test
    public void shouldReadThroughOnEmptyReaddirCache() throws IOException {

        vfsCache.list(root, DirectoryStream.ZERO_VERIFIER, 0L);
        verify(vfs).list(root, DirectoryStream.ZERO_VERIFIER, 0L);
    }

    @Test
    public void shouldUseReaddirCache() throws IOException {

        vfsCache.list(root, DirectoryStream.ZERO_VERIFIER, 0L);
        vfsCache.list(root, DirectoryStream.ZERO_VERIFIER, 0L);

        verify(vfs, times(1)).list(root, DirectoryStream.ZERO_VERIFIER, 0L);
    }

    @Test
    public void shouldUseReaddirCacheWithCookie() throws IOException {

        DirectoryStream stream = vfsCache.list(root, DirectoryStream.ZERO_VERIFIER, 0L);
        vfsCache.list(root, stream.getVerifier(), 5L);

        verify(vfs, times(1)).list(root, DirectoryStream.ZERO_VERIFIER, 0L);
    }

    @Test
    public void shouldReadThroughReaddirCacheOnUnknownVerifier() throws IOException {

        vfsCache.list(root, DirectoryStream.ZERO_VERIFIER, 0L);
        vfsCache.list(root, Arrays.copyOf(new byte[] {0x01}, nfs4_prot.NFS4_VERIFIER_SIZE), 0L);

        verify(vfs, times(2)).list(root, DirectoryStream.ZERO_VERIFIER, 0L);
    }

    private Inode createFile(Inode parent, String name) throws IOException {
        return vfs.create(parent, Stat.Type.REGULAR, name, subject, 0640);
    }

    private Inode createDir(Inode parent, String name) throws IOException {
        return vfs.mkdir(parent, name, subject, 0640);
    }

}
