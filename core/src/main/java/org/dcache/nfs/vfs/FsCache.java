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

import com.google.common.cache.*;
import com.google.common.io.BaseEncoding;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tigran
 */
public class FsCache {

    private final static Logger _log = LoggerFactory.getLogger(FsCache.class);

    private static class FileChannelSupplier extends CacheLoader<Inode, FileChannel> {

        private final File _base;

        FileChannelSupplier(File base) {
            if (!base.isDirectory()) {
                throw new IllegalArgumentException(base + " : not exist or not a directory");
            }
            _base = base;
        }

        @Override
        public FileChannel load(Inode inode) throws IOException {
            byte[] fid = inode.getFileId();
            String id = BaseEncoding.base16().lowerCase().encode(fid);
            File dir = getAndCreateDirectory(id);
            File f = new File(dir, id);
            return new RandomAccessFile(f, "rw").getChannel();
        }

        private File getAndCreateDirectory(String id) {
            int len = id.length();
            String topLevelDir = id.substring(len - 6, len - 4);
            String subDir = id.substring(len - 4, len - 2);
            File dir = new File(_base, topLevelDir + "/" + subDir);
            dir.mkdirs();
            return dir;
        }
    }

    private static class InodeGarbageCollector implements RemovalListener<Inode, FileChannel> {

        @Override
        public void onRemoval(RemovalNotification<Inode, FileChannel> notification) {
            try {
                notification.getValue().close();
            } catch (IOException e) {
                _log.error("Failed to close file channel of {} : {}",
                        notification.getKey(), e.getMessage());
            }
        }
    }
    private LoadingCache<Inode, FileChannel> _cache;
    private int _maxSize;
    private int _lastAccess;
    private File _base;

    public void setBase(File base) {
        this._base = base;
    }

    public void setMaxSize(int maxSize) {
        this._maxSize = maxSize;
    }

    public void setLastAccess(int timeInSec) {
        _lastAccess = timeInSec;
    }
    public void init() {
        _cache = CacheBuilder.newBuilder()
                .maximumSize(_maxSize)
                .expireAfterAccess(_lastAccess, TimeUnit.SECONDS)
                .removalListener(new InodeGarbageCollector())
                .build(new FileChannelSupplier(_base));
    }

    public FileChannel get(Inode inode) {
        return _cache.getUnchecked(inode);
    }

}
