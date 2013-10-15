/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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

public class Stat {

    public static final int S_TYPE = 0770000; // type mask
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
    private long _fileid = -1;

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

    public Type type() {
        switch(_mode & S_TYPE) {
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
}
