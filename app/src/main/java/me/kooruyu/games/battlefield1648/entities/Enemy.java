package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Enemy extends MovableEntity implements Animatable {

    private Set<Vertex> fieldOfView;
    private List<Vertex> path;

    public Enemy(int x, int y, Paint paint) {
        super(x, y, paint);
        fieldOfView = null;
        path = null;
    }

    @Override
    public void draw(Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawRect(
                getScreenLocation().getX() - 20, getScreenLocation().getY() - 20,
                getScreenLocation().getX() + 20, getScreenLocation().getY() + 20
                , getPaint());
    }

    public Set<Vertex> getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(Set<Vertex> fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public List<Vertex> getPath() {
        return path;
    }

    public void setPath(List<Vertex> path) {
        this.path = path;
    }

    public boolean hasFieldOfView() {
        return fieldOfView != null;
    }

    public boolean hasPath() {
        return path != null;
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
