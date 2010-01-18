package org.dcache.chimera.nfs.v4.acl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dcache.chimera.FsInode;
import org.dcache.chimera.nfs.v4.xdr.nfsace4;

public class AclStore {


	private final static AclStore ACL_STORE = new AclStore();

	private AclStore() {}

	static public AclStore getInstance() {
		return ACL_STORE;
	}

	private final Map<FsInode, nfsace4[] > _store = new ConcurrentHashMap<FsInode, nfsace4[] >();

	public void setAcl(FsInode inode, nfsace4[] ace) {
		_store.put(inode, ace);
	}

	public nfsace4[] getAcl(FsInode inode) {
		return _store.get(inode);
	}

}
