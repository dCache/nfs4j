/*
 * Copyright (c) 2009 - 2019 Deutsches Elektronen-Synchroton,
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

import java.util.Comparator;

import com.google.common.net.InetAddresses;

/**
 * {@link Comparator} to compare host names.
 *
 * This {@link Comparator} should be used to compare/sort host ip and hostnames. The sorting order is - more relevant
 * first, e.q: <pre>
 *   a.b.c.d
 *   sone.host.name
 *   a.b.c.d/N
 *   a.b.c.d/m ( N &gt; n)
 *   somelonger*.at.some.net
 *   some*.at.some.net
 * </pre>
 */
public class HostEntryComparator {

    private final static int MAX_NET_MASK = 128;

    public static int compare(String s1, String s2) {

        if (isFullName(s1) && isFullName(s2)) {
            return netmaskCmp(s1, s2);
        } else if (isFullName(s1)) {
            return -1;
        } else if (isFullName(s2)) {
            return 1;
        }

        return maskCmp(s1, s2);
    }

    private static int netmaskCmp(String s1, String s2) {
        int order = Integer.compare(netmaskOf(s2), netmaskOf(s1));
        return order != 0 ? order : preferIp(s1, s2);
    }

    private static int preferIp(String s1, String s2) {

        String addr1 = stripNetmask(s1);
        String addr2 = stripNetmask(s2);

        if (InetAddresses.isInetAddress(addr1) && InetAddresses.isInetAddress(addr2)) {
            return Integer.compare(
                    // Reverse order to prefer longer (IPv6) addresses
                    InetAddresses.forString(addr2).getAddress().length,
                    InetAddresses.forString(addr1).getAddress().length);
        } else if (InetAddresses.isInetAddress(addr1)) {
            return -1;
        } else if (InetAddresses.isInetAddress(addr2)) {
            return 1;
        }

        // both are full host names
        return 0;
    }

    private static String stripNetmask(String s) {
        int i = s.indexOf('/');
        return i > 0 ? s.substring(0, i) : s;
    }

    private static int netmaskOf(String s) {
        int i = s.indexOf('/');
        return i > 0 ? Integer.parseInt(s.substring(i + 1)) : MAX_NET_MASK;
    }

    private static boolean isFullName(String s) {
        return (s.indexOf('?') < 0) & (s.indexOf('*') < 0);
    }

    private static int maskCmp(String s1, String s2) {
        int maskIndex1 = s1.indexOf('*');
        int maskIndex2 = s2.indexOf('*');

        /*
         * as smaller index should be considered first, compare in reverse order.
         */
        return Integer.compare(maskIndex2, maskIndex1);
    }
}
