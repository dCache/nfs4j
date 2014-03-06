/*
 * Copyright (c) 2009 - 2014 Deutsches Elektronen-Synchroton,
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

import org.dcache.nfs.nfsstat;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.dcache.nfs.ChimeraNFSException;

class NameFilter {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /* utility calls */
    private NameFilter(){}

    /**
     * Validate name and convert into UTB8 {@link String}.
     *
     * @param bytes to convert
     * @return string
     * @throws ChimeraNFSException if provided {@code bytes} are not a UTF8 encoded.
     */
    public static String convert(byte[] bytes) throws ChimeraNFSException {

        if (bytes.length == 0) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "zero-length name");
        }

        if (bytes.length > NFSv4Defaults.NFS4_MAXFILENAME) {
            throw new ChimeraNFSException(nfsstat.NFSERR_NAMETOOLONG, "file name too long");
        }

        try {
            CharsetDecoder cd = UTF8.newDecoder();
            cd.onMalformedInput(CodingErrorAction.REPORT);
            cd.onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer uniBuf = ByteBuffer.wrap(bytes);
            CharBuffer charBuf = cd.decode(uniBuf);

            /*
             * Java uses internally UTF-16 and, as a result, encode accepts non valid
             * surrogate bytes as well. For NFs such sequences are invalid.
             *
             * See: http://www.oracle.com/technetwork/articles/javase/supplementary-142654.html#Modified_UTF-8
             *
             */
            for (char c : charBuf.array()) {
                if (Character.isSurrogate(c)) {
                    throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "invalid utf8 name");
                }
            }

            String name = new String(charBuf.array(), 0, charBuf.length());

            if (name.length() == 0) {
                throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "bad path name");
            }

            if (name.length() > NFSv4Defaults.NFS4_MAXFILENAME) {
                throw new ChimeraNFSException(nfsstat.NFSERR_NAMETOOLONG, "name too long");
            }

            if (name.equals(".") || name.equals("..")) {
                throw new ChimeraNFSException(nfsstat.NFSERR_BADNAME, "bad name '.' or '..'");
            }

            if (name.indexOf('/') != -1) {
                throw new ChimeraNFSException(nfsstat.NFSERR_BADNAME, "name with slash '/'");
            }

            return name;
        } catch (CharacterCodingException e) {
            throw new ChimeraNFSException(nfsstat.NFSERR_INVAL, "invalid utf8 name");
        }
    }
}
