package me.kooruyu.games.battlefield1648.cartography;


public enum Direction {

    NORTH(new int[][]{
            {1, 0, 0, -1},
            {0, 1, -1, 0},
            {0, 1, 1, 0},
            {1, 0, 0, 1}
    }),
    SOUTH(new int[][]{
            {-1, 0, 0, 1},
            {0, -1, 1, 0},
            {0, -1, -1, 0},
            {-1, 0, 0, -1}
    }),
    EAST(new int[][]{
            {0, -1, -1, 0},
            {-1, 0, 0, -1},
            {1, 0, 0, -1},
            {0, 1, -1, 0}
    }),
    WEST(new int[][]{
            {1, 0, 0, 1},
            {0, 1, 1, 0},
            {0, 1, -1, 0},
            {1, 0, 0, -1}
    }),
    //All octant multipliers
    ALL(new int[][]{
            {1, 0, 0, -1, -1, 0, 0, 1},
            {0, 1, -1, 0, 0, -1, 1, 0},
            {0, 1, 1, 0, 0, -1, -1, 0},
            {1, 0, 0, 1, -1, 0, 0, -1}
    });

    public final int[][] octants;

    Direction(int[][] octants) {
        this.octants = octants;
    }

    public static Direction getDirection(int previousX, int previousY, int newX, int newY) {
        if (newX > previousX) {
            return EAST;
        }
        if (newX < previousX) {
            return WEST;
        }
        if (newY > previousY) {
            return SOUTH;
        }
        if (newY < previousY) {
            return NORTH;
        }
        //if the entity is static
        return ALL;
    }
}

