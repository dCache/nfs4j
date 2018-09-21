/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

public class Stat implements Serializable, Cloneable {

    public enum StatAttribute {
        DEV,
        INO,
        MODE,
        NLINK,
        OWNER,
        GROUP,
        RDEV,
        SIZE,
        FILEID,
        GENERATION,
        ATIME,
        MTIME,
        CTIME
    };

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter LS_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd HH:mm");

    /**
     * Set of attributes defined in this {@code stat} object.
     */
    private final EnumSet<StatAttribute> _definedAttrs = EnumSet.noneOf(StatAttribute.class);

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
        SOCK;

        public int toMode() {
            switch (this) {
                case REGULAR:
                    return S_IFREG;
                case DIRECTORY:
                    return S_IFDIR;
                case SYMLINK:
                    return S_IFLNK;
                case CHAR:
                    return S_IFCHR;
                case BLOCK:
                    return S_IFBLK;
                case FIFO:
                    return S_IFIFO;
                case SOCK:
                    return S_IFSOCK;
                default:
                    throw new IllegalArgumentException("unhandled: "+this);
            }
        }

        public static Type fromMode(int mode) {
            switch(mode & S_TYPE) {
                case S_IFREG:
                    return Type.REGULAR;
                case S_IFDIR:
                    return Type.DIRECTORY;
                case S_IFLNK:
                    return Type.SYMLINK;
                case S_IFCHR:
                    return Type.CHAR;
                case S_IFBLK:
                    return Type.BLOCK;
                case S_IFIFO:
                    return Type.FIFO;
                case S_IFSOCK:
                    return Type.SOCK;
                default:
                    return Type.REGULAR;
            }
        }
    }

    private int _dev;
    private int _ino;
    private int _mode;
    private int _nlink;
    private int _owner;
    private int _group;
    private int _rdev;
    private long _size;
    private long _fileid;
    private long _generation;

    /*
     * Opposite to classic Unix, all times in milliseconds
     */
    private long _atime;
    private long _mtime;
    private long _ctime;

    public int getDev() {
        guard(StatAttribute.DEV);
        return _dev;
    }

    public void setDev(int dev) {
        define(StatAttribute.DEV);
        _dev = dev;
    }

    public int getIno() {
        guard(StatAttribute.INO);
        return _ino;
    }

    public void setIno(int ino) {
        define(StatAttribute.INO);
        _ino = ino;
    }

    public int getMode() {
        guard(StatAttribute.MODE);
        return _mode;
    }

    public void setMode(int mode) {
        define(StatAttribute.MODE);
        _mode = mode;
    }

    public int getNlink() {
        guard(StatAttribute.NLINK);
        return _nlink;
    }

    public void setNlink(int nlink) {
        define(StatAttribute.NLINK);
        _nlink = nlink;
    }

    public int getUid() {
        guard(StatAttribute.OWNER);
        return _owner;
    }

    public void setUid(int owner) {
        define(StatAttribute.OWNER);
        _owner = owner;
    }

    public int getGid() {
        guard(StatAttribute.GROUP);
        return _group;
    }

    public void setGid(int group) {
        define(StatAttribute.GROUP);
        _group = group;
    }

    public int getRdev() {
        guard(StatAttribute.RDEV);
        return _rdev;
    }

    public void setRdev(int rdev) {
        define(StatAttribute.RDEV);
        _rdev = rdev;
    }

    public long getSize() {
        guard(StatAttribute.SIZE);
        return _size;
    }

    public void setSize(long size) {
        define(StatAttribute.SIZE);
        _size = size;
    }

    public long getATime() {
        guard(StatAttribute.ATIME);
        return _atime;
    }

    public void setATime(long atime) {
        define(StatAttribute.ATIME);
        _atime = atime;
    }

    public long getMTime() {
        guard(StatAttribute.MTIME);
        return _mtime;
    }

    public void setMTime(long mtime) {
        define(StatAttribute.MTIME);
        _mtime = mtime;
    }

    public long getCTime() {
        guard(StatAttribute.CTIME);
        return _ctime;
    }

    public void setCTime(long ctime) {
        define(StatAttribute.CTIME);
        _ctime = ctime;
    }

    public long getFileId() {
        guard(StatAttribute.FILEID);
        return _fileid;
    }

    public void setFileid(long fileid) {
        define(StatAttribute.FILEID);
        _fileid = fileid;
    }

    public long getGeneration() {
        guard(StatAttribute.GENERATION);
        return _generation;
    }

    public void setGeneration(long generation) {
        define(StatAttribute.GENERATION);
        _generation = generation;
    }

    public Type type() {
        guard(StatAttribute.MODE);
        return Type.fromMode(_mode);
    }

    public static String modeToString(int mode) {
        StringBuilder result = new StringBuilder(10);
        switch (Type.fromMode(mode)) {
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

    //technically _size (java long) will overflow after ~8 exabytes, so "Z"/"Y" is unreachable
    private final static String[] SIZE_UNITS = {"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

    public static String sizeToString(long bytes) {
        if (bytes==0) {
            return "0";
        }
        int orderOfMagnitude = (int)Math.floor(Math.log(bytes) / Math.log(1024));
        double significantSize = (double)bytes / (1L << orderOfMagnitude*10);
        DecimalFormat sizeFormat = new DecimalFormat("#.#"); //not thread safe
        return sizeFormat.format(significantSize)+SIZE_UNITS[orderOfMagnitude];
    }

    @Override
    public Stat clone() {
        try {
            return (Stat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return the equivalent of "ls -lh" (as close as possible)
     */
    @Override
    public String toString() {
        String humanReadableSize = sizeToString(_size);
        String humanReadableMTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(_mtime), ZoneId.systemDefault())
                .format(LS_TIME_FORMAT);
        return modeToString(_mode)+" "+String.format("%4d %4d %4d %4s %s", _nlink, _owner, _group, humanReadableSize, humanReadableMTime);
    }

    /**
     * Check is attribute defined in this {@code stat} object;
     * @param attr attribute to check
     * @return true iff specified attribute is defined in this stat object.
     */
    public boolean isDefined(StatAttribute attr) {
        return _definedAttrs.contains(attr);
    }

    /**
     * Undefine attribute this {@code stat} object. Accessing an attribute
     * after it's undefined will throw {@link IllegalStateException}.
     *
     * @param attr attribute to undefine.
     */
    public void undefine(StatAttribute attr) {
        _definedAttrs.remove(attr);
    }

    /** Throws IllegalStateException if attribute is not defined. */
    private void guard(StatAttribute attr) throws IllegalStateException {
        if ( !isDefined(attr)) {
            throw new IllegalStateException("Attribute is not defined: " + attr);
        }
    }

    private void define(StatAttribute attr)  {
	_definedAttrs.add(attr);
    }
}
