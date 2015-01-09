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
package org.dcache.nfs;

import com.google.common.base.Splitter;
import java.net.InetAddress;

public class FsExport {

    /**
     * default UID for anonymous access.
     */
    public final static int DEFAULT_ANON_UID = 65534;
    /**
     * default GID for anonymous access.
     */
    public final static int DEFAULT_ANON_GID = 65534;

    public enum Root {
        TRUSTED, NOTTRUSTED
    }

    public enum IO {
        RW, RO
    }

    public enum Sec {
        NONE,
        SYS,
        KRB5,
        KRB5I,
        KRB5P
    }

    private final String _path;
    private final String _client;
    private final Root _isTrusted;
    private final IO _rw;
    private final boolean _withAcl;
    private final Sec _sec;
    private final boolean _allSquash;
    private final int _anonUid;
    private final int _anonGid;
    private final boolean _withDcap;
    private final int _index;

    /**
     * NFS clients may be specified in a number of ways:<br>
     * <p>
     *
     * <b>single host</b>
     * <p>
     * This is the most common format. You may specify a host either by an
     * abbreviated name recognized be the resolver, the fully qualified domain
     * name, or an IP address.
     * <p>
     *
     * <b>wildcards</b>
     * <p>
     * Machine names may contain the wildcard characters * and ?. This can be
     * used to make the exports file more compact; for instance, .cs.foo.edu
     * matches all hosts in the domain cs.foo.edu. As these characters also
     * match the dots in a domain name, the given pattern will also match all
     * hosts within any subdomain of cs.foo.edu.
     * <p>
     *
     * <b>IP networks</b>
     * <p>
     * You can also export directories to all hosts on an IP (sub-) network
     * simultaneously. This is done by specifying an IP address and netmask pair
     * as address/netmask where the netmask can be specified in dotted-decimal
     * format, or as a contiguous mask length (for example, either
     * `/255.255.252.0' or `/22' appended to the network base address result in
     * identical subnetworks with 10 bits of host). Wildcard characters
     * generally do not work on IP addresses, though they may work by accident
     * when reverse DNS lookups fail.
     * <p>
     *
     *
     * @param path
     * @param builder
     */
    private FsExport(String path, FsExportBuilder builder) {
        _path = path;
        _client = builder.getClient();
        _isTrusted = builder.getIsTrusted();
        _rw = builder.getIo();
        _withAcl = builder.isWithAcl();
        _sec = builder.getSec();
        _allSquash = builder.hasAllSquash();
        _anonUid = builder.getAnonUid();
        _anonGid = builder.getAnonGid();
	_withDcap = builder.isWithDcap();
        _index = getExportIndex(_path);
    }

    public static int getExportIndex(String path) {
        int index = 1;
        for (String s: Splitter.on('/').omitEmptyStrings().split(path) ) {
            index = 31 * index + s.hashCode();
        }
        return index;
    }

    public String getPath() {
        return _path;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(_path)
                .append(':')
                .append(' ')
                .append(_client)
                .append('(').append(_rw)
                .append(',')
                .append(_isTrusted == Root.TRUSTED ? "no_root_squash" : "root_squash")
                .append(',')
                .append(_withAcl ? "acl" : "noacl")
                .append(',')
                .append("sec=").append(_sec)
		.append(',')
		.append(_withDcap ? "dcap" : "no_dcap");
        if (_allSquash) {
            sb.append(",all_squash");
        }
        sb.append(',')
            .append("anonuid=")
            .append(_anonUid);
        sb.append(',')
            .append("anongid=")
            .append(_anonGid);
        sb.append(')')
                .append(':')
                .append("idx=")
                .append(Integer.toHexString(getIndex()));

        return sb.toString();

    }

    public boolean isAllowed(InetAddress client) {
        return IPMatcher.match(_client, client);
    }

    public boolean isTrusted(InetAddress client) {

        // localhost always allowed
        return isAllowed(client) && _isTrusted == Root.TRUSTED;
    }

    public boolean isTrusted() {
        return _isTrusted == Root.TRUSTED;
    }

    public String client() {
        return _client;
    }

    public IO ioMode() {
        return _rw;
    }

    public int getIndex() {
        return _index;
    }

    public boolean checkAcls() {
        return _withAcl;
    }

    public Sec getSec() {
        return _sec;
    }

    public boolean hasAllSquash() {
        return _allSquash;
    }

    public int getAnonUid() {
        return _anonUid;
    }

    public int getAnonGid() {
        return _anonGid;
    }

    public boolean isWithDcap() {
	return _withDcap;
    }

    public static class FsExportBuilder {

        private String _client = "*";
        private IO _io = IO.RO;
        private Root _isTrusted = Root.NOTTRUSTED;
        private boolean _withAcl = false;
        private Sec _sec = Sec.SYS;
        private boolean allSquash = false;
        private int _anonUid = DEFAULT_ANON_UID;
        private int _anonGid = DEFAULT_ANON_GID;
	private boolean _withDcap = true;

        public FsExportBuilder forClient(String client) {
            _client = client;
            return this;
        }

        public FsExportBuilder trusted() {
            _isTrusted = Root.TRUSTED;
            return this;
        }

        public FsExportBuilder notTrusted() {
            _isTrusted = Root.NOTTRUSTED;
            return this;
        }

        public FsExportBuilder ro() {
            _io = IO.RO;
            return this;
        }

        public FsExportBuilder rw() {
            _io = IO.RW;
            return this;
        }

        public FsExportBuilder withAcl() {
            _withAcl = true;
            return this;
        }

        public FsExportBuilder withoutAcl() {
            _withAcl = false;
            return this;
        }

        public FsExportBuilder withSec(Sec sec) {
            _sec = sec;
            return this;
        }

        public FsExportBuilder withAnonUid(int id) {
            _anonUid = id;
            return this;
        }

        public FsExportBuilder withAnonGid(int id) {
            _anonGid = id;
            return this;
        }

        public FsExportBuilder allSquash() {
            allSquash = true;
            return this;
        }

	public FsExportBuilder withDcap() {
	    _withDcap = true;
	    return this;
	}

	public FsExportBuilder withoutDcap() {
	    _withDcap = false;
	    return this;
	}

        public String getClient() {
            return _client;
        }

        public IO getIo() {
            return _io;
        }

        public Root getIsTrusted() {
            return _isTrusted;
        }

        public boolean isWithAcl() {
            return _withAcl;
        }

        public Sec getSec() {
            return _sec;
        }

        public boolean hasAllSquash() {
            return allSquash;
        }

        public int getAnonUid() {
            return _anonUid;
        }

        public int getAnonGid() {
            return _anonGid;
        }

	public boolean isWithDcap() {
	    return _withDcap;
	}

        public FsExport build(String path) {
            return new FsExport(path, this);
        }
    }
}
