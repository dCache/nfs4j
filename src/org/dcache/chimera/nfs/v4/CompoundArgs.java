/*
 * $Id:CompoundArgs.java 140 2007-06-07 13:44:55Z tigran $
 */
package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FsInode;


public class CompoundArgs {

    
	private static final Logger _log = Logger.getLogger(CompoundArgs.class.getName());
    
    private FsInode _rootInode = null;
    private FsInode _currentInode = null;
    private FsInode _savedInode = null;

    private final int _minorversion;
    private int _position = 0;
    
    private NFSv41Session _session = null;
    
    public CompoundArgs(int minorversion) {
    	_minorversion = minorversion;
    }
    
    public int getMinorversion() {
    	return _minorversion;
    }
    
    public FsInode currentInode() throws ChimeraNFSException {
        if( _currentInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        return _currentInode;
    }

    public void currentInode(FsInode inode) throws ChimeraNFSException {
        _currentInode = inode;
        _log.log(Level.FINEST, "current Inode: {0}",  _currentInode.toString() );
    }
    
    public FsInode rootInode() {
        return _rootInode;
    }

    public void rootInode(FsInode inode) {
        _rootInode = inode;
        _log.log(Level.FINEST, "root Inode: {0}", _rootInode.toFullString() );
    }

    public void restoreSavedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_RESTOREFH, "no saved file handle");
        }       
        _currentInode = _savedInode;
        _log.log(Level.FINEST, "restored Inode: {0}",  _currentInode.toString() );
    }

    public FsInode savedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        return _savedInode;
    }    
    
    public void saveCurrentInode() throws ChimeraNFSException {
        if( _currentInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        _savedInode = _currentInode;
        _log.log(Level.FINEST, "saved Inode: {0}", _savedInode.toString() );
    }
    
    public void setSession(NFSv41Session session) {
    	_session = session;
    }
    
    public NFSv41Session getSession() {
    	return _session;
    }
    
    /**
     * the position in compound operation starting from zero
     * @return position
     */
    public int position() {
    	return _position;
    }
    
    public void nexPosition() {
    	_position ++ ;
    }
}
