package me.kooruyu.games.battlefield1648.animations;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SequentialListAnimator implements Animator {
    private Queue<List<Animatable>> animatables;
    private Queue<AnimationScheduler> animations;

    public SequentialListAnimator() {
        animatables = new LinkedList<>();
        animations = new LinkedList<>();
    }

    public void addAnimationLevel(AnimationScheduler animationLevel, List<Animatable> listeners) {
        animatables.offer(listeners);
        animations.offer(animationLevel);
    }

    @Override
    public void addListener(Animatable animatable) {
        throw new UnsupportedOperationException("Listeners should be added for each level not for the whole animator");
    }

    @Override
    public void dispatchUpdate() {
        if (animations.isEmpty()) return;

        animations.peek().refreshFrame();

        for (Animatable a : animatables.peek()) {
            a.onAnimationUpdate(this);
        }

        if (animations.peek().ended()) {
            animations.poll();
            animatables.poll();
        }
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
