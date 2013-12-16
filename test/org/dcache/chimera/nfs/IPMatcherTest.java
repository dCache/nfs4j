/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.chimera.nfs;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import static org.junit.Assert.*;

public class IPMatcherTest {

    @Test
    public void testHostWildcart() {

        boolean match = IPMatcher.match("*.desy.de", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testHostWildcart1() {

        boolean match = IPMatcher.match("*", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testHostWildcart2() {

        boolean match = IPMatcher.match("w*.desy.de", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testHostWildcart3() {

        boolean match = IPMatcher.match("b*.desy.de", "www.desy.de");
        assertFalse("Invalid match of host by domain", match);
    }

    @Test
    public void testDomainWildcart() {

        boolean match = IPMatcher.match("www.*.de", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testDomainWildcart2() {

        boolean match = IPMatcher.match("www.d*.de", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testDomainWildcart3() {

        boolean match = IPMatcher.match("www.b*.de", "www.desy.de");
        assertFalse("Invalid to match host by domain", match);
    }

    @Test
    public void testExactMatch() {

        boolean match = IPMatcher.match("www.desy.de", "www.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    /*
     * FIXME: frgale test - depends on DNS record
     */
    @Test
    public void testHostWildcartByIp() throws UnknownHostException {

        boolean match = IPMatcher.match("*.desy.de", InetAddress.getByName("www.desy.de"));
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testHostWildcartOneChar() {
        boolean match = IPMatcher.match("h?.desy.de", "h1.desy.de");
        assertTrue("failed to match host by domain", match);
    }

    @Test
    public void testHostWildcartOneChar2() {
        boolean match = IPMatcher.match("h?.desy.de", "h11.desy.de");
        assertFalse("one one character have to match", match);
    }

    @Test
    public void testIpBased() throws UnknownHostException {

        boolean match = IPMatcher.match(InetAddress.getByName("131.169.214.0"),
                InetAddress.getByName("131.169.40.255"), 16);

        assertTrue("failed to match host by netmask", match);
    }

    @Test
    public void testIpBased2() throws UnknownHostException {

        boolean match = IPMatcher.match(InetAddress.getByName("131.169.40.16"),
                InetAddress.getByName("131.169.40.0"), 28);

        assertFalse("Wrong subnet matched", match);
    }

    @Test
    public void testIpBased3() throws UnknownHostException {

        boolean match = IPMatcher.match(InetAddress.getByName("131.169.40.15"),
                InetAddress.getByName("131.169.40.0"), 28);

        assertTrue("subnet not matched", match);
    }

    @Test
    public void testIpBasedNegative() throws UnknownHostException {

        boolean match = IPMatcher.match(InetAddress.getByName("131.169.214.0"),
                InetAddress.getByName("131.169.40.255"),
                24);

        assertFalse("Invalid ip to matched", match);
    }

    @Test
    public void testMixed() throws UnknownHostException {

        boolean match = IPMatcher.match("localhost",
                InetAddress.getByName("127.0.0.1"));

        assertTrue("failed to match localhost", match);
    }

    @Test
    public void testIpV6SuccessfulIpNetMatchingFractionedMask() throws UnknownHostException {

        boolean match = IPMatcher.match(InetAddress.getByName("fe80::3FF:F00D:BAD:F00D"),
                InetAddress.getByName("fe80::0:0:0:0"), 70);

        assertTrue("Failed to match host with netmask.", match);
    }
}
