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

import java.net.UnknownHostException;
import java.net.InetAddress;
import org.junit.Test;

import static com.google.common.net.InetAddresses.forString;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class InetAddressMatcherTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSpecWithName() throws UnknownHostException {
        InetAddressMatcher.forPattern("www.dcache.org/24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSpecWithTooManySlashes() throws UnknownHostException {
        InetAddressMatcher.forPattern("1.1.1.1/24/25");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSpecWithNegMask() throws UnknownHostException {
        InetAddressMatcher.forPattern("1.1.1.1/-7");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSpecWithTooBigMaskV4() throws UnknownHostException {
        InetAddressMatcher.forPattern("1.1.1.1/33");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSpecWithTooBigMaskV6() throws UnknownHostException {
        InetAddressMatcher.forPattern("fe80::3FF:F00D:BAD:F00D/129");
    }

    @Test
    public void testForIpString() throws UnknownHostException {
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("fe80::0:0:0:0/70");
        assertEquals(InetAddressMatcher.IpAddressMatcher.class, ipMatcher.getClass());
    }

    @Test
    public void testForRegexpBigString() throws UnknownHostException {
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("a*.b.c");
        assertEquals(InetAddressMatcher.RegexpNameMatcher.class, ipMatcher.getClass());
    }

    @Test
    public void testForRegexpSmallString() throws UnknownHostException {
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("a.b?.c");
        assertEquals(InetAddressMatcher.RegexpNameMatcher.class, ipMatcher.getClass());
    }

    @Test
    public void testForHostString() throws UnknownHostException {
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.google.com");
        assertEquals(InetAddressMatcher.HostNameMatcher.class, ipMatcher.getClass());
    }

    @Test
    public void testIpV6SuccessfulIpNetMatching() throws UnknownHostException {

        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("fe80::0:0:0:0/70");

        assertTrue("Failed to match host with netmask.", ipMatcher.test(forString("fe80::3FF:F00D:BAD:F00D")));
    }

    @Test
    public void testIpV4SuccessfulIpNetMatching() throws UnknownHostException {

        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("1.1.1.1/16");

        assertTrue("Failed to match host with netmask.", ipMatcher.test(forString("1.1.2.3")));
    }

    @Test
    public void testDomainWildcart1() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.*.org");
        assertTrue("failed to match host by domain", ipMatcher.match(addr));
    }

    @Test
    public void testDomainWildcart2() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.d*.org");
        assertTrue("failed to match host by domain", ipMatcher.match(addr));
    }

    @Test
    public void testDomainWildcartNoMatch() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.b*.org");
        assertFalse("incorrect host matched by domain", ipMatcher.match(addr));
    }

    @Test
    public void testDomainOneChar() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("ww?.dcache.org");
        assertTrue("failed to match host by domain", ipMatcher.match(addr));
    }

    @Test
    public void testDomainOneCharNoMatch() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www1.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("ww?.dcache.org");
        assertFalse("incorrect host matched by domain", ipMatcher.match(addr));
    }

    @Test
    @Ignore
    public void testDomainMatch() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.dcache.org");
        assertTrue("failed to match host by domain", ipMatcher.match(addr));
    }

    @Test
    @Ignore
    public void testDomainMatchMultipleIPs() throws UnknownHostException {

        InetAddress addr = mockInetAddress("www.dcache.org", "1.1.1.1", "fe80::3FF:F00D:BAD:F00D");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.dcache.org");
        assertTrue("failed to match host by domain", ipMatcher.match(addr));
    }

    @Test
    public void testDomainNoMatch() throws UnknownHostException {

        InetAddress addr = mockInetAddress("ww1.dcache.org", "1.1.1.1");
        InetAddressMatcher ipMatcher = InetAddressMatcher.forPattern("www.dcache.org");
        assertFalse("incorrect host matched by domain", ipMatcher.match(addr));
    }

    private InetAddress mockInetAddress(String  dnsName, String...ips) throws UnknownHostException {

        InetAddress mockedAddress = mock(InetAddress.class);
        given(mockedAddress.getHostName()).willReturn(dnsName);
        given(mockedAddress.getCanonicalHostName()).willReturn(dnsName);

        return mockedAddress;
    }
}
