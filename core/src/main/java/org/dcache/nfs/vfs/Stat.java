/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Stat {

    public static final int S_TYPE = 0770000; // type mask
    public static final int S_PERMS = 0777;   // permissions mask
    /**
     * Unix domain socket
     */
    public static final int S_IFSOCK = 0140000;
    /**
     * Symbolic link
     */
    public static final int S_IFLNK = 0120000;
    /**
     * Regular file
     */
    public static final int S_IFREG = 0100000;
    /**
     * BLock device
     */
    public static final int S_IFBLK = 0060000;
    /**
     * Directory
     */
    public static final int S_IFDIR = 0040000;
    /**
     * Character device
     */
    public static final int S_IFCHR = 0020000;
    /**
     * Named pipe
     */
    public static final int S_IFIFO = 0010000;

    public enum Type {

        LEGACY,
        REGULAR,
        DIRECTORY,
        SYMLINK,
        CHAR,
        BLOCK,
        FIFO,
        SOCK,
    }

    private int _dev = -1; //
    private int _ino = -1; //
    private int _mode = -1; //
    private int _nlink = -1; //
    private int _owner = -1; //
    private int _group = -1; //
    private int _rdev = -1; //
    private long _size = -1; //
    private long _fileid = -1; //
    private long _generation = -1; //

    /*
     * Opposite to classic Unix, all times in milliseconds
     */
    private long _atime = -1; //
    private long _mtime = -1; //
    private long _ctime = -1; //

    public int getDev() {
        return _dev;
    }

    public void setDev(int dev) {
        _dev = dev;
    }

    public int getIno() {
        return _ino;
    }

    public void setIno(int ino) {
        _ino = ino;
    }

    public int getMode() {
        return _mode;
    }

    public void setMode(int mode) {
        _mode = mode;
    }

    public int getNlink() {
        return _nlink;
    }

    public void setNlink(int nlink) {
        _nlink = nlink;
    }

    public int getUid() {
        return _owner;
    }

    public void setUid(int owner) {
        _owner = owner;
    }

    public int getGid() {
        return _group;
    }

    public void setGid(int group) {
        _group = group;
    }

    public int getRdev() {
        return _rdev;
    }

    public void setRdev(int rdev) {
        _rdev = rdev;
    }

    public long getSize() {
        return _size;
    }

    public void setSize(long size) {
        _size = size;
    }

    public long getATime() {
        return _atime;
    }

    public void setATime(long atime) {
        _atime = atime;
    }

    public long getMTime() {
        return _mtime;
    }

    public void setMTime(long mtime) {
        _mtime = mtime;
    }

    public long getCTime() {
        return _ctime;
    }

    public void setCTime(long ctime) {
        _ctime = ctime;
    }

    public long getFileId() {
        return _fileid;
    }

    public void setFileid(long fileid) {
        _fileid = fileid;
    }

    public long getGeneration() {
        return _generation;
    }

    public void setGeneration(long generation) {
        _generation = generation;
    }

    public Type type() {
        return extractType(_mode);
    }

    public static Type extractType(int mode) {
        switch(mode & S_TYPE) {
            case S_IFBLK:
                return Type.BLOCK;
            case S_IFCHR:
                return Type.CHAR;
            case S_IFDIR:
                return Type.DIRECTORY;
            case S_IFIFO:
                return Type.FIFO;
            case S_IFLNK:
                return Type.SYMLINK;
            case S_IFREG:
                return Type.REGULAR;
            case S_IFSOCK:
                return Type.SOCK;
            default:
                return Type.REGULAR;
        }
    }

    public static String modeToString(int mode) {
        StringBuilder result = new StringBuilder(10);
        switch (extractType(mode)) {
            case BLOCK:
                result.append("b");
                break;
            case CHAR:
                result.append("c");
                break;
            case DIRECTORY:
                result.append("d");
                break;
            case FIFO:
                result.append("p");
                break;
            case SOCK:
                result.append("s");
                break;
            case SYMLINK:
                result.append("l");
                break;
            default:
                result.append("-");
        }
        //owner, group, other
        for (int i=0; i<3; i++) {
            int acl = (mode >> (6 - 3*i)) & 0000007;
            switch (acl) {
                case 00:
                    result.append("---");
                    break;
                case 01:
                    result.append("--x");
                    break;
                case 02:
                    result.append("-w-");
                    break;
                case 03:
                    result.append("-wx");
                    break;
                case 04:
                    result.append("r--");
                    break;
                case 05:
                    result.append("r-x");
                    break;
                case 06:
                    result.append("rw-");
                    break;
                case 07:
                    result.append("rwx");
                    break;
            }
        }
        return result.toString();
    }

    //technically _size (java long) will overflow after ~9 zettabytes, so "Y" is unreachable
    private final static String[] SIZE_UNITS = {"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

    public static String sizeToString(long bytes) {
        int orderOfMagnitude = (int)Math.floor(Math.log(bytes) / Math.log(1024));
        double significantSize = (double)bytes / (1L << orderOfMagnitude*10);
        DecimalFormat sizeFormat = new DecimalFormat("#.#"); //not thread safe
        return sizeFormat.format(significantSize)+SIZE_UNITS[orderOfMagnitude];
    }

    /**
     * @return the equivalent of "ls -lh" (as close as possible)
     */
    @Override
    public String toString() {
        String humanReadableSize = sizeToString(_size);
        String humanReadableMTime = new SimpleDateFormat("MMM dd HH:mm").format(new Date(_mtime)); //not thread safe
        return modeToString(_mode)+" "+String.format("%4d %4d %4d %4s %s", _nlink, _owner, _group, humanReadableSize, humanReadableMTime);
    }
}
