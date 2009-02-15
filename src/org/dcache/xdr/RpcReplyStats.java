package org.dcache.xdr;

/**
 * A reply to a call message can take on two forms:
 * The message was either accepted or rejected.
 */
public interface RpcReplyStats {
	public static final int MSG_ACCEPTED = 0;
	public static final int MSG_DENIED = 1;
}
