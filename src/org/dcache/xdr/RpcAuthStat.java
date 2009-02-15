package org.dcache.xdr;

/**
 *
 * Why authentication failed.
 */
public enum RpcAuthStat {
          AUTH_BADCRED      ,  /* bad credentials (seal broken) */
          AUTH_REJECTEDCRED ,  /* client must begin new session */
          AUTH_BADVERF      ,  /* bad verifier (seal broken)    */
          AUTH_REJECTEDVERF ,  /* verifier expired or replayed  */
          AUTH_TOOWEAK         /* rejected for security reasons */

}
