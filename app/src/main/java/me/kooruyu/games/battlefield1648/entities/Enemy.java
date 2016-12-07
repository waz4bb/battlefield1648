package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Enemy extends MovableEntity implements Animatable {

    public static final int IDLE = 0;
    public static final int SEARCHING = 1;

    private Set<Vertex> fieldOfView;
    private List<Vertex> path;
    private List<Vertex> pathSnippet;
    private int lastPathIndex;

    private int status;
    private boolean isDead;

    public Enemy(int x, int y, Paint paint) {
        super(x, y, paint);

        fieldOfView = null;
        path = null;
        pathSnippet = null;

        status = IDLE;
        isDead = false;
        lastPathIndex = -1;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawRect(
                getScreenLocation().getX() - 20, getScreenLocation().getY() - 20,
                getScreenLocation().getX() + 20, getScreenLocation().getY() + 20
                , getPaint());
    }

    public Set<Vertex> getFieldOfView() {
        return fieldOfView;
    }

    public boolean hasFieldOfView() {
        return fieldOfView != null;
    }

    public void setFieldOfView(Set<Vertex> fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public void setPathSnippet(List<Vertex> pathSnippet) {
        this.pathSnippet = pathSnippet;
    }

    public List<Vertex> getPathSnippet() {
        return pathSnippet;
    }

    public void setPath(List<Vertex> path) {
        this.path = path;
        lastPathIndex = (path == null) ? -1 : 0;
    }

    public List<Vertex> getPath() {
        return path;
    }

    public boolean hasPath() {
        return path != null;
    }

    public boolean hasTraversed() {
        return lastPathIndex == -1 || lastPathIndex >= path.size();
    }

    public void increasePathIndexBy(int steps) {
        lastPathIndex += steps;
    }

    public int getLastPathIndex() {
        return lastPathIndex;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void kill() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }

    /*
     * TODO: add enemy features:
     * 2. rotation
     * 3. pathfinding state
     */
}
