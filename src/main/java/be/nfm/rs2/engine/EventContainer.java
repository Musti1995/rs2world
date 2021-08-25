package be.nfm.rs2.engine;

import be.nfm.rs2.util.Bool;

public class EventContainer {

    private Event event;
    private Bool cancelled;
    private Bool finished;

    public EventContainer(Event event) {
        this.event = event;
    }

    protected Event event() {
        return event;
    }

    public Bool isCancelled() {
        return cancelled;
    }

    public Bool isFinished() {
        return finished;
    }

    public void cancel() {
        this.cancelled = Bool.TRUE;
    }

    protected void markFinished() {
        this.finished = Bool.TRUE;
    }
}
