package org.dcache.chimera.nfs.v4;

import org.dcache.chimera.FileSystemProvider;
import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.v4.xdr.nfs_resop4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.chimera.posix.UnixUser;
import org.dcache.xdr.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CompoundContext {

    private static final Logger _log = LoggerFactory.getLogger(CompoundContext.class);

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
    private final NFSv41DeviceManager _deviceManager;
    private final AclHandler _aclHandler;

    /**
     * Create context of COUMPOUND request.
     *
     * @param processedOps @{link List} where results of processed operations are stored.
     * @param minorversion NFSv4 minor version number.
     * @param fs backend file-system interface
     * @param call RPC call
     * @param exportFile list of servers exports.
     */
    public CompoundContext(List<nfs_resop4> processedOps, int minorversion, FileSystemProvider fs,
            NFSv41DeviceManager deviceManager, AclHandler aclHandler, RpcCall call, ExportFile exportFile) {
        _processedOps = processedOps;
        _minorversion = minorversion;
        _fs = fs;
        _deviceManager = deviceManager;
        _aclHandler = aclHandler;
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

    public NFSv41DeviceManager getDeviceManager() {
        return _deviceManager;
    }

    public AclHandler getAclHandler() {
        return _aclHandler;
    }
    /**
     * Get NFSv4 minor version number. The version number os provided by client
     * for each compound.
     * @return version number.
     */
    public int getMinorversion() {
        return _minorversion;
    }

    /**
     * Current file handle is a server side variable passed from one operation
     * to other inside a compound.
     *
     * @return file handle
     * @throws ChimeraNFSException
     */
    public FsInode currentInode() throws ChimeraNFSException {
        if( _currentInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        return _currentInode;
    }

    /**
     * Set current file handle.
     *
     * @param inode
     * @throws ChimeraNFSException
     */
    public void currentInode(FsInode inode) throws ChimeraNFSException {
        _currentInode = inode;
        _log.debug("current Inode: {}",  _currentInode.toString() );
    }
    
    public FsInode rootInode() {
        return _rootInode;
    }

    public void rootInode(FsInode inode) {
        _rootInode = inode;
        _log.debug("root Inode: {}", _rootInode.toFullString() );
    }

    /**
     * Set the current file handle to the value in the saved file handle.
     * If there is no saved filehandle then the server will return the
     * error NFS4ERR_RESTOREFH.
     * @throws ChimeraNFSException
     */
    public void restoreSavedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_RESTOREFH, "no saved file handle");
        }
        _currentInode = _savedInode;
        _log.debug("restored Inode: {}",  _currentInode.toString() );
    }

    public FsInode savedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        return _savedInode;
    }

    /**
     * Save the current filehandle. If a previous filehandle was saved then it
     * is no longer accessible. The saved filehandle can be restored as
     * the current filehandle with the RESTOREFH operator.
     * @throws ChimeraNFSException
     */
    public void saveCurrentInode() throws ChimeraNFSException {
        if( _currentInode == null ) {
            throw new ChimeraNFSException(nfsstat4.NFS4ERR_NOFILEHANDLE, "no file handle");
        }
        _savedInode = _currentInode;
        _log.debug("saved Inode: {}", _savedInode.toString() );
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
