package me.kooruyu.games.battlefield1648.events;

import java.util.HashMap;
import java.util.Map;

import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class EventMap {
    private Map<Vertex, EventObserver> eventObserverMap;

    public EventMap() {
        eventObserverMap = new HashMap<>();
    }

    public EventMap(Map<Vertex, EventObserver> eventObserverMap) {
        this.eventObserverMap = eventObserverMap;
    }

    public boolean containsPosition(int x, int y) {
        return eventObserverMap.containsKey(new Vertex(x, y));
    }

    public boolean containsPosition(Vertex location) {
        return eventObserverMap.containsKey(location);
    }

    public void put(Vertex location, EventObserver observer) {
        eventObserverMap.put(location, observer);
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
