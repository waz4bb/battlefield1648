package me.kooruyu.games.battlefield1648.cartography;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

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
        return null;
    }
}

