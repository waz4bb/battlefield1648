package me.kooruyu.games.battlefield1648.cartography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MapGenerator {
    private static final int PARTITION_THRESHOLD = 8;
    private static final int MINIMUM_SIDE_LENGTH = 6;

    private Random rand;
    private char[][] map;
    private long seed;

    public MapGenerator(long seed) {
        this.seed = seed;
        rand = new Random(seed);
    }

    public CampData generateCamp(int width, int height) {
        map = new char[height][width];

        //fill map with empty space
        for (char[] line : map) {
            Arrays.fill(line, ',');
        }

        //create camp borders
        //10%-20%
        int verticalOffset = rand.nextInt((int) (width * .1)) + (int) (width * .1);
        //5%-15%
        int horizontalOffset = rand.nextInt((int) (height * .05)) + (int) (height * .1);
        //5%-15%
        int campHeight = map.length - horizontalOffset;
        //10%-20%
        int campWidth = map[0].length - verticalOffset;

        //Partitioning camp area into regions
        Queue<Region> partitions = new LinkedList<>();
        List<Region> minimumPartitions = new ArrayList<>();

        partitions.offer(new Region(verticalOffset, horizontalOffset, campWidth - verticalOffset, campHeight - horizontalOffset));

        while (!partitions.isEmpty()) {
            Region partition = partitions.poll();

            if (partition.width > PARTITION_THRESHOLD || partition.height > PARTITION_THRESHOLD) {
                if (partition.height <= partition.width && (partition.width > PARTITION_THRESHOLD && rand.nextInt(partition.width + partition.height) < partition.width)) {
                    // split vertically
                    int split = rand.nextInt(Math.max((int) (partition.width * .8), partition.width) - Math.max((int) (partition.width * .4), 8)) + Math.max((int) (partition.width * .4), 8);
                    partitions.offer(new Region(partition.x, partition.y, split, partition.height));
                    partitions.offer(new Region(partition.x + split, partition.y, partition.width - split, partition.height));
                    continue;
                } else if (partition.height > PARTITION_THRESHOLD) {
                    // split horizontally
                    int split = rand.nextInt(Math.max((int) (partition.height * .8), partition.height) - Math.max((int) (partition.height * .4), 8)) + Math.max((int) (partition.height * .4), 8);
                    partitions.offer(new Region(partition.x, partition.y, partition.width, split));
                    partitions.offer(new Region(partition.x, partition.y + split, partition.width, partition.height - split));
                    continue;
                }
            }
            if (partition.width >= MINIMUM_SIDE_LENGTH && partition.height >= MINIMUM_SIDE_LENGTH) {
                minimumPartitions.add(partition);
            }
        }

        int boundsMinX, boundsMinY, boundsMaxX, boundsMaxY;
        boundsMaxX = boundsMaxY = 0;
        boundsMinX = boundsMinY = Integer.MAX_VALUE;

        for (Region r : minimumPartitions) {
            if (r.x < boundsMinX) boundsMinX = r.x;
            if (r.y < boundsMinY) boundsMinY = r.y;
            if ((r.x + r.width) > boundsMaxX) boundsMaxX = r.x + r.width;
            if ((r.y + r.height) > boundsMaxY) boundsMaxY = r.y + r.height;

            for (int y = r.y; y < (r.y + r.height); y++) {
                if (y == r.y || y == (r.y + r.height - 1)) {
                    for (int x = r.x; x < (r.x + r.width); x++) {
                        map[y][x] = '#';
                    }
                } else {
                    map[y][r.x] = '#';
                    map[y][r.x + r.width - 1] = '#';
                    for (int x = r.x + 1; x < (r.x + r.width - 1); x++) {
                        map[y][x] = '.';
                    }
                }
            }
        }

        for (int y = boundsMinY - 1; y <= boundsMaxY + 1; y++) {
            for (int x = boundsMinX - 1; x <= boundsMaxX + 1; x++) {
                if (map[y][x] == ',') map[y][x] = '=';
            }
        }

		/*
         * Generate Rooms
		 */


        List<List<Vertex>> toOtherRooms = new ArrayList<>();
        List<List<Vertex>> toOutsideConnections = new ArrayList<>();

        //find connections
        for (Region r : minimumPartitions) {
            List<Vertex> toOutside = new ArrayList<>();
            List<Vertex> toOtherRoom = new ArrayList<>();

            for (int y = r.y; y < (r.y + r.height); y++) {
                if (y == r.y || y == (r.y + r.height - 1)) {
                    for (int x = r.x + 1; x < (r.x + r.width - 1); x++) {
                        if ((map[y - 1][x] == '=' && map[y + 1][x] == '.')
                                || (map[y + 1][x] == '=' && map[y - 1][x] == '.')) {

                            toOutside.add(new Vertex(x, y));
                        } else if ((map[y - 1][x] == '#' && map[y + 1][x] == '.')
                                || (map[y + 1][x] == '#' && map[y - 1][x] == '.')) {

                            toOtherRoom.add(new Vertex(x, y));
                        }
                    }
                } else {
                    if (map[y][r.x - 1] == '=' && map[y][r.x + 1] == '.') {
                        toOutside.add(new Vertex(r.x, y));
                    } else if (map[y][r.x - 1] == '#' && map[y][r.x + 1] == '.') {
                        toOtherRoom.add(new Vertex(r.x, y));
                    }
                    if (map[y][r.x + r.width - 2] == '.' && map[y][r.x + r.width] == '=') {
                        toOutside.add(new Vertex(r.x + r.width - 1, y));
                    } else if (map[y][r.x + r.width - 2] == '.' && map[y][r.x + r.width] == '#') {
                        toOtherRoom.add(new Vertex(r.x + r.width - 1, y));
                    }
                }
            }
            toOtherRooms.add(toOtherRoom);
            toOutsideConnections.add(toOutside);
        }

        //mark room to room connections
        for (List<Vertex> roomConnections : toOtherRooms) {
            for (Vertex c : roomConnections) {
                map[c.y][c.x] = '~';
            }
        }

        //resolve connections and create random doors
        for (int i = 0; i < toOutsideConnections.size(); i++) {
            if (!toOutsideConnections.get(i).isEmpty()) {
                List<Vertex> connections = toOutsideConnections.get(i);
                Vertex door;
                if (connections.size() == 1) {
                    door = connections.get(0);
                } else {
                    door = connections.get(rand.nextInt(connections.size()));
                }
                map[door.y][door.x] = '+';
            }
            if (!toOtherRooms.isEmpty()) {
                for (Vertex c : toOtherRooms.get(i)) {
                    if (map[c.y + 1][c.x] != '#'
                            && map[c.y - 1][c.x] != '#'
                            && map[c.y][c.x + 1] != '#'
                            && map[c.y][c.x - 1] != '#'
                            ) {

                        map[c.y][c.x] = '.';
                    } else {
                        map[c.y][c.x] = '~';
                    }
                }
            }
        }

        generateTrees(',', 'o', 25);
        generateTrees('=', 'o', 5);

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                switch (map[y][x]) {
                    case '~':
                        map[y][x] = '#';
                        break;
                    case '=':
                        map[y][x] = ',';
                        break;
                    default:
                        break;
                }
            }
        }

        //Create wall, gates and paths
        List<Vertex>[] possibleGates = new ArrayList[4];
        for (int i = 0; i < 4; i++) {
            possibleGates[i] = new ArrayList<>();
        }

        for (int y = boundsMinY - 2; y < (boundsMaxY + 2); y++) {
            if (y == boundsMinY - 2 || y == (boundsMaxY + 1)) {
                for (int x = boundsMinX - 2; x < (boundsMaxX + 2); x++) {
                    map[y][x] = '|';
                    if (y == boundsMinY - 2 && x > boundsMinX && x < (boundsMaxX - 1)) {
                        possibleGates[2].add(new Vertex(x, y));
                    } else if (x > boundsMinX && x < (boundsMaxX - 1)) {
                        possibleGates[3].add(new Vertex(x, y));
                    }
                }
            } else {
                map[y][boundsMinX - 2] = '|';
                map[y][boundsMaxX + 1] = '|';
                if (y > boundsMinY && y < (boundsMaxY - 1)) {
                    possibleGates[0].add(new Vertex(boundsMaxX + 1, y));
                    possibleGates[1].add(new Vertex(boundsMinX - 2, y));
                }
            }
        }

        Vertex gate = possibleGates[0].get(rand.nextInt(possibleGates[0].size()));
        map[gate.y - 1][gate.x] = '-';
        map[gate.y][gate.x] = '-';
        map[gate.y + 1][gate.x] = '-';
        for (int y = gate.y - 2; y <= gate.y + 2; y++) {
            for (int x = gate.x + 1; x < width; x++) {
                map[y][x] = '-';
            }
        }

        gate = possibleGates[1].get(rand.nextInt(possibleGates[1].size()));
        map[gate.y - 1][gate.x] = '-';
        map[gate.y][gate.x] = '-';
        map[gate.y + 1][gate.x] = '-';
        for (int y = gate.y - 2; y <= gate.y + 2; y++) {
            for (int x = gate.x - 1; x >= 0; x--) {
                map[y][x] = '-';
            }
        }

        gate = possibleGates[2].get(rand.nextInt(possibleGates[2].size()));
        map[gate.y][gate.x - 1] = '-';
        map[gate.y][gate.x] = '-';
        map[gate.y][gate.x + 1] = '-';
        for (int y = gate.y - 1; y >= 0; y--) {
            for (int x = gate.x - 2; x <= gate.x + 2; x++) {
                map[y][x] = '-';
            }
        }

        gate = possibleGates[3].get(rand.nextInt(possibleGates[3].size()));
        map[gate.y][gate.x - 1] = '-';
        map[gate.y][gate.x] = '-';
        map[gate.y][gate.x + 1] = '-';
        for (int y = gate.y + 1; y < height; y++) {
            for (int x = gate.x - 2; x <= gate.x + 2; x++) {
                map[y][x] = '-';
            }
        }

        //offset by 2 to account for the wall
        return new CampData(map, minimumPartitions, new Region(boundsMinX - 2, boundsMinY - 2, boundsMaxX - boundsMinX + 2, boundsMaxY - boundsMinY + 2), seed);
    }

    private void generateTrees(char surroundings, char tree, int percent) {
        for (int y = 1; y < map.length - 1; y++) {
            for (int x = 1; x < map[y].length - 1; x++) {
                if (map[y][x] == surroundings
                        && map[y + 1][x] == surroundings
                        && map[y - 1][x] == surroundings
                        && map[y][x + 1] == surroundings
                        && map[y][x - 1] == surroundings
                        && map[y + 1][x + 1] == surroundings
                        && map[y + 1][x - 1] == surroundings
                        && map[y - 1][x + 1] == surroundings
                        && map[y - 1][x - 1] == surroundings
                        && rand.nextInt(100) < percent) {

                    map[y][x] = tree;
                }
            }
        }
    }
}