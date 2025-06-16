package org.dcache.testutils;

import org.dcache.nfs.v3.xdr.READDIR3args;
import org.dcache.nfs.v3.xdr.READDIRPLUS3args;
import org.dcache.nfs.v3.xdr.cookie3;
import org.dcache.nfs.v3.xdr.cookieverf3;
import org.dcache.nfs.v3.xdr.count3;
import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.nfs.v3.xdr.nfs_fh3;
import org.dcache.nfs.v3.xdr.uint32;
import org.dcache.nfs.v3.xdr.uint64;
import org.dcache.nfs.vfs.FileHandle;

public class NfsV3Ops {
    public static READDIR3args readDir(FileHandle requestedDirHandle) {
        return readDir(requestedDirHandle, 0, new byte[nfs3_prot.NFS3_COOKIEVERFSIZE], Integer.MAX_VALUE);
    }

    public static READDIR3args readDir(FileHandle requestedDirHandle, int maxResponseSize) {
        return readDir(requestedDirHandle, 0, new byte[nfs3_prot.NFS3_COOKIEVERFSIZE], maxResponseSize);
    }

    public static READDIR3args readDir(FileHandle requestedDirHandle, long cookie, byte[] cookieVerifier) {
        return readDir(requestedDirHandle, cookie, cookieVerifier, Integer.MAX_VALUE);
    }

    public static READDIR3args readDir(FileHandle requestedDirHandle, long cookie, byte[] cookieVerifier,
            int maxResponseBytes) {
        READDIR3args args = new READDIR3args();
        args.dir = new nfs_fh3();
        args.dir.data = requestedDirHandle.bytes();
        args.cookie = new cookie3(new uint64(cookie));
        args.cookieverf = new cookieverf3(cookieVerifier);
        args.count = new count3(new uint32(maxResponseBytes));
        return args;
    }

    public static READDIRPLUS3args readDirPlus(FileHandle requestedDirHandle) {
        return readDirPlus(requestedDirHandle, 0, new byte[nfs3_prot.NFS3_COOKIEVERFSIZE], Integer.MAX_VALUE,
                Integer.MAX_VALUE);
    }

    public static READDIRPLUS3args readDirPlus(FileHandle requestedDirHandle, int maxResponseSize) {
        return readDirPlus(requestedDirHandle, 0, new byte[nfs3_prot.NFS3_COOKIEVERFSIZE], maxResponseSize,
                maxResponseSize);
    }

    public static READDIRPLUS3args readDirPlus(FileHandle requestedDirHandle, long cookie, byte[] cookieVerifier) {
        return readDirPlus(requestedDirHandle, cookie, cookieVerifier, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static READDIRPLUS3args readDirPlus(FileHandle requestedDirHandle, long cookie, byte[] cookieVerifier,
            int maxResponseBytes, int maxDirectoryListingBytes) {
        READDIRPLUS3args args = new READDIRPLUS3args();
        args.dir = new nfs_fh3();
        args.dir.data = requestedDirHandle.bytes();
        args.cookie = new cookie3(new uint64(cookie));
        args.cookieverf = new cookieverf3(cookieVerifier);
        args.maxcount = new count3(new uint32(maxResponseBytes));
        args.dircount = new count3(new uint32(maxDirectoryListingBytes));
        return args;
    }
}
