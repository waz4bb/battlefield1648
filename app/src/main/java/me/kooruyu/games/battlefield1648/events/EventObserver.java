package me.kooruyu.games.battlefield1648.events;


import android.os.Bundle;

public class EventObserver {
    private final EventCallable[] linkedEvents;
    private boolean enabled;

    public EventObserver(EventCallable linkedEvent, boolean enabled) {
        linkedEvents = new EventCallable[]{linkedEvent};
        this.enabled = enabled;
    }

    public EventObserver(EventCallable[] linkedEvents) {
        this.linkedEvents = linkedEvents;
    }

    public void triggerAll() {
        for (EventCallable e : linkedEvents) {
            e.trigger();
        }
    }

    public void setAll(boolean active) {
        for (EventCallable e : linkedEvents) {
            e.setActive(active);
        }
    }

    public Bundle[] getAllMetadata() {
        Bundle[] metadata = new Bundle[linkedEvents.length];

        for (int i = 0; i < linkedEvents.length; i++) {
            metadata[i] = linkedEvents[i].getMetadata();
        }
        return metadata;
    }

    public void trigger(int index) {
        linkedEvents[index].trigger();
    }

    public void setActive(int index, boolean active) {
        linkedEvents[index].setActive(active);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
