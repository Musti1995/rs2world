package be.nfm.rs2.engine;

import be.nfm.rs2.util.Bool;
import be.nfm.rs2.util.Timer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;

@Component
public final class Engine implements Runnable {

    private long currentTick;
    private final HashMap<Long, LinkedList<EventContainer>> eventMap;

    public Engine() {
        this.eventMap = new HashMap<>();
        this.currentTick = 0;
    }

    @Override
    public void run() {
        currentTick++;
        triggerScheduledEvents();
    }

    private void triggerScheduledEvents() {
        LinkedList<EventContainer> eventList = eventMap.get(currentTick);
        if (eventList == null) return;
        for (EventContainer container : eventList) {
            Bool.or(container.isFinished(), container.isCancelled()).computeIfFalse(() -> {
                long newDelay = container.event().trigger();
                Bool.of(newDelay > 0)
                        .computeIfTrue(() -> scheduleEvent(container, newDelay))
                        .computeIfFalse(() -> container.markFinished());
            });
        }
        eventMap.remove(currentTick);
    }

    public EventContainer scheduleEvent(Event event, long delay) {
        return scheduleEvent(new EventContainer(event), delay);
    }

    public EventContainer scheduleEvent(EventContainer eventContainer, long delay) {
        if (delay <= 0) return eventContainer;
        long targetTick = currentTick + delay;
        LinkedList<EventContainer> eventList = eventMap.get(targetTick);
        if (eventList == null) {
            eventList = new LinkedList<>();
            eventMap.put(targetTick, eventList);
        }
        eventList.add(eventContainer);
        return eventContainer;
    }

    public long currentTick() {
        return currentTick;
    }

    public Timer newTimer() {
        return new Timer(this::currentTick);
    }
}
