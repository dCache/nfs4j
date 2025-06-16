/*
 * Copyright (c) 2016 - 2020 Deutsches Elektronen-Synchroton,
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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.status.BadXdrException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.ff.ff_data_server4;
import org.dcache.nfs.v4.ff.ff_device_addr4;
import org.dcache.nfs.v4.ff.ff_device_versions4;
import org.dcache.nfs.v4.ff.ff_layout4;
import org.dcache.nfs.v4.ff.ff_layoutreturn4;
import org.dcache.nfs.v4.ff.ff_mirror4;
import org.dcache.nfs.v4.ff.flex_files_prot;
import org.dcache.nfs.v4.xdr.device_addr4;
import org.dcache.nfs.v4.xdr.deviceid4;
import org.dcache.nfs.v4.xdr.fattr4_owner;
import org.dcache.nfs.v4.xdr.fattr4_owner_group;
import org.dcache.nfs.v4.xdr.layout_content4;
import org.dcache.nfs.v4.xdr.layouttype4;
import org.dcache.nfs.v4.xdr.length4;
import org.dcache.nfs.v4.xdr.multipath_list4;
import org.dcache.nfs.v4.xdr.netaddr4;
import org.dcache.nfs.v4.xdr.nfs_fh4;
import org.dcache.nfs.v4.xdr.stateid4;
import org.dcache.nfs.v4.xdr.uint32_t;
import org.dcache.nfs.v4.xdr.utf8str_mixed;
import org.dcache.oncrpc4j.xdr.Xdr;

/**
 * layout driver for Flexible File layout type as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8435.txt">rfc8435</a>
 */
public class FlexFileLayoutDriver implements LayoutDriver {

    /**
     * The {@code nfsVersion} and {@code nfsMinorVersion} represent the NFS protocol to be used to access the storage
     * device.
     */
    private final int nfsVersion;
    private final int nfsMinorVersion;

    /**
     * User principal, which must be used by client when RPC packet sent to data server.
     */
    private final fattr4_owner userPrincipal;

    /**
     * Group principal, which must be used by client when RPC packet sent to data server.
     */
    private final fattr4_owner_group groupPrincipal;

    /**
     * Consumer which accepts data provided on layout return.
     */
    private final BiConsumer<CompoundContext, ff_layoutreturn4> layoutReturnConsumer;

    /**
     * Layout flags, like no IO through MDS. See rfc8435#5.1
     */
    private final uint32_t layoutFlags;

    /**
     * The data transfer buffer size used for READ and WRITE operations.
     *
     * REVISIT: for now, we assume that all data server prefer the same rsize/wsize.
     */
    private final uint32_t ioBufferSize;

    /**
     * Create new FlexFile layout driver with. The @code nfsVersion} and {@code nfsMinorVersion} represent the protocol
     * to be used to access the storage device. If client uses AUTH_SYS, then provided {@code userPrincipal} and
     * {@code groupPrincipal} must be used for client - data server communication.
     *
     * @param nfsVersion nfs version to use
     * @param nfsMinorVersion nfs minor version to use.
     * @param flags layout flags.
     * @param ioBufferSize the data transfer buffer size used for READ or WRITE.
     * @param userPrincipal user principal to be used by client
     * @param groupPrincipal group principal to be used by client
     * @param layoutReturnConsumer consumer which accepts data provided on layout return.
     */
    public FlexFileLayoutDriver(int nfsVersion, int nfsMinorVersion, int flags, int ioBufferSize,
            utf8str_mixed userPrincipal, utf8str_mixed groupPrincipal,
            BiConsumer<CompoundContext, ff_layoutreturn4> layoutReturnConsumer) {

        checkArgument((flags & ~flex_files_prot.FF_FLAGS_MASK) == 0, "Invalid flex files layout flag");
        this.nfsVersion = nfsVersion;
        this.nfsMinorVersion = nfsMinorVersion;
        this.userPrincipal = new fattr4_owner(userPrincipal);
        this.groupPrincipal = new fattr4_owner_group(groupPrincipal);
        this.layoutReturnConsumer = layoutReturnConsumer;
        this.ioBufferSize = new uint32_t(ioBufferSize);
        this.layoutFlags = new uint32_t(flags);
    }

    @Override
    public layouttype4 getLayoutType() {
        return layouttype4.LAYOUT4_FLEX_FILES;
    }

