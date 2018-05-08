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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.dcache.nfs.status.InvalException;
import org.dcache.nfs.v4.xdr.bitmap4;
import org.dcache.nfs.v4.xdr.fattr4;
import org.dcache.nfs.v4.xdr.fattr4_acl;
import org.dcache.nfs.v4.xdr.fattr4_fs_locations;
import org.dcache.nfs.v4.xdr.fattr4_lease_time;
import org.dcache.nfs.v4.xdr.fattr4_size;
import org.dcache.nfs.v4.xdr.fattr4_type;
import org.dcache.nfs.v4.xdr.int32_t;
import org.dcache.nfs.v4.xdr.mode4;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.v4.xdr.nfstime4;
import org.dcache.nfs.v4.xdr.settime4;
import org.dcache.nfs.v4.xdr.utf8str_cs;
import org.dcache.oncrpc4j.rpc.OncRpcException;
import org.dcache.oncrpc4j.xdr.XdrAble;
import org.dcache.oncrpc4j.xdr.Xdr;
import org.dcache.oncrpc4j.xdr.XdrDecodingStream;

/**
 * A {@link Map} like container to store values of file attributes.
 *
 * <pre>
 * Example usage:
 *
 *    bitmap4 attrs = ...;
 *    AttributeMap attributeMap = new AttributeMap(attrs);
 *    Optional&lt;unit64_t&gt size = attributeMap.get(nfs4_prot.FATTR4_SIZE);
 *    if(size.isPresent()) {
 *      // use size
 *    }
 * </pre>
 */
public class AttributeMap {
    private final Map<Integer, ? extends XdrAble> _attrs;

    /**
     * Create new {@link AttributeMap} from give attributes {@link bitmap4}.
     * @param attributes bitmap to process
     * @throws OncRpcException
     * @throws IOException
     */
    public AttributeMap(fattr4 attributes) throws OncRpcException, IOException {
	_attrs = asMap(attributes);
    }

    /**
     * Retrieve the {@link Optional} value for a given attribute.
     * @param <T>
     * @param attr to get
     * @return present Optional if value is defined or absent if not.
     */
    public <T extends XdrAble> Optional<T> get(Integer attr) {
	return (Optional<T>) Optional.ofNullable(_attrs.get(attr));
    }

    private static Map<Integer, XdrAble> asMap(fattr4 attributes) throws OncRpcException, IOException {
	Map<Integer, XdrAble> attrs = new HashMap<>();

	if (attributes != null) {
	    int[] mask = attributes.attrmask.value;

            try (Xdr xdr = new Xdr(attributes.attr_vals.value)) {
                xdr.beginDecoding();
                if (mask.length != 0) {
                    int maxAttr = Integer.SIZE * mask.length;
                    for (int i = 0; i < maxAttr; i++) {
                        int bitmapIdx = i / Integer.SIZE;
                        int newmask = mask[bitmapIdx] >> i % Integer.SIZE;
                        if ((newmask & 1L) != 0) {
                            xdr2fattr(attrs, i, xdr);
                        }
                    }
                }
                xdr.endDecoding();
            }
	}
	return attrs;
    }

    private static void xdr2fattr(Map<Integer,XdrAble> attrs, int fattr, XdrDecodingStream xdr) throws OncRpcException, IOException {
	XdrAble attr;
	switch (fattr) {
	    case nfs4_prot.FATTR4_SIZE:
		attr = new fattr4_size();
		break;
	    case nfs4_prot.FATTR4_ACL:
		attr = new fattr4_acl();
		break;
	    case nfs4_prot.FATTR4_ARCHIVE:
		attr = new int32_t();
		break;
	    case nfs4_prot.FATTR4_HIDDEN:
		attr = new int32_t();
		break;
	    case nfs4_prot.FATTR4_MIMETYPE:
		attr = new utf8str_cs();
		break;
	    case nfs4_prot.FATTR4_MODE:
		attr = new mode4();
		break;
	    case nfs4_prot.FATTR4_OWNER:
		attr = new utf8str_cs();
		break;
	    case nfs4_prot.FATTR4_OWNER_GROUP:
		attr = new utf8str_cs();
		break;
	    case nfs4_prot.FATTR4_SYSTEM:
		attr = new int32_t();
		break;
	    case nfs4_prot.FATTR4_TIME_ACCESS_SET:
		attr = new settime4();
		break;
	    case nfs4_prot.FATTR4_TIME_BACKUP:
		attr = new nfstime4();
		break;
	    case nfs4_prot.FATTR4_TIME_CREATE:
		attr = new nfstime4();
		break;
	    case nfs4_prot.FATTR4_TIME_MODIFY_SET:
		attr = new settime4();
		break;
	    case nfs4_prot.FATTR4_FS_LOCATIONS:
		attr = new fattr4_fs_locations();
		break;
	    case nfs4_prot.FATTR4_TYPE:
		attr = new fattr4_type();
		break;
	    case nfs4_prot.FATTR4_LEASE_TIME:
		attr = new fattr4_lease_time();
		break;
	    default:
		throw new InvalException("invalid attribute: " + OperationGETATTR.attrMask2String(fattr));
	}
	attr.xdrDecode(xdr);
	attrs.put(fattr, attr);
    }

}
