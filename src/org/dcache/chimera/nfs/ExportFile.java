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
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.StringTokenizer;

import org.dcache.chimera.nfs.FsExport.IO;
import org.dcache.chimera.nfs.FsExport.Root;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class ExportFile {

    private final Set<FsExport> _exports ;

    /**
     * Create a new empty exports list
     */
    public ExportFile()  {
        _exports = new HashSet<FsExport>();
    }

    public ExportFile(File file) throws IOException  {
        _exports = parse(file);
    }

    public List<FsExport> getExports() {
        return Lists.newArrayList(_exports);
    }

    private static Set<FsExport> parse(File exportFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(exportFile));
        Set<FsExport> exports = new HashSet<FsExport>();

        String line;
        try {
            int lineCount = 0;
            while ((line = br.readLine()) != null) {

                ++lineCount;

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.charAt(0) == '#')
                    continue;

                StringTokenizer st = new StringTokenizer(line);
                String path = st.nextToken();

                if( st.hasMoreTokens() ) {
                    while(st.hasMoreTokens() ) {

                        String hostAndOptions = st.nextToken();
                        StringTokenizer optionsTokenizer = new StringTokenizer(hostAndOptions, "(),");

                        String host = optionsTokenizer.nextToken();
                        Root isTrusted = Root.NOTTRUSTED;
                        IO rw = IO.RO;
                        while(optionsTokenizer.hasMoreTokens()) {

                            String option = optionsTokenizer.nextToken();
                            if( option.equals("rw") ) {
                                rw = IO.RW;
                                continue;
                            }

                            if( option.equals("no_root_squash") ) {
                                isTrusted = Root.TRUSTED;
                                continue;
                            }

                        }

                        exports.add(new FsExport(path, host,isTrusted, rw ));
                    }

                }else{
                    exports.add( new FsExport(path, "*", Root.NOTTRUSTED, IO.RO ) );
                }

            }
        } finally {
            try {
                br.close();
            } catch (IOException dummy) {
                // ignored
            }
        }

        return exports;

    }


    public FsExport getExport(String path, InetAddress client) {
        for (FsExport export : _exports) {
            if (export.getPath().equals(path) && export.isAllowed(client)) {
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

    /**
     * add a new export to existing exports
     *
     * @param export
     */
    public void addExport( FsExport export) {
        _exports.add(export);
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
}
