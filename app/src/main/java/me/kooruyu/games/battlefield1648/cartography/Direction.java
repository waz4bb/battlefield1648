package me.kooruyu.games.battlefield1648.cartography;


public enum Direction {

    NORTH(new int[][]{
            {1, 0, 0, -1},
            {0, 1, -1, 0},
            {0, 1, 1, 0},
            {1, 0, 0, 1}
    }),
    NORTH_WEST(new int[][]{
            {1, 0, 0, -1},
            {0, 1, 1, 0},
            {0, 1, -1, 0},
            {1, 0, 0, 1}
    }),
    NORTH_EAST(new int[][]{
            {-1, 1, 0, 0},
            {0, 0, -1, -1},
            {0, 0, 1, -1},
            {1, 1, 0, 0}
    }),
    SOUTH(new int[][]{
            {-1, 0, 0, 1},
            {0, -1, 1, 0},
            {0, -1, -1, 0},
            {-1, 0, 0, -1}
    }),
    SOUTH_WEST(new int[][]{
            {1, -1, 0, 0},
            {0, 0, 1, 1},
            {0, 0, -1, 1},
            {-1, -1, 0, 0}
    }),
    SOUTH_EAST(new int[][]{
            {-1, 0, 0, 1},
            {0, -1, -1, 0},
            {0, -1, 1, 0},
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
        if (newY < previousY) {
            if (newX < previousX) {
                return NORTH_WEST;
            }
            if (newX > previousY) {
                return NORTH_EAST;
            }
            return NORTH;
        }
        if (newY > previousY) {
            if (newX < previousX) {
                return SOUTH_WEST;
            }
            if (newX > previousY) {
                return SOUTH_EAST;
            }
            return SOUTH;
        }
        if (newX < previousX) {
            return WEST;
        }
        if (newX > previousX) {
            return EAST;
        }

        //if the entity is static
        return ALL;
    }
}

