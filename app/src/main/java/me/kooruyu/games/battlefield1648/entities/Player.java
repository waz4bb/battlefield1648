package me.kooruyu.games.battlefield1648.entities;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.util.Set;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class Player extends MovableEntity implements Animatable {

    private Set<Vertex> shootArch;
    private Set<Vertex> movablePositions;

    public Player(int x, int y, Paint paint) {
        super(x, y, paint);
        shootArch = null;
    }

    public void setShootArch(Set<Vertex> shootArch) {
        this.shootArch = shootArch;
    }

    public Set<Vertex> getShootArch() {
        return shootArch;
    }

    public Set<Vertex> getMovablePositions() {
        return movablePositions;
    }

    public void setMovablePositions(Set<Vertex> movablePositions) {
        this.movablePositions = movablePositions;
    }

    public boolean canMoveTo(int x, int y) {
        return !(getX() == x && getY() == y) //return false if the target is the same as the current position
                && movablePositions.contains(new Vertex(x, y)); //check if the target is in reach of the precomputed movable area
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawCircle(getScreenLocation().x, getScreenLocation().y, 20, getPaint());
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }
}
