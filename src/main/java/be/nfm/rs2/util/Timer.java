package be.nfm.rs2.util;

import java.util.function.Supplier;

public class Timer {

    private final Supplier<Long> timeSupplier;
    private long lastRefresh;

    public Timer(Supplier<Long> timeSupplier) {
        this.timeSupplier = timeSupplier;
        this.lastRefresh = 0;
    }

    public void reset() {
        lastRefresh = timeSupplier.get();
    }

    public long elapsed() {
        return timeSupplier.get() - lastRefresh;
    }

    public Bool elapsed(long time) {
        return Bool.of(elapsed() >= time);
    }

}
