package me.kooruyu.games.battlefield1648.entities;


import android.graphics.Canvas;
import android.graphics.Paint;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Player extends MovableEntity implements Animatable {

    public Player(int x, int y, Paint paint) {
        super(x, y, paint);
    }

    @Override
    public void draw(Canvas canvas) {
        //TODO: make size dynamic
        canvas.drawCircle(getScreenLocation().getX(), getScreenLocation().getY(), 20, getPaint());
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }
}
