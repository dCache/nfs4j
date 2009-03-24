
/*
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
