package me.kooruyu.games.battlefield1648.animations;

import android.animation.TypeEvaluator;


public class AnimationScheduler {
    private TypeEvaluator evaluator;
    private Object currentValue;
    private Object start;
    private Object end;
    private int totalFrames;
    private int framesLeft;

    public AnimationScheduler(TypeEvaluator evaluator, Object start, Object end, int frames) {
        this.evaluator = evaluator;
        this.start = start;
        this.end = end;
        this.totalFrames = frames;
        framesLeft = frames;
        currentValue = start;
    }

    protected boolean ended() {
        return framesLeft == 0;
    }

    /**
     * Calculates one frame of the animation
     *
     * @return true if the animation ended
     */
    protected boolean refreshFrame() {
        //on the last frame make sure to deliver accurate end state
        if (--framesLeft == 0) {
            currentValue = start;
            return true;
        }
        currentValue = evaluator.evaluate((float) framesLeft / totalFrames, start, end);
        return false;
    }

    protected Object getAnimatedValue() {
        return currentValue;
    }
}
