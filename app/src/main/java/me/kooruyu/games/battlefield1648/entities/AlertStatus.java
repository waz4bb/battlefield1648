package me.kooruyu.games.battlefield1648.entities;


import java.util.Random;

public enum AlertStatus {

    IDLE(4, 35),
    SEARCHING(5, 5),
    FOLLOWING(6, 0);

    private static final Random stopRng = new Random();

    private final int stoppingChance;
    public final int movementSpeed;

    AlertStatus(int movementSpeed, int stoppingChance) {
        this.movementSpeed = movementSpeed;
        this.stoppingChance = stoppingChance;
    }

    public boolean stops(boolean stoppedBefore) {
        //if there was a stop before make another stop less likely by decreasing the chance
        return !(stoppedBefore && stopRng.nextInt(100) >= (stoppingChance * 2))
                && stopRng.nextInt(100) < stoppingChance;
    }
}
