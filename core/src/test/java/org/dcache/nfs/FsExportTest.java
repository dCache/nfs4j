/*
 * Copyright (c) 2009 - 2013 Deutsches Elektronen-Synchroton,
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

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FsExportTest {

    private ExportFile _exportFile;

    @Before
    public void setUp() throws IOException {
        _exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports"));
    }

    @Test
    public void testIsEmpty() {

        Iterable<FsExport> exports = _exportFile.getExports();
        assertFalse("Export file should not produce empty export list", Iterables.isEmpty(exports));
    }

    @Test
    public void testTrustedMultimpleClients() throws UnknownHostException {

        InetAddress trusted = InetAddress.getByName("www.google.com");
        InetAddress nontrusted = InetAddress.getByName("www.yahoo.com");

        FsExport export1 = _exportFile.getExport("/trusted", trusted);
        FsExport export2 = _exportFile.getExport("/trusted", nontrusted);

        assertTrue("trusted host not respected", export1.isTrusted(trusted) );
        assertFalse("nontrusted host respected", export2.isTrusted(nontrusted) );
    }

    @Test
    public void testSubnets_B() throws UnknownHostException {

        InetAddress allowed = InetAddress.getByName("192.168.2.2");
        InetAddress deny = InetAddress.getByName("192.168.3.1");

        FsExport export1 = _exportFile.getExport("/subnet_b", allowed);
        FsExport export2 = _exportFile.getExport("/subnet_b", deny);

        assertNotNull("Allowed host not recognized", export1);
        assertNull("Deny host not recognized", export2);
    }

    @Test
    public void testSubnets_C() throws UnknownHostException {
        InetAddress allowed = InetAddress.getByName("192.168.2.2");
        InetAddress deny = InetAddress.getByName("192.169.2.2");

        FsExport export1 = _exportFile.getExport("/subnet_c", allowed);
        FsExport export2 = _exportFile.getExport("/subnet_c", deny);


        assertNotNull("Allowed host not recognized", export1);
        assertNull("Deny host not recognized", export2);
    }

    @Test
    public void testSubnets_Bad() throws UnknownHostException {
        FsExport export1 = _exportFile.getExport("/subnet_bad", InetAddress.getByName("192.168.2.1"));
        FsExport export2 = _exportFile.getExport("/subnet_bad", InetAddress.getByName("192.169.2.2"));

        assertNull("Deny host not recognized", export1);
        assertNull("Deny host not recognized", export2);
    }

    @Test
    public void testSubnets_IPv6() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/subnet_ipv6", InetAddress.getByName("fe80::21c:c0ff:fea0:caf4"));
        assertNotNull("Ipv6 Allowed not recognized", export);
    }

    @Test
    public void testSubnets_IPv6Bad() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/subnet_ipv6",  InetAddress.getByName("ae80::21c:c0ff:fea0:caf4"));
        assertNull("Deny ipv6 not recognized", export);
    }

    @Test
    public void testSecKrb5() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_with_krp5", InetAddress.getByName("192.169.2.2"));
        assertEquals("Invalid security flavor", FsExport.Sec.KRB5, export.getSec());
    }

    @Test
    public void testSecKrb5i() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_with_krp5i", InetAddress.getByName("192.169.2.2"));
        assertEquals("Invalid security flavor", FsExport.Sec.KRB5I, export.getSec());
    }

    @Test
    public void testSecKrb5p() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_with_krp5p", InetAddress.getByName("192.169.2.2"));
        assertEquals("Invalid security flavor", FsExport.Sec.KRB5P, export.getSec());
    }

    @Test
    public void testIgnoreBadExport() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/bad_export", InetAddress.getByName("192.169.2.2"));
        assertNull("Invalid export entry not ignored", export);
    }

    @Test
    public void testAllSquash() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/all_squash", InetAddress.getByName("192.169.2.2"));
        assertTrue("all_squash option not detected", export.hasAllSquash());
    }

    @Test
    public void testNoSquash() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/no_squash", InetAddress.getByName("192.169.2.2"));
        assertFalse("all_squash not defined, but detected", export.hasAllSquash());
    }

    @Test
    public void testAnonUid() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/anon_access", InetAddress.getByName("192.169.2.2"));
        assertEquals("anonuid not respected", 500, export.getAnonUid());
        assertEquals("anongid not respected", 501, export.getAnonGid());
    }

    @Test
    public void testAnonUidDefault() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/anon_access_default", InetAddress.getByName("192.169.2.2"));
        assertEquals("default anonuid not used", FsExport.DEFAULT_ANON_UID, export.getAnonUid());
        assertEquals("default anongid not used", FsExport.DEFAULT_ANON_GID, export.getAnonUid());
    }

    @Test
    public void testWithDcap() throws UnknownHostException {
	FsExport export = _exportFile.getExport("/with_dcap", InetAddress.getByName("192.169.2.2"));
	assertTrue("dcap should be default option", export.isWithDcap());
    }

    @Test
    public void testWithoutDcap() throws UnknownHostException {
	FsExport export = _exportFile.getExport("/without_dcap", InetAddress.getByName("192.169.2.2"));
	assertFalse("dcap is not disabled", export.isWithDcap());
    }

    @Test
    public void testExplicitExportForLocalhost() throws Exception {
        FsExport export = _exportFile.getExport("/", InetAddress.getByName("127.0.0.1"));
        assertNull("localhost must not be able to access without explicit intry", export);
    }
}
