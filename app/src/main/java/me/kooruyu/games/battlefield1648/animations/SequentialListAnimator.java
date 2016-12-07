package me.kooruyu.games.battlefield1648.animations;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SequentialListAnimator implements Animator {
    private List<Animatable> animatables;
    private Queue<List<AnimationScheduler>> animations;

    public SequentialListAnimator() {
        animatables = new ArrayList<>();
        animations = new LinkedList<>();
    }

    public void addAnimationLevel(List<AnimationScheduler> animationLevel) {
        animations.offer(animationLevel);
    }

    @Override
    public void addListener(Animatable animatable) {
        animatables.add(animatable);
    }

    @Override
    public void dispatchUpdate() {
        if (animations.isEmpty()) return;

        for (AnimationScheduler a : animations.peek()) {
            a.refreshFrame();
        }

        for (Animatable a : animatables) {
            a.onAnimationUpdate(this);
        }

        if (animations.peek().get(0).ended()) animations.poll();
    }

    @Override
    public boolean isRunning() {
        return !animations.isEmpty();
    }

    @Override
    public Object getAnimatedValue() {
        return null;
    }
}
