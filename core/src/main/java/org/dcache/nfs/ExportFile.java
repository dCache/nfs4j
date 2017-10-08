/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.dcache.nfs.v4.xdr.layouttype4;


public class ExportFile {

    private static final Logger _log = LoggerFactory.getLogger(ExportFile.class);

    private volatile ImmutableMultimap<Integer,FsExport> _exports;
    private final URI _exportFile;

    public ExportFile(File file) throws IOException {
        this(file.toURI());
    }

    public ExportFile(URI uri) throws IOException {
        _exportFile = uri;
        _exports = parse(uri);
    }

    public ExportFile(Reader reader) throws IOException  {
        _exportFile = null;
        _exports = parse(reader);
    }

    public Stream<FsExport> getExports() {
        return _exports.values().stream();
    }

    private static ImmutableMultimap<Integer,FsExport> parse(Reader reader) throws IOException {
        List<String> lines;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            lines = bufferedReader.lines().collect(Collectors.toList());
        }
        return parse(lines);
    }

    private static ImmutableMultimap<Integer,FsExport> parse(URI exportFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(exportFile));
        return parse(lines);
    }

    private static ImmutableMultimap<Integer,FsExport> parse(Iterable<String> lines) throws IOException {

        ImmutableListMultimap.Builder<Integer,FsExport> exportsBuilder = ImmutableListMultimap.builder();

        for (String line: lines) {

            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (line.charAt(0) == '#') {
                continue;
            }

            if (line.charAt(0) != '/') {
                _log.warn("Ignoring entry with non absolute export path: " + line);
                continue;
            }

            int pathEnd = line.indexOf(' ');

            String path;
            if (pathEnd < 0) {
                FsExport export = new FsExport.FsExportBuilder().build(line);
                exportsBuilder.put(export.getIndex(), export);
                continue;
            } else {
                path = line.substring(0, pathEnd);
            }

            Splitter splitter = Splitter.on(' ').omitEmptyStrings().trimResults();

            for (String hostAndOptions : splitter.split(line.substring(pathEnd + 1))) {

                try {
                    FsExport.FsExportBuilder exportBuilder = new FsExport.FsExportBuilder();

                    Iterator<String> s = Splitter
                            .on(CharMatcher.anyOf("(,)"))
                            .omitEmptyStrings()
                            .trimResults()
                            .split(hostAndOptions).iterator();

                    String host = s.next();

                    exportBuilder.forClient(host);
                    while (s.hasNext()) {
                        String option = s.next();

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

                        if (option.equals("noacl") || option.equals("no_acl")) {
                            exportBuilder.withoutAcl();
                            continue;
                        }

                        if (option.equals("all_squash")) {
                            exportBuilder.allSquash();
                            continue;
                        }

                        if (option.startsWith("sec=")) {
                            String secFlavor = option.substring(4);
                            exportBuilder.withSec(FsExport.Sec.valueOf(secFlavor.toUpperCase()));
                            continue;
                        }

                        if (option.startsWith("anonuid=")) {
                            int anonuid = Integer.parseInt(option.substring(8));
                            exportBuilder.withAnonUid(anonuid);
                            continue;
                        }

                        if (option.startsWith("anongid=")) {
                            int anongid = Integer.parseInt(option.substring(8));
                            exportBuilder.withAnonGid(anongid);
                            continue;
                        }

                        if (option.equals("dcap")) {
                            exportBuilder.withDcap();
                            continue;
                        }

                        if (option.equals("no_dcap")) {
                            exportBuilder.withoutDcap();
                            continue;
                        }

                        if (option.equals("all_root")) {
                            exportBuilder.withAllRoot();
                            continue;
                        }

                        if (option.equals("pnfs")) {
                            exportBuilder.withPnfs();
                            continue;
                        }

                        if (option.equals("nopnfs") || option.equals("no_pnfs")) {
                            exportBuilder.withoutPnfs();
                            continue;
                        }

			if (option.startsWith("lt=")) {
			    Iterable<String> lt = Splitter.on(":")
				.omitEmptyStrings()
				.split(option.substring(3));

			    StreamSupport.stream(lt.spliterator(), false)
				.map(String::toUpperCase)
				.map(t -> "LAYOUT4_" + t)
				.map(layouttype4::valueOf)
				.forEach(exportBuilder::withLayoutType);
			    continue;
			}

                        throw new IllegalArgumentException("Unsupported option: " + option);
                    }
                    FsExport export = exportBuilder.build(path);
                    exportsBuilder.put(export.getIndex(), export);
                } catch (IllegalArgumentException e) {
                    _log.error("Invalid export entry [" + hostAndOptions + "] : " + e.getMessage());
                }
            }
        }

        /*
         * sort in reverse order to get smallest network first
         */
        return exportsBuilder
                .orderValuesBy(Ordering.from(HostEntryComparator::compare).onResultOf(FsExport::client).reverse())
                .build();
    }

    public FsExport getExport(String path, InetAddress client) {
        String normalizedPath = FsExport.normalize(path);
        return getExport(FsExport.getExportIndex(normalizedPath), client);
    }

    public FsExport getExport(int index, InetAddress client) {
        for (FsExport export : _exports.get(index)) {
            if (export.isAllowed(client)) {
                return export;
            }
        }
        return null;
    }

    public Stream<FsExport> exportsFor(InetAddress client) {
        return _exports.values().stream().filter(e -> e.isAllowed(client));
    }

    public void rescan() throws IOException {
        if (_exportFile == null) {
            throw new IllegalStateException("exports uri not set, rescan impossible");
        }
        _exports = parse(_exportFile);
    }
}
