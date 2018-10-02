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
package org.dcache.nfs.vfs;

import com.google.common.collect.ForwardingNavigableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Function;
import org.dcache.nfs.v4.xdr.nfs4_prot;

/**
 *
 */
public class DirectoryStream implements Iterable<DirectoryEntry>{

    // v4 and v3 have the same verifier size
    public final static byte[] ZERO_VERIFIER = new byte[nfs4_prot.NFS4_VERIFIER_SIZE];

    private final byte[] verifier;
    private final NavigableSet<DirectoryEntry> entries;

    public DirectoryStream(Collection<DirectoryEntry> entries) {
        this(ZERO_VERIFIER, new TreeSet<>(entries));
    }

    public DirectoryStream(byte[] verifier, Collection<DirectoryEntry> entries) {
        this(verifier, new TreeSet<>(entries));
    }

    public DirectoryStream(byte[] verifier, NavigableSet<DirectoryEntry> entries) {
        this.verifier = verifier;
        this.entries = Collections.unmodifiableNavigableSet(entries);
    }

    public byte[] getVerifier() {
        return verifier;
    }

    @Override
    public Iterator<DirectoryEntry> iterator() {
        return entries.iterator();
    }

    /**
     * Get view of this DirectoryStream, where all entries have cookies
     * greater than specified {@code fromCookie}.
     *
     * @param fromCookie lowest cookie, exclusive.
     * @return view of this DirectoryStream, where all entries have cookies
     * greater than specified {@code fromCookie}.
     */
    public DirectoryStream tail(long fromCookie) {
        final DirectoryEntry cookieEntry = new DirectoryEntry("", null, null, fromCookie);
        return new DirectoryStream(verifier, entries.tailSet(cookieEntry, false));
    }

    /**
     * Returns a {@link DirectoryStream} that applies {@code function} to
     * each entry in this stream.
     *
     * @param function a transformation function to apply.
     * @return the new stream with transformed elements.
     */
    public DirectoryStream transform(Function<? super DirectoryEntry, DirectoryEntry> function) {
        return new DirectoryStream(this.verifier, new TransformingNavigableSet(function, entries));
    }

    private static class TransformingNavigableSet extends ForwardingNavigableSet<DirectoryEntry> {

        private final Function<? super DirectoryEntry, DirectoryEntry> transformation;
        private final NavigableSet<DirectoryEntry> inner;

        public TransformingNavigableSet(Function<? super DirectoryEntry, DirectoryEntry> transformation, NavigableSet<DirectoryEntry> inner) {
            this.transformation = transformation;
            this.inner = inner;
        }

        @Override
        protected NavigableSet<DirectoryEntry> delegate() {
            return inner;
        }

        @Override
        public Iterator<DirectoryEntry> iterator() {
            return new TransformingIterator(transformation, delegate().iterator());
        }

        /*
         * the result of ForwardingNavigableSet#tailSet does not inherit
         * overloaded iterator, thus we have to override it again.
         */
        @Override
        public NavigableSet<DirectoryEntry> tailSet(DirectoryEntry fromElement, boolean inclusive) {
            return new TransformingNavigableSet(transformation, super.tailSet(fromElement, inclusive));
        }
    }

    // iterator decorator.
    private static class TransformingIterator implements Iterator<DirectoryEntry> {

        private final Function<? super DirectoryEntry, DirectoryEntry> transformation;
        private final Iterator<DirectoryEntry> inner;

        public TransformingIterator(Function<? super DirectoryEntry, DirectoryEntry> transformation, Iterator<DirectoryEntry> inner) {
            this.transformation = transformation;
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public DirectoryEntry next() {
            return transformation.apply(inner.next());
        }
    }
}
