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
package org.dcache.nfs.v3;

import org.dcache.nfs.v3.xdr.fattr3;
import org.dcache.nfs.v3.xdr.fileid3;
import org.dcache.nfs.v3.xdr.ftype3;
import org.dcache.nfs.v3.xdr.gid3;
import org.dcache.nfs.v3.xdr.mode3;
import org.dcache.nfs.v3.xdr.nfstime3;
import org.dcache.nfs.v3.xdr.post_op_attr;
import org.dcache.nfs.v3.xdr.pre_op_attr;
import org.dcache.nfs.v3.xdr.sattr3;
import org.dcache.nfs.v3.xdr.size3;
import org.dcache.nfs.v3.xdr.specdata3;
import org.dcache.nfs.v3.xdr.time_how;
import org.dcache.nfs.v3.xdr.uid3;
import org.dcache.nfs.v3.xdr.uint32;
import org.dcache.nfs.v3.xdr.uint64;
import org.dcache.nfs.v3.xdr.wcc_attr;
import org.dcache.nfs.v3.xdr.wcc_data;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class HimeraNfsUtils {


	private static final int MODE_MASK = 0770000;

	private static final Logger _log = LoggerFactory.getLogger(HimeraNfsUtils.class);

    private HimeraNfsUtils() {
        // no instance allowed
    }

    public static void fill_attributes(Stat stat,  fattr3 at) {

        at.type = unixType2NFS(stat.getMode());
        at.mode = new mode3(new uint32( stat.getMode()  & 0777777 ) );

        //public int nlink;
        at.nlink= new uint32( stat.getNlink() );

        //public int uid;
        at.uid= new uid3( new uint32(stat.getUid()) );

        //public int gid;
        at.gid=new gid3(new uint32( stat.getGid()) );

        //public int rdev;
        at.rdev = new specdata3();
        at.rdev.specdata1 = new uint32(19);  		// ARBITRARY
        at.rdev.specdata2 = new uint32(17);
        //public int blocks;

        //public int fsid;
        at.fsid= new uint64( stat.getDev() );

        //public int fileid;
        // Get some value for this file/dir
        at.fileid = new fileid3(new uint64( stat.getFileId() ) );

        at.size = new size3( new uint64( stat.getSize() ) );
        at.used = new size3( new uint64( stat.getSize() ) );

        //public nfstime atime;
        at.atime = convertTimestamp(stat.getATime());
        //public nfstime mtime;
        at.mtime = convertTimestamp(stat.getMTime());
        //public nfstime ctime;
        at.ctime = convertTimestamp(stat.getCTime());
    }


    public static void fill_attributes(Stat stat,  wcc_attr at) {

        at.size = new size3( new uint64( stat.getSize() ) );
        //public nfstime mtime;
        at.mtime = convertTimestamp(stat.getMTime());
        //public nfstime ctime;
        at.ctime = convertTimestamp(stat.getCTime());
    }

    public static nfstime3 convertTimestamp(long gmtMillis) {
        nfstime3 result = new nfstime3();
        result.seconds = new uint32( (int)TimeUnit.SECONDS.convert(gmtMillis , TimeUnit.MILLISECONDS) ); //== / 1000
        result.nseconds = new uint32((int)(1000000 * (gmtMillis % 1000))); //take millis rounded off above, multiply by 1 mil for nanos
        return result;
    }

    public static long convertTimestamp(nfstime3 gmtNanos) {
        return ((long)gmtNanos.seconds.value)*1000 + ((long)gmtNanos.nseconds.value)/1000000;
    }

    public static void set_sattr(Inode inode, VirtualFileSystem fs, sattr3 s) throws IOException {

        Stat stat = new Stat();
        long now = System.currentTimeMillis();

        if( s.uid.set_it ) {
            stat.setUid( s.uid.uid.value.value);
        }

        if( s.gid.set_it ) {
            stat.setGid(s.gid.gid.value.value);
        }

        if( s.mode.set_it  ) {
            int mode = s.mode.mode.value.value;
            _log.debug("New mode [{}]", Integer.toOctalString(mode));
            stat.setMode(mode);
        }

        if( s.size.set_it ) {
            stat.setSize( s.size.size.value.value);
        }

   /*     switch( s.atime.set_it ) {

            case time_how.SET_TO_SERVER_TIME:
            	inode.setATime( System.currentTimeMillis()/1000 );
                break;
            case time_how.SET_TO_CLIENT_TIME:
            	inode.setATime(  (long) s.atime.atime.seconds.value );
                break;
            default:
        } */

        switch( s.mtime.set_it ) {

            case time_how.SET_TO_SERVER_TIME:
                stat.setMTime( now );
                break;
            case time_how.SET_TO_CLIENT_TIME:
                // update mtime only if it's more than 10 seconds
                long mtime =  TimeUnit.MILLISECONDS.convert(s.mtime.mtime.seconds.value , TimeUnit.SECONDS)  +
                	TimeUnit.MILLISECONDS.convert(s.mtime.mtime.nseconds.value , TimeUnit.NANOSECONDS);
                stat.setMTime(  mtime );
                break;
            default:
        }

        fs.setattr(inode, stat);
    }


    static int unixType2NFS( int type ) {

        int ret;

        switch ( type & MODE_MASK  ) {

            case Stat.S_IFREG:
                ret = ftype3.NF3REG;
                break;
            case Stat.S_IFDIR:
                ret = ftype3.NF3DIR;
                break;
            case Stat.S_IFLNK:
                ret = ftype3.NF3LNK;
                break;
            case Stat.S_IFSOCK:
                ret = ftype3.NF3SOCK;
                break;
            case Stat.S_IFBLK:
                ret = ftype3.NF3BLK;
                break;
            case Stat.S_IFCHR:
                ret = ftype3.NF3CHR;
                break;
            case Stat.S_IFIFO:
                ret = ftype3.NF3FIFO;
                break;
            default:
                _log.info("Unknown mode [{}]", Integer.toOctalString(type));
                ret = 0;
        }

        return ret;
    }

    /**
     * Create empty post operational attributes.
     * @return attrs
     */
    public static post_op_attr defaultPostOpAttr() {
        post_op_attr postOpAttr = new post_op_attr();
        postOpAttr.attributes_follow = false;
        return postOpAttr;
    }

    /**
     * Create empty pre operational attributes;
     * @return attrs
     */
    public static pre_op_attr defaultPreOpAttr() {
        pre_op_attr preOpAttr = new pre_op_attr();
        preOpAttr.attributes_follow = false;
        return preOpAttr;
    }

    /**
     * Create empty weak cache consistency information.
     * @return cache entry
     */
    public static wcc_data defaultWccData() {
        wcc_data wccData = new wcc_data();
        wccData.after = defaultPostOpAttr();
        wccData.before = defaultPreOpAttr();
        return wccData;
    }
}
