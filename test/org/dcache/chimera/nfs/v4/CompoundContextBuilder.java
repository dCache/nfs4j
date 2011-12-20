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
package org.dcache.chimera.nfs.v4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.security.auth.Subject;
import org.dcache.chimera.nfs.ExportFile;
import org.dcache.chimera.nfs.vfs.VirtualFileSystem;
import org.dcache.chimera.posix.AclHandler;
import org.dcache.xdr.*;

public class CompoundContextBuilder {

    private final static XdrTransport transport = new XdrTransport() {

        @Override
        public void send(Xdr xdr) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReplyQueue<Integer, RpcReply> getReplyQueue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InetSocketAddress getLocalSocketAddress() {
            return new InetSocketAddress(2049);
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return new InetSocketAddress(7777);
        }

        @Override
        public XdrTransport getPeerTransport() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    private final static RpcAuth auth = new RpcAuth() {

        @Override
        public int type() {
            return RpcAuthType.UNIX;
        }

        @Override
        public RpcAuthVerifier getVerifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Subject getSubject() {
            return new Subject();
        }

        @Override
        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    private RpcCall call = new RpcCall(100003, 4, auth, transport);
    private int minorversion = 0;
    private VirtualFileSystem fs = null;
    private NFSv4StateHandler stateHandler = null;
    private NFSv41DeviceManager deviceManager = null;
    private AclHandler aclHandler = null;
    private NfsIdMapping idMapping = null;
    private ExportFile exportFile = null;
    private int opCount = 0;

    public CompoundContextBuilder withAclHandler(AclHandler aclHandler) {
        this.aclHandler = aclHandler;
        return this;
    }

    public CompoundContextBuilder withCall(RpcCall call) {
        this.call = call;
        return this;
    }

    public CompoundContextBuilder withDeviceManager(NFSv41DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        return this;
    }

    public CompoundContextBuilder withExportFile(ExportFile exportFile) {
        this.exportFile = exportFile;
        return this;
    }

    public CompoundContextBuilder withFs(VirtualFileSystem fs) {
        this.fs = fs;
        return this;
    }

    public CompoundContextBuilder withIdMapping(NfsIdMapping idMapping) {
        this.idMapping = idMapping;
        return this;
    }

    public CompoundContextBuilder withMinorversion(int minorversion) {
        this.minorversion = minorversion;
        return this;
    }

    public CompoundContextBuilder withOpCount(int opCount) {
        this.opCount = opCount;
        return this;
    }

    public CompoundContextBuilder withStateHandler(NFSv4StateHandler stateHandler) {
        this.stateHandler = stateHandler;
        return this;
    }

    public CompoundContext build() {
        return new CompoundContext(
                minorversion,
                fs,
                stateHandler,
                deviceManager,
                aclHandler,
                call,
                idMapping,
                exportFile,
                opCount);
    }
}
