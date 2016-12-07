package me.kooruyu.games.battlefield1648.animations;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SequentialAnimator implements Animator {
    private List<Animatable> animatables;
    private Queue<AnimationScheduler> animations;

    public SequentialAnimator() {
        animatables = new ArrayList<>();
        animations = new LinkedList<>();
    }

    public void addAnimator(AnimationScheduler animation) {
        animations.offer(animation);
    }

    public void addAnimatorSequence(List<AnimationScheduler> animations) {
        this.animations.addAll(animations);
    }

    @Override
    public void addListener(Animatable animatable) {
        animatables.add(animatable);
    }

    @Override
    public void dispatchUpdate() {
        if (animations.isEmpty()) return;

        animations.peek().refreshFrame();

        for (Animatable a : animatables) {
            a.onAnimationUpdate(this);
        }

        if (animations.peek().ended()) animations.poll();
    }

    @Override
    public boolean isRunning() {
        return !animations.isEmpty();
    }

    @Override
    public Object getAnimatedValue() {
        return animations.peek().getAnimatedValue();
    }
}
