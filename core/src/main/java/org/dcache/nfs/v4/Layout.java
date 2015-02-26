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

import java.io.IOException;
import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.layout4;
import org.dcache.nfs.v4.xdr.layout_content4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.nfs.v4.xdr.offset4;
import org.dcache.xdr.XdrBuffer;
import org.glassfish.grizzly.Buffer;

/**
 * A Layout defines how a file's data is organized on one or more storage devices.
 * There are three layout types are defined in rfc 5661:
 * <pre>
 *    NFSV4.1 FILE (rfc 5661)
 *    BLOCK VOLUME (rfc 5663)
 *    ODS OBJECT (rfc 5664)
 * </pre>
 *
 * A Layout is expressed as an array of layout segments. The elements of the array
 * MUST be sorted in ascending order of the value of the offset field of each element.
 *
 * <pre>
 *   segment1: offset 0, len 256K
 *   segment2: offset 256, len 256K
 *   ....
 *   segmentN: offset M, len 256K
 * </pre>
 *
 * There MUST be no gaps or overlaps in the range between two successive elements.
 *
 * There are a two ways to stripe a file:  across different devices or/and across
 * multiple data servers defined as a single device. Striping within a single
 * device is the one which supported by all clients (as of kernel 2.6.34 linux
 * client does not support multiple layout segments and uses the first entry one only).
 */
public class Layout {

    private final boolean _returnOnClose;
    private final stateid4 _stateid;
    private final layout4[] _layoutSegments;

    public Layout(boolean returnOnClose, stateid4 stateid, layout4[] layoutSegments) {
        _returnOnClose = returnOnClose;
        _stateid = stateid;
        _layoutSegments = layoutSegments;
    }

    /**
     * Should the client return the layout prior close.
     * @return  <code>true</code> if a client should returns the layout prior close.
     */
    public boolean returnOnClose() {
        return _returnOnClose;
    }

    /**
     * Get stateid associated with layout.
     * @return stateid
     */
    public stateid4 getStateid() {
        return _stateid;
    }

    /**
     * Get array of layout segments.
     * @return layout segments.
     */
    public layout4[] getLayoutSegments() {
        return _layoutSegments;
    }

    private static layout_content4 getSegmentContent(deviceid4 deviceid, int stripeSize, nfs_fh4 fh) throws ChimeraNFSException {

        nfsv4_1_file_layout4 layout = Layouts.newNfsFileLayout(deviceid, stripeSize, fh);

        XdrBuffer xdr = new XdrBuffer(512);
        xdr.beginEncoding();

        try {
            layout.xdrEncode(xdr);
        } catch (IOException e) {
            throw new ServerFaultException("failed to encode layout body");
        }
        xdr.endEncoding();

        Buffer xdrBody = xdr.asBuffer();
        byte[] body = new byte[xdrBody.remaining()];
        xdrBody.get(body);

        layout_content4 content = new layout_content4();
        content.loc_type = layouttype4.LAYOUT4_NFSV4_1_FILES;
        content.loc_body = body;

        return content;
    }

    /**
     * Create a layout segment for a given io mode, offset and length.
     * The special value of length NFS4_UINT64_MAX corresponds to up to EOF.
     * The valid values for <code>iomode</code> are LAYOUTIOMODE4_READ
     * or LAYOUTIOMODE4_RW.
     *
     * @param deviceid on which segment is available.
     * @param stripeSize preferred stripe unit size
     * @param fh file handle to be used on data servers.
     * @param iomode io mode for the segment.
     * @param offset where segment starts.
     * @param length segment length.
     * @return layout segment
     * @throws IOException
     */
    public static layout4 getLayoutSegment(deviceid4 deviceid, int stripeSize, nfs_fh4 fh, int iomode, long offset, long length)
            throws IOException {

        layout4 segment = new layout4();
        segment.lo_offset = new offset4(offset);
        segment.lo_length = new length4(length);
        segment.lo_iomode = iomode;
        segment.lo_content = new layout_content4();
        segment.lo_content = getSegmentContent(deviceid, stripeSize, fh);

        return segment;
    }
}
