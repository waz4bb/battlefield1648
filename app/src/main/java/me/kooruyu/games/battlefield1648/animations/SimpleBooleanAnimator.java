package me.kooruyu.games.battlefield1648.animations;

import android.animation.TypeEvaluator;


public class SimpleBooleanAnimator implements TypeEvaluator {

    @Override
    public Object evaluate(float v, Object start, Object end) {
        return (v == 1) ? end : start;
    }
}
