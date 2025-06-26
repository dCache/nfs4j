package org.dcache.nfs.benchmarks;

import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.UnixNumericUserPrincipal;
import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
public class SubjectBenchmark {


    @Benchmark
    public Subject subjectByAddingToSet() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UnixNumericUserPrincipal(0));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(0, true));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(1, false));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(2, false));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(3, false));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(4, false));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(5, false));
        subject.getPrincipals().add(new UnixNumericGroupPrincipal(6, false));
        subject.setReadOnly();
        return subject;
    }


    @Benchmark
    public Subject subjectByPassingSet() {

        Principal[] principals = new Principal[]{
              new UnixNumericUserPrincipal(0),
              new UnixNumericGroupPrincipal(0, true),
              new UnixNumericGroupPrincipal(1, false),
              new UnixNumericGroupPrincipal(2, false),
              new UnixNumericGroupPrincipal(3, false),
              new UnixNumericGroupPrincipal(4, false),
              new UnixNumericGroupPrincipal(5, false),
              new UnixNumericGroupPrincipal(6, false)
        };

        return new Subject(true, Set.of(principals), Set.of(), Set.of());
    }
}


