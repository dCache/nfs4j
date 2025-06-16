package org.dcache.nfs.util;

import static org.dcache.nfs.util.UnixSubjects.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.security.auth.Subject;

import org.junit.Test;

import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.UnixNumericUserPrincipal;

public class UnixSubjectsTest {

    @Test
    public void shouldBeRootIfSubjectHasUidZero() {
        Subject subject = toSubject(0, 0);
        assertThat(isRootSubject(subject), is(true));
    }

    @Test
    public void shouldNotBeRootIfSubjectHasUidZero() {
        Subject subject = toSubject(1, 1);
        assertThat(isRootSubject(subject), is(false));
    }

    @Test
    public void shouldBeNobodyIfNoUid() {
        Subject subject = new Subject(false,
                Set.of(new UnixNumericGroupPrincipal(1, true)),
                Set.of(),
                Set.of());

        assertThat(isNobodySubject(subject), is(true));
    }

    @Test
    public void shouldContainDesiredUid() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(hasUid(subject, 1), is(true));
    }

    @Test
    public void shouldContainDesiredGid() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(hasGid(subject, 2), is(true));
        assertThat(hasGid(subject, 4), is(true));
        assertThat(hasGid(subject, 5), is(true));
    }

    @Test
    public void shouldNotContainOtherGid() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(hasGid(subject, 7), is(false));
    }

    @Test
    public void shouldNotContainOtherUid() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(hasUid(subject, 7), is(false));
    }

    @Test
    public void shouldBuildSubjectWithProvidedUidAndGid() {
        Subject subject = toSubject(1, 2);
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericUserPrincipal(1)));
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericGroupPrincipal(2, true)));
    }

    @Test
    public void shouldBuildSubjectWithProvidedUidAndGids() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericUserPrincipal(1)));
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericGroupPrincipal(2, true)));
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericGroupPrincipal(4, false)));
        assertThat(subject.getPrincipals(), hasItem(new UnixNumericGroupPrincipal(5, false)));
    }

    @Test
    public void shouldReturnUid() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(getUid(subject), is(1L));
    }

    @Test
    public void shouldReturnPrimaryGroup() {
        Subject subject = toSubject(1, 2, 4, 5);
        assertThat(getPrimaryGid(subject), is(2L));
    }

    @Test
    public void shouldReturnSecondaryGroups() {
        Subject subject = toSubject(1, 2, 4, 5);

        // hamcrest can't work with primitive arrays.
        Set<Long> gids = LongStream.of(getSecondaryGids(subject))
                .mapToObj(Long::valueOf)
                .collect(Collectors.toSet());

        assertThat(gids, hasSize(2));
        assertThat(gids, containsInAnyOrder(4L, 5L));
    }
}
