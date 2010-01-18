package org.dcache.chimera.nfs.v4;

import java.util.List;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.ChimeraNFSException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.posix.UnixUser;
import org.dcache.xdr.RpcCall;


public class CompoundContext {

    
    private static final Logger _log = Logger.getLogger(CompoundContext.class.getName());
    
    private FsInode _rootInode = null;
    private FsInode _currentInode = null;
    private FsInode _savedInode = null;

    private final int _minorversion;
    
    private NFSv41Session _session = null;
    private final List<nfs_resop4> _processedOps;

    private final FileSystemProvider _fs;
    private final RpcCall _callInfo;
    private final UnixUser _user;
    private final ExportFile _exportFile;    

    /**
     * Create context of COUMPOUND request.
     *
     * @param processedOps @{link List} wnere results of processed operations are stored.
     * @param minorversion NFSv4 minor version number.
     * @param fs backend file-system interface
     * @param call RPC call
     * @param exportFile list of servers exports.
     */
    public CompoundContext(List<nfs_resop4> processedOps, int minorversion, FileSystemProvider fs,
            RpcCall call, ExportFile exportFile) {
        _processedOps = processedOps;
    	_minorversion = minorversion;
        _fs = fs;
        _callInfo = call;
        _exportFile = exportFile;
        _user = HimeraNFS4Utils.remoteUser(_callInfo, _exportFile);
    }

    public RpcCall getRpcCall() {
        return _callInfo;
    }
    public UnixUser getUser() {
        return _user;
    }

    public FileSystemProvider getFs() {
        return _fs;
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

    /**
     * Set NFSv4.1 session of current request.
     * @param session
     */
    public void setSession(NFSv41Session session) {
    	_session = session;
    }

    /**
     * Get {@link NFSv41Session} used by current request.
     * @return current session
     */
    public NFSv41Session getSession() {
    	return _session;
    }

    /**
     * Get list of currently processed operations.
     * @return list of operations.
     */
    public List<nfs_resop4> processedOperations() {
        return _processedOps;
    }
}
