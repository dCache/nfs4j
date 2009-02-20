package org.dcache.xdr;

/**
 *
 * Why authentication failed.
 */
public interface RpcAuthStat {
    /*
     * failed on remote end
     */

    public static final int AUTH_OK = 0; /* success                          */
    public static final int AUTH_BADCRED = 1; /* bad credential (seal broken)     */
    public static final int AUTH_REJECTEDCRED = 2; /* client must begin new session    */
    public static final int AUTH_BADVERF = 3; /* bad verifier (seal broken)       */
    public static final int AUTH_REJECTEDVERF = 4; /* verifier expired or replayed     */
    public static final int AUTH_TOOWEAK = 5; /* rejected for security reasons    */
    /*
     * failed locally
     */
    public static final int AUTH_INVALIDRESP = 6; /* bogus response verifier          */
    public static final int AUTH_FAILED = 7; /* reason unknown                   */
}
