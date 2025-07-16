package org.dcache.nfs.vfs;

import org.dcache.nfs.util.Opaque;

/**
 * Describes metadata determined upon opening the resource at the NFS level.
 * <p>
 * This is used, for example, to infer access privileges that were determined upon opening a resource, so read/write
 * operations don't have to check every time.
 * 
 * @see PseudoFs
 */
public interface OpenHandle {
    /**
     * Returns an opaque bytes representation of the handle.
     * 
     * @return The opaque.
     */
    Opaque getOpaque();
}
