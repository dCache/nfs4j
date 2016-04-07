/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.xdr.RpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.dcache.auth.UidPrincipal;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.NoFileHandleException;
import org.dcache.nfs.status.RestoreFhException;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.v4.xdr.server_owner4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint64_t;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.xdr.RpcAuthType;


public class CompoundContext {

    private static final Logger _log = LoggerFactory.getLogger(CompoundContext.class);

    private static final Principal NO_PRINCIPAL = new Principal() {

            private final String _name = "";

            @Override
            public String getName() {
                return _name;
            }

            @Override
            public int hashCode() {
                return getName().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                return obj != null && obj.getClass().isInstance(this);
            }
        };

    private Inode _rootInode = null;
    private Inode _currentInode = null;
    private Inode _savedInode = null;

    private final int _minorversion;

    private NFSv41Session _session = null;

    private final VirtualFileSystem _fs;
    private final RpcCall _callInfo;
    private final Subject _subject;
    private final ExportFile _exportFile;
    private final NFSv41DeviceManager _deviceManager;
    private final NFSv4StateHandler _stateHandler;
    private int _slotId;
    private boolean _cacheThis;
    private final int _totalOperationsCount;
    private int _currentOpPosition = -1;
    private stateid4 _currentStateid = null;
    private stateid4 _savedStateid = null;
    private final Principal _principal;
    private final LockManager _nlm;

    /**
     * Create context of COUMPOUND request.
     *
     * @param processedOps @{link List} where results of processed operations are stored.
     * @param minorversion NFSv4 minor version number.
     * @param fs backend file-system interface
     * @param call RPC call
     * @param exportFile list of servers exports.
     */
    public CompoundContext(int minorversion, VirtualFileSystem fs,
            NFSv4StateHandler stateHandler,
            LockManager nlm,
            NFSv41DeviceManager deviceManager, RpcCall call,
            ExportFile exportFile, int opCount) {
        _minorversion = minorversion;
        _fs = fs;
        _deviceManager = deviceManager;
        _callInfo = call;
        _exportFile = exportFile;
        _subject = call.getCredential().getSubject();
        _stateHandler = stateHandler;
        _totalOperationsCount = opCount;
        _principal = principalOf(call);
        _nlm = nlm;
    }

    public RpcCall getRpcCall() {
        return _callInfo;
    }

    public Subject getSubject() {
        return _subject;
    }

    public VirtualFileSystem getFs() {
        return _fs;
    }

    public NFSv41DeviceManager getDeviceManager() {
        return _deviceManager;
    }

    public LockManager getLm() {
        return _nlm;
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
    public Inode currentInode() throws ChimeraNFSException {
        if( _currentInode == null ) {
            throw new NoFileHandleException("no file handle");
        }
        return _currentInode;
    }

    /**
     * Set current file handle.
     *
     * @param inode
     * @throws ChimeraNFSException
     */
    public void currentInode(Inode inode) throws ChimeraNFSException {
        _currentInode = inode;
        _log.debug("current Inode: {}",  _currentInode );
    }

    /**
     * Consume current file handle.
     *
     * @throws ChimeraNFSException
     */
    public void clearCurrentInode() throws ChimeraNFSException {
        _currentInode = null;
    }

    public Inode rootInode() {
        return _rootInode;
    }

    public void rootInode(Inode inode) {
        _rootInode = inode;
        _log.debug("root Inode: {}", _rootInode );
    }

    /**
     * Set the current file handle to the value in the saved file handle.
     * If there is no saved filehandle then the server will return the
     * error NFS4ERR_RESTOREFH.
     * @throws ChimeraNFSException
     */
    public void restoreSavedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new RestoreFhException("no saved file handle");
        }
        _currentInode = _savedInode;
        _currentStateid = _savedStateid;
        _log.debug("restored Inode: {}",  _currentInode );
    }

    public Inode savedInode() throws ChimeraNFSException {
        if( _savedInode == null ) {
            throw new NoFileHandleException("no file handle");
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
            throw new NoFileHandleException("no file handle");
        }
        _savedInode = _currentInode;
        _savedStateid = _currentStateid;
        _log.debug("saved Inode: {}", _savedInode );
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

    public NFSv4StateHandler getStateHandler() {
        return _stateHandler;
    }

    public int getSlotId() {
        return _slotId;
    }

    public void setSlotId(int slotId) {
        _slotId = slotId;
    }

    public boolean cacheThis() {
        return _cacheThis;
    }

    public void setCacheThis(boolean cacheThis) {
        _cacheThis = cacheThis;
    }

    private List<nfs_resop4> _cache;

    public List<nfs_resop4> getCache() {
        return _cache;
    }

    public void setCache(List<nfs_resop4> cache) {
        _cache = cache;
    }

    public int getOperationPosition() {
        return _currentOpPosition;
    }

    public int getTotalOperationCount() {
        return _totalOperationsCount;
    }

    public void nextOperation() {
        assert _currentOpPosition < _totalOperationsCount;
        _currentOpPosition ++;
    }

    public stateid4 currentStateid() throws ChimeraNFSException {
        if(_currentStateid == null)
            throw new BadStateidException("no current stateid");
        return _currentStateid;
    }

    public void currentStateid(stateid4 currentStateid) {
        _currentStateid = currentStateid;
    }

    public ExportFile getExportFile() {
        return _exportFile;
    }

    public ServerIdProvider getServerIdProvider() {
        // FIXME: bond to file system and DS
        return new ServerIdProvider() {

            @Override
            public server_owner4 getOwner() {
                server_owner4 owner = new server_owner4();
                owner.so_minor_id = new uint64_t(0);
                owner.so_major_id = _callInfo.
                        getTransport().
                        getLocalSocketAddress().
                        getAddress().
                        getAddress();
                return owner;
            }

            @Override
            public byte[] getScope() {
                return "".getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    public Principal getPrincipal() {
        return _principal;
    }

    private Principal principalOf(final RpcCall call) {

        Class<? extends Principal> type;
        if(call.getCredential().type() == RpcAuthType.RPCGSS_SEC) {
            type = KerberosPrincipal.class;
        } else {
            type = UidPrincipal.class;
        }

        Set<? extends Principal> principals =
                call.getCredential().getSubject().getPrincipals(type);
        return principals.isEmpty() ? NO_PRINCIPAL : principals.iterator().next();
    }

    /**
     * Returns the address of local endpoint which have received the requests.
     * @return a socketAddress representing the local endpoint.
     */
    public InetSocketAddress getRemoteSocketAddress() {
        return getRpcCall().getTransport().getRemoteSocketAddress();
    }

    /**
     * Returns the address of remote endpoint which has sent the requests.
     *
     * @return a socketAddress representing the remote endpoint.
     */
    public InetSocketAddress getLocalSocketAddress() {
        return getRpcCall().getTransport().getLocalSocketAddress();
    }
}
