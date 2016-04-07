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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import javax.security.auth.Subject;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v4.nlm.LockManager;
import org.dcache.nfs.v4.nlm.SimpleLm;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.xdr.*;

public class CompoundContextBuilder {

    private final LockManager lm = new SimpleLm();
    private final XdrTransport transport = new XdrTransport() {

        private final Random rnd = new Random();
        private final InetSocketAddress local = new InetSocketAddress(2049);
        private final InetSocketAddress remote = new InetSocketAddress(rnd.nextInt(65535));

        @Override
        public void send(Xdr xdr) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReplyQueue getReplyQueue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InetSocketAddress getLocalSocketAddress() {
            return local;
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return remote;
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
    private ExportFile exportFile = null;
    private int opCount = 0;

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
                lm,
                deviceManager,
                call,
                exportFile,
                opCount);
    }

}
