package me.kooruyu.games.battlefield1648.entities;


import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Player implements Animatable {

    private int x, y;
    private int previousX, previousY;
    private Vertex screenLocation;

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

    public Vertex getPosition() {
        return new Vertex(x, y);
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

    public Vertex getScreenLocation() {
        return screenLocation;
    }

    public void setScreenLocation(Vertex screenLocation) {
        this.screenLocation = screenLocation;
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        screenLocation = (Vertex) animator.getAnimatedValue();
    }
}
