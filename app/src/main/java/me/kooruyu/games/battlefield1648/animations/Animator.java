package me.kooruyu.games.battlefield1648.animations;


import java.util.ArrayList;
import java.util.List;

public class Animator {
    private List<Animatable> animatables;
    private List<AnimationScheduler> animations;
    private boolean running;

    public Animator() {
        animatables = new ArrayList<>();
        animations = new ArrayList<>();
    }

    public void addListener(Animatable animatable) {
        animatables.add(animatable);
    }

    public void addAnimator(AnimationScheduler animation) {
        animations.add(animation);
    }

    public void addAnimatorSequence(List<AnimationScheduler> animations) {
        this.animations.addAll(animations);
    }

    public void dispatchUpdate() {
        if (animations.isEmpty()) return;

        animations.get(0).refreshFrame();

        for (Animatable a : animatables) {
            a.onAnimationUpdate(this);
        }

        if (animations.get(0).ended()) animations.remove(0);
    }

    public boolean isRunning() {
        return !animations.isEmpty();
    }

    public Object getAnimatedValue() {
        return animations.get(0).getAnimatedValue();
    }
}
