/*
 * Copyright (c) 2009 - 2018 Deutsches Elektronen-Synchroton,
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.dcache.nfs.v4.xdr.layouttype4;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FsExportTest {

    private ExportFile _exportFile;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        _exportFile = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports").toURI());
    }

    @Test
    public void testIsEmpty() {
        assertTrue("Export file should not produce empty export list", _exportFile.getExports().count() > 0);
    }

    @Test
    public void testTrustedMultimpleClients() throws UnknownHostException {

        InetAddress trusted = InetAddress.getByName("www.google.com");
        InetAddress nontrusted = InetAddress.getByName("www.yahoo.com");

        FsExport export1 = _exportFile.getExport("/trusted", trusted);
        FsExport export2 = _exportFile.getExport("/trusted", nontrusted);

        assertTrue("trusted host not respected", export1.isTrusted() );
        assertFalse("nontrusted host respected", export2.isTrusted() );
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
    public void testExportWithSlash() throws UnknownHostException {
        FsExport export;
        export = _exportFile.getExport("/export/with/slash/", InetAddress.getByName("192.169.2.2"));
        assertNotNull("Failed to get export with slash", export);

        export = _exportFile.getExport("/export/with/slash", InetAddress.getByName("192.169.2.2"));
        assertNotNull("Failed to get export with slash", export);
    }

    @Test
    public void testNotAbsolutePath() throws UnknownHostException {
        FsExport export = _exportFile.getExport("not/absolute/path", InetAddress.getByName("192.169.2.2"));
        assertNull("Not absolute path must be ignored", export);
    }

    @Test
    public void testAllRoot() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_all_root", InetAddress.getByName("192.168.1.1"));
        assertTrue(export.isAllRoot());
    }

    @Test
    public void testWithPnfs() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_with_pnfs", InetAddress.getByName("192.168.1.1"));
        assertTrue(export.isWithPnfs());
    }

    @Test
    public void testWithoutPnfs() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_without_pnfs", InetAddress.getByName("192.168.1.1"));
        assertFalse(export.isWithPnfs());
    }

    @Test
    public void testPnfsDefault() throws UnknownHostException {
        FsExport export = _exportFile.getExport("/export_default_pnfs", InetAddress.getByName("192.168.1.1"));
        assertTrue(export.isWithPnfs());
    }

    @Test
    public void testExplicitExportForLocalhost() throws Exception {
        FsExport export = _exportFile.getExport("/", InetAddress.getByName("127.0.0.1"));
        assertNull("localhost must not be able to access without explicit intry", export);
    }

    @Test
    public void testEntrySorting() throws Exception {

        ExportFile exportFile  = new ExportFile(ClassLoader.getSystemResource("org/dcache/nfs/exports.unsorted").toURI());
        String[] sortedEntries = new String[] {
            "10.0.0.1", "10.0.0.0/24", "10.0.0.0/16", "10.0.0.0/8"
        };

        String[] unsortedEntries = exportFile.exportsFor(InetAddress.getByName("10.0.0.1"))
                .map(FsExport::client)
                .toArray(String[]::new);

        assertArrayEquals("the export entries are not sorted as expected", sortedEntries, unsortedEntries);
    }

    @Test
    public void testLyoutTypeOptions() throws Exception {
	FsExport export = _exportFile.getExport("/layouttypes", InetAddress.getByName("172.16.1.1"));

	assertEquals("invalid number of returned layouts", 1, export.getLayoutTypes().size());
	assertEquals("wrong layout type", layouttype4.LAYOUT4_NFSV4_1_FILES, export.getLayoutTypes().get(0));
    }

    @Test
    public void testLyoutTypeOrder1() throws Exception {
	FsExport export = _exportFile.getExport("/layouttypes", InetAddress.getByName("172.16.2.1"));

	assertEquals("invalid number of returned layouts", 2, export.getLayoutTypes().size());
	assertEquals("wrong layout type", layouttype4.LAYOUT4_FLEX_FILES, export.getLayoutTypes().get(0));
	assertEquals("wrong layout type", layouttype4.LAYOUT4_NFSV4_1_FILES, export.getLayoutTypes().get(1));
    }

    @Test
    public void testLyoutTypeOrder2() throws Exception {
	FsExport export = _exportFile.getExport("/layouttypes", InetAddress.getByName("172.16.3.1"));

	assertEquals("invalid number of returned layouts", 2, export.getLayoutTypes().size());
	assertEquals("wrong layout type", layouttype4.LAYOUT4_NFSV4_1_FILES, export.getLayoutTypes().get(0));
	assertEquals("wrong layout type", layouttype4.LAYOUT4_FLEX_FILES, export.getLayoutTypes().get(1));
    }

    @Test
    public void testNoLyoutTypes() throws Exception {
        FsExport export = _exportFile.getExport("/layouttypes", InetAddress.getByName("172.16.4.1"));

        assertTrue("No layout types expected", export.getLayoutTypes().isEmpty());
    }

}
