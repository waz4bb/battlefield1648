package me.kooruyu.games.battlefield1648.cartography;

import java.util.List;
import java.util.Random;

public class CampData {

    public final char[][] cells;
    public final List<Region> rooms;
    public final Region bounds;
    private Random rand;

    public CampData(char[][] cells, List<Region> rooms, Region bounds, long seed) {
        this.cells = cells;
        this.rooms = rooms;
        this.bounds = bounds;
        this.rand = new Random(seed);
    }

    public Region getRandomRoom() {
        return rooms.get(rand.nextInt(rooms.size()));
    }
}
