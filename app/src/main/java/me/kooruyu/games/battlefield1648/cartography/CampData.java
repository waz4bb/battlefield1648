package me.kooruyu.games.battlefield1648.cartography;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.kooruyu.games.battlefield1648.util.ListUtils;

public class CampData {

    public final char[][] cells;
    public final int width, height;
    public final List<Region> rooms;
    public final Region bounds;
    private Random rand;

    public CampData(char[][] cells, List<Region> rooms, Region bounds, long seed) {
        this.cells = cells;
        this.rooms = rooms;
        this.bounds = bounds;
        this.rand = new Random(seed);
        height = cells.length;
        width = cells[0].length;
    }

    public Region getRandomRoom() {
        return rooms.get(rand.nextInt(rooms.size()));
    }

    public List<Region> getRandomRoomList(int numRooms) {
        return ListUtils.getRandomSubList(rooms, numRooms, rand);
    }

    public List<Vertex> getRandomRoomPositions(int numPositions) {
        List<Region> randomRooms = ListUtils.getRandomSubList(rooms, numPositions, rand);
        List<Vertex> randomPositions = new ArrayList<>(numPositions);

        for (Region room : randomRooms) {
            List<Vertex> possibleLocations = new ArrayList<>((room.height - 2) * (room.width - 2));

            for (int y = room.y; y < (room.y + room.width); y++) {
                for (int x = room.x; x < (room.x + room.width); x++) {
                    if (cells[y][x] == '.') {
                        possibleLocations.add(new Vertex(x, y));
                    }
                }
            }

            randomPositions.add(possibleLocations.get(rand.nextInt(possibleLocations.size())));
        }

        return randomPositions;
    }

    public Vertex getStartingPosition() {
        List<Vertex> possibleLocations = new ArrayList<>(height * bounds.x);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < bounds.x; x++) {
                if (cells[y][x] == ',') {
                    possibleLocations.add(new Vertex(x, y));
                }
            }
        }

        return possibleLocations.get(rand.nextInt(possibleLocations.size()));
    }
}
