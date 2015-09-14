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

import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.layout4;

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
}
