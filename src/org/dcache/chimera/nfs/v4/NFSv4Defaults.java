package org.dcache.chimera.nfs.v4;

public interface NFSv4Defaults {

    public static final int NFS4_LEASE_TIME = 90;
    public final static int NFS4_MAXFILENAME = 255;
    // maximal read/write buffer size
    public static final long NFS4_MAXIOBUFFERSIZE = 4096;

    // max file size 28Gb
    public final static long NFS4_MAXFILESIZE = 28L*1024L*1024L*1024L;

    /**
     * max link count
     */
    public final static int NFS4_MAXLINK = 255;

    // setting the stripe size
    public static final int NFS4_STRIPE_SIZE = 8192;


    /**
     * NFSv4.1 implementation ID
     */
    public final static String NFS4_IMPLEMENTATION_ID = "Chimera NFSv4.1";

    /**
     * NFSv4.1 implementation domain
     */
    public final static String NFS4_IMPLEMENTATION_DOMAIN = "dCache.ORG";

    /**
     * NFSv4.1 implementation date
     */
    public final static long NFS4_IMPLEMENTATION_DATE = System.currentTimeMillis();

}
