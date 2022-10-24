package org.dcache.nfs.util;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link Clock} that allows to manually advance the time.
 */
public class ManualClock extends Clock {

    private final AtomicLong currentTime = new AtomicLong();

    @Override
    public Instant instant() {
        return Instant.ofEpochMilli(currentTime.get());
    }

    public void advance(long time, TimeUnit unit) {
        currentTime.addAndGet(unit.toMillis(time));
    }

    public void advance(Duration duration) {
        currentTime.addAndGet(duration.toMillis());
    }

    @Override
    public ZoneId getZone() {
        return Clock.systemDefaultZone().getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedClassVersionError();
    }
}
