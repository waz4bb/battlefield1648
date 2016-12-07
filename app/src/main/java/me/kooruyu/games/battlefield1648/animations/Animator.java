package me.kooruyu.games.battlefield1648.animations;


public interface Animator {

    void addListener(Animatable animatable);

    void dispatchUpdate();

    boolean isRunning();

    Object getAnimatedValue();
}
