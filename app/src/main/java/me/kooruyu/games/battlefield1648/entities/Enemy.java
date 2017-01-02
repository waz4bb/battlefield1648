package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.util.List;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class Enemy extends MovableEntity implements Animatable {

    public static final int IDLE = 0;
    public static final int SEARCHING = 1;

    private List<Vertex> path;
    private List<Vertex> pathSnippet;
    private int lastPathIndex;

    private int status;
    private boolean isDead;
    private boolean isHeard;

    private Paint firstPaint;
    private Paint secondPaint;

    public Enemy(Vertex location, Paint paint, Paint secondPaint) {
        this(location.x, location.y, paint, secondPaint);
    }

    public Enemy(int x, int y, Paint paint, Paint secondPaint) {
        super(x, y, paint);

        path = null;
        pathSnippet = null;

        status = IDLE;
        isDead = false;
        isHeard = false;
        lastPathIndex = -1;

        this.firstPaint = paint;
        this.secondPaint = secondPaint;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        if (isHeard || isVisible()) {
            canvas.drawRect(
                    getScreenLocation().x - 20, getScreenLocation().y - 20,
                    getScreenLocation().x + 20, getScreenLocation().y + 20
                    , (isVisible()) ? firstPaint : secondPaint);
        }
    }

    public void markHeard(boolean heard) {
        isHeard = heard;
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