    @Override
    public device_addr4 getDeviceAddress(InetSocketAddress... deviceAddress) throws ChimeraNFSException {
        ff_device_addr4 flexfile_type = new ff_device_addr4();

        flexfile_type.ffda_versions = new ff_device_versions4[1];
        flexfile_type.ffda_versions[0] = new ff_device_versions4();
        flexfile_type.ffda_versions[0].ffdv_version = new uint32_t(nfsVersion);
        flexfile_type.ffda_versions[0].ffdv_minorversion = new uint32_t(nfsMinorVersion);
        flexfile_type.ffda_versions[0].ffdv_rsize = ioBufferSize;
        flexfile_type.ffda_versions[0].ffdv_wsize = ioBufferSize;
        flexfile_type.ffda_versions[0].ffdv_tightly_coupled = true;

        flexfile_type.ffda_netaddrs = new multipath_list4();
        flexfile_type.ffda_netaddrs.value = new netaddr4[deviceAddress.length];
        for (int i = 0; i < deviceAddress.length; i++) {
            flexfile_type.ffda_netaddrs.value[i] = new netaddr4(deviceAddress[i]);
        }

        try (Xdr xdr = new Xdr(128)) {
            xdr.beginEncoding();
            flexfile_type.xdrEncode(xdr);
            xdr.endEncoding();

            device_addr4 addr = new device_addr4();
            addr.da_layout_type = layouttype4.LAYOUT4_FLEX_FILES.getValue();
            addr.da_addr_body = xdr.getBytes();

            return addr;
        } catch (IOException e) {
            /* forced by interface, should never happen. */
            throw new RuntimeException("Unexpected IOException:" + e.getMessage(), e);
        }

    }

    @Override
    public layout_content4 getLayoutContent(stateid4 stateid, int stripeSize, nfs_fh4 fh, deviceid4... deviceids)
            throws ChimeraNFSException {

        checkArgument(deviceids.length > 0, "Layout driver supports need at least one (1) device.");

        ff_layout4 layout = new ff_layout4();

        layout.ffl_stripe_unit = new length4(0);
        layout.ffl_mirrors = createMirrors(deviceids, 0, stateid, fh);
        layout.ffl_flags4 = layoutFlags;
        layout.ffl_stats_collect_hint = new uint32_t(0);

        try (Xdr xdr = new Xdr(512)) {
            xdr.beginEncoding();
            layout.xdrEncode(xdr);
            xdr.endEncoding();

            layout_content4 content = new layout_content4();
            content.loc_type = layouttype4.LAYOUT4_FLEX_FILES.getValue();
            content.loc_body = xdr.getBytes();

            return content;
        } catch (IOException e) {
            throw new ServerFaultException("failed to encode layout body", e);
        }
    }

    private ff_data_server4 createDataserver(deviceid4 deviceid,
            int efficiency, stateid4 stateid, nfs_fh4 fileHandle) {
        ff_data_server4 ds = new ff_data_server4();
        ds.ffds_deviceid = deviceid;
        ds.ffds_efficiency = new uint32_t(efficiency);
        ds.ffds_stateid = stateid;
        ds.ffds_fh_vers = new nfs_fh4[] {fileHandle};
        ds.ffds_user = userPrincipal;
        ds.ffds_group = groupPrincipal;
        return ds;
    }

    private ff_mirror4[] createMirrors(deviceid4[] deviceids, int efficiency, stateid4 stateid, nfs_fh4 fileHandle) {
        ff_mirror4[] mirrors = new ff_mirror4[deviceids.length];
        for (int i = 0; i < deviceids.length; i++) {
            mirrors[i] = new ff_mirror4();
            mirrors[i].ffm_data_servers = new ff_data_server4[1];
            mirrors[i].ffm_data_servers[0] = createDataserver(deviceids[i], efficiency, stateid, fileHandle);
        }
        return mirrors;
    }

    /**
     * Consumes flexfiles specific data provided on layout return. The must be xdr encoded ff_layoutreturn4 object.
     *
     * See: https://www.rfc-editor.org/rfc/rfc8435.txt#9
     *
     * @throws org.dcache.nfs.status.BadXdrException if provided data cant be decoded.
     */
    @Override
    public void acceptLayoutReturnData(CompoundContext context, byte[] data) throws BadXdrException {
        try {

            ff_layoutreturn4 lr;
            try (Xdr xdr = new Xdr(data)) {
                xdr.beginDecoding();
                lr = new ff_layoutreturn4(xdr);
                xdr.endDecoding();
            }

            layoutReturnConsumer.accept(context, lr);
        } catch (IOException e) {
            throw new BadXdrException("invalid data", e);
        }
    }
}
