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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


public class ExportFile {

    private volatile List<FsExport> _exports ;
    private final URL _exportFile;

    public ExportFile(File file) throws IOException {
        this(file.toURI().toURL());
    }

    public ExportFile(URL url) throws IOException  {
        _exportFile = url;
        _exports = parse(_exportFile);
    }

    public List<FsExport> getExports() {
        return Lists.newArrayList(_exports);
    }

    private static List<FsExport> parse(URL exportFile) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(exportFile.openStream()));
        List<FsExport> exports = new ArrayList<FsExport>();

        String line;
        try {
            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.charAt(0) == '#')
                    continue;

                StringTokenizer st = new StringTokenizer(line);
                String path = st.nextToken();
                FsExport.FsExportBuilder exportBuilder = new FsExport.FsExportBuilder();

                if (!st.hasMoreTokens()) {
                    FsExport export = exportBuilder.build(path);
                    exports.add(export);
                    continue;
                }

                while (st.hasMoreTokens()) {

                    String hostAndOptions = st.nextToken();
                    StringTokenizer optionsTokenizer = new StringTokenizer(hostAndOptions, "(),");

                    String host = optionsTokenizer.nextToken();
                    if (!isValidHostSpecifier(host))
                        continue;

                    exportBuilder.forClient(host);
                    while (optionsTokenizer.hasMoreTokens()) {

                        String option = optionsTokenizer.nextToken();
                        if (option.equals("rw")) {
                            exportBuilder.rw();
                            continue;
                        }

                        if (option.equals("ro")) {
                            exportBuilder.ro();
                            continue;
                        }

                        if (option.equals("root_squash")) {
                            exportBuilder.notTrusted();
                            continue;
                        }

                        if (option.equals("no_root_squash")) {
                            exportBuilder.trusted();
                            continue;
                        }

                        if (option.equals("acl")) {
                            exportBuilder.withAcl();
                            continue;
                        }

                        if (option.equals("noacl")) {
                            exportBuilder.withoutAcl();
                            continue;
                        }
                    }
                    FsExport export = exportBuilder.build(path);
                    exports.add(export);
                }

            }
        } finally {
            try {
                br.close();
            } catch (IOException dummy) {
                // ignored
            }
        }

        Collections.sort(exports, new Comparator<FsExport>() {
            @Override
            public int compare(FsExport e1, FsExport e2) {
                return HostEntryComparator.compare(e1.client(), e2.client());
            }
        });
        return exports;
    }

    /**
     * Check for valid host name. The allowed format is:
     * <pre>
     *   IPv4[/n]
     *   IPv6[/N]
     *   host.domain[/N]
     * </pre>
     *
     * @param s
     * @return
     */
    private static boolean isValidHostSpecifier(String s) {
        int maskIdx = s.indexOf('/');

        String host;
        String mask;
        if (maskIdx < 0) {
            host = s;
            mask = "128";
        } else {
            host = s.substring(0, maskIdx);
            mask = s.substring(maskIdx + 1);
        }

        return (isValidIpAddress(host) && isValidNetmask(mask))
                || (isValidHostName(host) || isValidWildcard(host));
    }

    private static boolean isValidIpAddress(String s) {
        try {
            InetAddresses.forString(s);
            return true;
        } catch (IllegalArgumentException e) {
        }
        return false;
    }

    private static boolean isValidHostName(String s) {
        return InternetDomainName.isValid(s);
    }

    private static boolean isValidWildcard(String s) {
        return isValidHostName(s.replace('?', 'a').replace('*', 'a'));
    }

    private static boolean isValidNetmask(String s) {
        try {
            int mask = Integer.parseInt(s);
            return mask >= 0 && mask <= 128;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public FsExport getExport(String path, InetAddress client) {
        for (FsExport export : _exports) {
            if (export.getPath().equals(path) && export.isAllowed(client)) {
                return export;
            }
        }
        return null;
    }

    public FsExport getExport(int index, InetAddress client) {
        for (FsExport export : _exports) {
            if (export.getIndex() == index && export.isAllowed(client)) {
                return export;
            }
        }
        return null;
    }

    // FIXME: one trusted client has an access to all tree
    public boolean isTrusted(java.net.InetAddress client) {

        List<FsExport> exports = getExports();
        for (FsExport export : exports) {
            if (export.isTrusted(client)) {
                return true;
            }
        }
        return false;
    }


    public Collection<FsExport> exportsFor(InetAddress client) {
        return Collections2.filter(_exports, new AllowedExports(client));
    }

    private static class AllowedExports implements Predicate<FsExport> {

        private final InetAddress _client;

        public AllowedExports(InetAddress client) {
            _client = client;
        }

        @Override
        public boolean apply(FsExport export) {
            return export.isAllowed(_client);
        }
    }

    public void rescan() throws IOException {
        _exports = parse(_exportFile);
    }
}
