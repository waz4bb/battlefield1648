package me.kooruyu.games.battlefield1648.entities;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Player extends MovableEntity implements Animatable {

    private Set<Vertex> shootArch;

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

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawCircle(getScreenLocation().getX(), getScreenLocation().getY(), 20, getPaint());
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }
}
