package me.kooruyu.games.battlefield1648.animations;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Animator {
    private List<Animatable> animatables;
    private Queue<AnimationScheduler> animations;
    private boolean running;

    public Animator() {
        animatables = new ArrayList<>();
        animations = new LinkedList<>();
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

        animations.peek().refreshFrame();

        for (Animatable a : animatables) {
            a.onAnimationUpdate(this);
        }

        if (animations.peek().ended()) animations.poll();
    }

    public boolean isRunning() {
        return !animations.isEmpty();
    }

    public Object getAnimatedValue() {
        return animations.peek().getAnimatedValue();
    }
}
