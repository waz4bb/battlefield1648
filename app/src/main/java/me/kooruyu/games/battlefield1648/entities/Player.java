package me.kooruyu.games.battlefield1648.entities;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;
import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class Player extends MovableEntity implements Animatable, Serializable {

    public static final int SHOOTING_NOISE = 85;
    public static final int FOV_SIZE = 40;
    public static final int PISTOL_RANGE = 5;

    private final int MAX_BULLETS = 4;
    private final int RELOAD_TIME = 5;

    private int bulletsLeft;
    private int reloadTimer;

    private Set<Vertex> shootArch;
    private Set<Vertex> movablePositions;

    private Direction lastDirection;

    public Player(Vertex location, int squareWidth, Map<Direction, Bitmap> characterImages, Paint paint) {
        this(location.x, location.y, squareWidth, characterImages, paint);
    }

    public Player(int x, int y, int squareWidth, Map<Direction, Bitmap> characterImages, Paint paint) {
        super(x, y, squareWidth, Direction.ALL, characterImages, paint);
        shootArch = null;
        bulletsLeft = MAX_BULLETS;
        reloadTimer = 0;
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

    public boolean canShoot() {
        return reloadTimer == 0 && bulletsLeft > 0;
    }

    public boolean shoot() {
        if (reloadTimer == 0 && bulletsLeft > 0) {
            bulletsLeft--;
            reloadTimer = RELOAD_TIME;
            return true;
        }
        return false;
    }

    public void reload(boolean instant) {
        reloadTimer = (reloadTimer == 0 || instant) ? 0 : reloadTimer - 1;
    }

    public Direction getDirection() {
        return lastDirection;
    }

    @Override
    public void moveTo(int x, int y) {
        super.moveTo(x, y);
        lastDirection = Direction.getDirection(getPreviousX(), getPreviousY(), getX(), getY());
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawCircle(getScreenLocation().x, getScreenLocation().y, squareWidth / 2, getPaint());
        //drawCharacterImage(canvas);
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }
}
