/*
 * $Id:NFSHandle.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs;


import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.FsInodeType;
import org.dcache.chimera.FsInode_CONST;
import org.dcache.chimera.FsInode_ID;
import org.dcache.chimera.FsInode_NAMEOF;
import org.dcache.chimera.FsInode_PARENT;
import org.dcache.chimera.FsInode_PATHOF;
import org.dcache.chimera.FsInode_PGET;
import org.dcache.chimera.FsInode_PSET;
import org.dcache.chimera.FsInode_TAG;
import org.dcache.chimera.FsInode_TAGS;

public class NFSHandle {

    private static final Logger _log = Logger.getLogger(NFSHandle.class.getName());

    private NFSHandle() {
        // no instance allowed
    }

    public static FsInode toFsInode(FileSystemProvider fs, byte[] handle) {

        FsInode inode = null;

        String strHandle = new String(handle);

        _log.log(Level.FINEST, "Processing FH: {0}", strHandle );

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
            _log.log(Level.INFO, "Failed to generate an inode from file handle : {0} : {1}",
                    new Object[] {strHandle, iae});
            inode = null;
        }

        return inode;

    }

}
