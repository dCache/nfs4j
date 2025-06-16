/*
 * Copyright (c) 2020 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.util;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.UnixNumericUserPrincipal;

/**
 * A collection of utility methods to manipulate with Unix based subjects.
 */
public class UnixSubjects {

    private UnixSubjects() {
    }

    /**
     * Returns true if and only if subjects contains UnixNumericUserPrincipal with uid 0 (zero).
     *
     * @param subject subject to evaluate.
     * @return true if subjects contains UnixNumericUserPrincipal with uid 0.
     */
    public static boolean isRootSubject(Subject subject) {
        return hasUid(subject, 0);
    }

    /**
     * Returns true if and only if subjects doesn't contain any UnixNumericUserPrincipal.
     *
     * @param subject subject to evaluate.
     * @return true if subjects doesn't contain any UnixNumericUserPrincipal.
     */
    public static boolean isNobodySubject(Subject subject) {
        return subject.getPrincipals().stream()
                .noneMatch(UnixNumericUserPrincipal.class::isInstance);
    }

    /**
     * Returns true if and only if the subject has the given uid.
     *
     * @param subject
     * @param uid
     * @return true, if the subject has given uid..
     */
    public static boolean hasUid(Subject subject, long uid) {

        return subject.getPrincipals().stream()
                .filter(UnixNumericUserPrincipal.class::isInstance)
                .map(UnixNumericUserPrincipal.class::cast)
                .anyMatch(p -> p.longValue() == uid);
    }

    /**
     * Returns true if and only if the subject has as primary or secondary the given gid.
     *
     * @param subject
     * @param gid
     * @return true, if the subject has given gid.
     */
    public static boolean hasGid(Subject subject, long gid) {
        return subject.getPrincipals().stream()
                .filter(UnixNumericGroupPrincipal.class::isInstance)
                .map(UnixNumericGroupPrincipal.class::cast)
                .anyMatch(p -> p.longValue() == gid);
    }

    /**
     * Create subject with given uid and gid.
     *
     * @param uid users numeric id.
     * @param gid users primary group numeric id.
     * @return subject with given uid, gid.
     */
    public static Subject toSubject(long uid, long gid) {
        return new Subject(false,
                Set.of(new UnixNumericUserPrincipal(uid), new UnixNumericGroupPrincipal(gid, true)),
                Set.of(),
                Set.of());
    }

    /**
     * Create subject with given uid, primary gid and secondary gids.
     *
     * @param uid users numeric id.
     * @param gid users primary group numeric id.
     * @param gids array of users secondary group numeric ids.
     * @return subject with given uid, gid and gids.
     */
    public static Subject toSubject(long uid, long gid, long... gids) {
        Subject subject = toSubject(uid, gid);
        subject.getPrincipals()
                .addAll(
                        Arrays.stream(gids)
                                .mapToObj(l -> new UnixNumericGroupPrincipal(l, false))
                                .collect(Collectors.toSet()));
        return subject;
    }

    /**
     * Returns the user ID represented by UnixNumericUserPrincipal.
     *
     * @param subject subject to evaluate.
     * @return the user id.
     */

    public static long getUid(Subject subject) {
        return subject.getPrincipals().stream().filter(UnixNumericUserPrincipal.class::isInstance)
                .map(UnixNumericUserPrincipal.class::cast)
                .mapToLong(UnixNumericUserPrincipal::longValue)
                .findFirst()
                .getAsLong();
    }

    /**
     * Returns the primary group ID of a subject represented by UnixNumericGroupPrincipal.
     *
     * @param subject subject to evaluate.
     * @return the primary group ID.
     */
    public static long getPrimaryGid(Subject subject) {
        return subject.getPrincipals().stream().filter(UnixNumericGroupPrincipal.class::isInstance)
                .map(UnixNumericGroupPrincipal.class::cast)
                .filter(UnixNumericGroupPrincipal::isPrimaryGroup)
                .mapToLong(UnixNumericGroupPrincipal::longValue)
                .findFirst()
                .getAsLong();
    }

    /**
     * Returns the secondary group IDs of a subject represented by UnixNumericGroupPrincipal.
     *
     * @param subject subject to evaluate.
     * @return an array with secondary group IDs, possibly empty.
     */
    public static long[] getSecondaryGids(Subject subject) {
        return subject.getPrincipals().stream().filter(UnixNumericGroupPrincipal.class::isInstance)
                .map(UnixNumericGroupPrincipal.class::cast)
                .filter(Predicate.not(UnixNumericGroupPrincipal::isPrimaryGroup))
                .mapToLong(UnixNumericGroupPrincipal::longValue)
                .toArray();
    }

}
