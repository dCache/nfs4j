/*
 * $Id:NFSHandle.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs;


import org.dcache.chimera.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

public class NFSHandle {

    private static final Logger _log = LoggerFactory.getLogger(NFSHandle.class);

    private NFSHandle() {
        // no instance allowed
    }

    public static FsInode toFsInode(FileSystemProvider fs, byte[] handle) {

        FsInode inode = null;

        String strHandle = new String(handle);

        _log.debug("Processing FH: {}", strHandle );

        StringTokenizer st = new StringTokenizer(strHandle, "[:]");

        if (st.countTokens() < 3) {
            throw new IllegalArgumentException("Invalid HimeraNFS handler.("
                    + strHandle + ")");
        }

        /*
         * reserved for future use
         */
        int fsId = Integer.parseInt(st.nextToken());

        String type = st.nextToken();

        try {
            // IllegalArgumentException will be thrown is it's wrong type

            FsInodeType inodeType = FsInodeType.valueOf(type);
            String id;
            int argc;
            String[] args;

            switch (inodeType) {
                case INODE:
                    id = st.nextToken();
                    int level = 0;
                    if (st.countTokens() > 0) {
                        level = Integer.parseInt(st.nextToken());
                    }
                    inode = new FsInode(fs, id, level);
                    break;

                case ID:
                    id = st.nextToken();
                    inode = new FsInode_ID(fs, id);
                    break;

                case TAGS:
                    id = st.nextToken();
                    inode = new FsInode_TAGS(fs, id);
                    break;

                case TAG:
                    id = st.nextToken();
                    String tag = st.nextToken();
                    inode = new FsInode_TAG(fs, id, tag);
                    break;

                case NAMEOF:
                    id = st.nextToken();
                    inode = new FsInode_NAMEOF(fs, id);
                    break;
                case PARENT:
                    id = st.nextToken();
                    inode = new FsInode_PARENT(fs, id);
                    break;

                case PATHOF:
                    id = st.nextToken();
                    inode = new FsInode_PATHOF(fs, id);
                    break;

                case CONST:
                    String cnst = st.nextToken();
                    inode = new FsInode_CONST(fs, cnst);
                    break;

                case PSET:
                    id = st.nextToken();
                    argc = st.countTokens();
                    args = new String[argc];
                    for (int i = 0; i < argc; i++) {
                        args[i] = st.nextToken();
                    }
                    inode = new FsInode_PSET(fs, id, args);
                    break;

                case PGET:
                    id = st.nextToken();
                    argc = st.countTokens();
                    args = new String[argc];
                    for (int i = 0; i < argc; i++) {
                        args[i] = st.nextToken();
                    }
                    inode = new FsInode_PGET(fs, id, args);
                    break;

            }
        } catch (IllegalArgumentException iae) {
            _log.info("Failed to generate an inode from file handle : {} : {}", strHandle, iae);
            inode = null;
        }

        return inode;

    }

}
