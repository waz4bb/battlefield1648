package me.kooruyu.games.battlefield1648.entities;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public class Player {

    private int x, y;
    private int previousX, previousY;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public void moveTo(Vertex target) {
        moveTo(target.getX(), target.getY());
    }

    public void moveTo(int x, int y) {
        previousX = this.x;
        previousY = this.y;
        this.x = x;
        this.y = y;
    }
}
