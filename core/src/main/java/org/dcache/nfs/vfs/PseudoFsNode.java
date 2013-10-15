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
package org.dcache.nfs.vfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dcache.nfs.FsExport;

/**
 * A Graph like structure to represent file system like tree. Each node can
 * point to one or more other nodes (subdirectories). Node without any
 * subdirectory called leaf. There are two data object attached to each node -
 * it's name and, optionally, export information.
 *
 * @author tigran
 */
public class PseudoFsNode {

    private final List<FsExport> _exports = new ArrayList<>();
    private final Map<String, PseudoFsNode> _children = new HashMap<>();
    private Inode _id;

    public PseudoFsNode(Inode id) {
        _id = id;
    }

    public Collection<String> getChildren() {
        return _children.keySet();
    }

    public void addChild(String name, PseudoFsNode child) {
        _children.put(name, child);
    }

    public boolean isMountPoint() {
        return !_exports.isEmpty();
    }

    public PseudoFsNode getChild(String t) {
        return _children.get(t);
    }

    public List<FsExport> getExports() {
        return _exports;
    }

    public void addExport(FsExport e) {
        _exports.add(e);
    }

    public Inode id() {
        return _id;
    }

    public void setId(Inode id) {
        _id = id;
    }

    public String toString() {
        return _id + " : " + _children.keySet().toString();
    }
}
