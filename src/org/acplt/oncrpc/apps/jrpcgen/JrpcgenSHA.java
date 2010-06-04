/* SHA.java -- Class implementing the SHA-1 algorithm as specified in [1].
Copyright (C) 1999, 2000 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenSHA.java,v 1.1.1.1 2003/08/13 12:03:47 haraldalbrecht Exp $
 *
 * The original file gnu.java.security.provider.SHA.java has been
 * renamed to reflect the many chances made to it. While the processing
 * kernel has not been changed, the overall interface has. Especially
 * some methods have been added which can hash several kinds of data
 * types, as needed by the jrpcgen protocol compiler.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:47 $ $State: Exp $ $Locker:  $
 * @author GNU Classpath
 */
package org.acplt.oncrpc.apps.jrpcgen;

/**
 * This class implements the SHA-1 algorithm as described in
 * "Federal Information Processing Standards Publication 180-1:
 * Specifications for the Secure Hash Standard. April 17, 1995."
 */
public class JrpcgenSHA {

    /**
     * Create a new SHA-1 hashing object.
     */
    public JrpcgenSHA() {
        reset();
    }

    /**
     * Update the hash using a single byte (8 bits).
     *
     * @param b Byte to hash.
     */
    public void update(byte b) {
        int i = (int) bytecount % 64;
        int shift = (3 - i % 4) * 8;
        int idx = i / 4;

        // if you could index ints, this would be: W[idx][shift/8] = b
        W[idx] = (W[idx] & ~(0xff << shift)) | ((b & 0xff) << shift);

        // if we've filled up a block, then process it
        if ((++bytecount) % 64 == 0) {
            process();
        }
    }

    /**
     * Update the hash using a short integer (16 bits).
     *
     * @param s Short integer to hash.
     */
    public void update(short s) {
        update((byte) s);
        update((byte) (s >>> 8));
    }

    /**
     * Update the hash using an integer (32 bits).
     *
     * @param i Integer to hash.
     */
    public void update(int i) {
        update((byte) i);
        update((byte) (i >>> 8));
        update((byte) (i >>> 16));
        update((byte) (i >>> 24));
    }

    /**
     * Update the hash using a string.
     *
     * @param s String to hash.
     */
    public void update(String s) {
        int len = s.length();
        for (int idx = 0; idx < len; ++idx) {
            update((short) s.charAt(idx));
        }
    }

    /**
     * Reset the hashing engine to start hashing another set of innocent
     * bytes.
     */
    public void reset() {
        bytecount = 0;
        // magic numbers from [1] p. 10.
        H0 = 0x67452301;
        H1 = 0xefcdab89;
        H2 = 0x98badcfe;
        H3 = 0x10325476;
        H4 = 0xc3d2e1f0;
    }

    /**
     * Retrieve the digest (that is, informally spoken, the "hash value").
     *
     * @return digest as a series of 20 bytes (80 bits).
     */
    public byte[] getDigest() {
        long bitcount = bytecount * 8;
        update((byte) 0x80); // 10000000 in binary; the start of the padding

        // add the rest of the padding to fill this block out, but leave 8
        // bytes to put in the original bytecount
        while ((int) bytecount % 64 != 56) {
            update((byte) 0);
        }

        // add the length of the original, unpadded block to the end of
        // the padding
        W[14] = (int) (bitcount >>> 32);
        W[15] = (int) bitcount;
        bytecount += 8;

        // digest the fully padded block
        process();

        byte[] result = new byte[]{
            (byte) (H0 >>> 24), (byte) (H0 >>> 16),
            (byte) (H0 >>> 8), (byte) H0,
            (byte) (H1 >>> 24), (byte) (H1 >>> 16),
            (byte) (H1 >>> 8), (byte) H1,
            (byte) (H2 >>> 24), (byte) (H2 >>> 16),
            (byte) (H2 >>> 8), (byte) H2,
            (byte) (H3 >>> 24), (byte) (H3 >>> 16),
            (byte) (H3 >>> 8), (byte) H3,
            (byte) (H4 >>> 24), (byte) (H4 >>> 16),
            (byte) (H4 >>> 8), (byte) H4
        };

        reset();
        return result;
    }

    /**
     * Return first 64 bits of hash digest for use as a serialization
     * UID, etc.
     *
     * @return hash digest with only 64 bit size.
     */
    public long getHash() {
        byte[] hash = getDigest();
        return (((long) hash[0]) & 0xFF)
                + ((((long) hash[1]) & 0xFF) << 8)
                + ((((long) hash[2]) & 0xFF) << 16)
                + ((((long) hash[3]) & 0xFF) << 24)
                + ((((long) hash[4]) & 0xFF) << 32)
                + ((((long) hash[5]) & 0xFF) << 40)
                + ((((long) hash[6]) & 0xFF) << 48)
                + ((((long) hash[7]) & 0xFF) << 56);
    }

    /**
     * Process a single block. This is pretty much copied verbatim from
     * "Federal Information Processing Standards Publication 180-1:
     * Specifications for the Secure Hash Standard. April 17, 1995.",
     * pp. 9, 10.
     */
    private void process() {
        for (int t = 16; t < 80; ++t) {
            int Wt = W[t - 3] ^ W[t - 8] ^ W[t - 14] ^ W[t - 16];
            W[t] = Wt << 1 | Wt >>> 31;
        }

        int A = H0;
        int B = H1;
        int C = H2;
        int D = H3;
        int E = H4;

        for (int t = 0; t < 20; ++t) {
            int TEMP = (A << 5 | A >>> 27) // S^5(A)
                    + ((B & C) | (~B & D)) // f_t(B,C,D)
                    + E + W[t]
                    + 0x5a827999;                // K_t

            E = D;
            D = C;
            C = B << 30 | B >>> 2;                  // S^30(B)
            B = A;
            A = TEMP;
        }

        for (int t = 20; t < 40; ++t) {
            int TEMP = (A << 5 | A >>> 27) // S^5(A)
                    + (B ^ C ^ D) // f_t(B,C,D)
                    + E + W[t]
                    + 0x6ed9eba1;                // K_t

            E = D;
            D = C;
            C = B << 30 | B >>> 2;                  // S^30(B)
            B = A;
            A = TEMP;
        }

        for (int t = 40; t < 60; ++t) {
            int TEMP = (A << 5 | A >>> 27) // S^5(A)
                    + (B & C | B & D | C & D) // f_t(B,C,D)
                    + E + W[t]
                    + 0x8f1bbcdc;                // K_t

            E = D;
            D = C;
            C = B << 30 | B >>> 2;                  // S^30(B)
            B = A;
            A = TEMP;
        }

        for (int t = 60; t < 80; ++t) {
            int TEMP = (A << 5 | A >>> 27) // S^5(A)
                    + (B ^ C ^ D) // f_t(B,C,D)
                    + E + W[t]
                    + 0xca62c1d6;                // K_t

            E = D;
            D = C;
            C = B << 30 | B >>> 2;                  // S^30(B)
            B = A;
            A = TEMP;
        }

        H0 += A;
        H1 += B;
        H2 += C;
        H3 += D;
        H4 += E;

        // Reset W by clearing it.
        for (int t = 0; t < 80; ++t) {
            W[t] = 0;
        }
    }
    /**
     * Work buffer for calculating the hash.
     */
    private final int W[] = new int[80];
    private long bytecount;
    private int H0;
    private int H1;
    private int H2;
    private int H3;
    private int H4;
}
// End of JrpcgenSHA.java

