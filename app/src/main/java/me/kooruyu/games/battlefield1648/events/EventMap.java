package me.kooruyu.games.battlefield1648.events;

import java.util.Map;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public class EventMap {
    private Map<Vertex, EventObserver> eventObserverMap;

    public EventMap(Map<Vertex, EventObserver> eventObserverMap) {
        this.eventObserverMap = eventObserverMap;
    }

    public EventObserver getEventAt(int x, int y) {
        return eventObserverMap.get(new Vertex(x, y));
    }

    public EventObserver getEventAt(Vertex location) {
        return eventObserverMap.get(location);
    }

    public void disableAll() {
        for (EventObserver e : eventObserverMap.values()) {
            e.setAll(false);
        }
    }
}
