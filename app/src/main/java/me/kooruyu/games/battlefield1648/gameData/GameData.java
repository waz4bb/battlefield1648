package me.kooruyu.games.battlefield1648.gameData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameData implements Serializable {
    public final int itemsTotal;
    public final long seed;

    private boolean failed;
    private String fate;

    private List<String> collectedItems;

    private int enemiesShot;
    private int timesSeen;
    private int numTurns;

    public GameData(int itemsTotal, long seed) {
        this.itemsTotal = itemsTotal;
        this.seed = seed;

        failed = false;
        fate = "";
        collectedItems = new ArrayList<>();

        enemiesShot = timesSeen = 0;
    }

    public void fail() {
        failed = true;
    }

    public boolean hasFailed() {
        return failed;
    }

    public void addShot() {
        enemiesShot++;
    }

    public int getEnemiesShot() {
        return enemiesShot;
    }

    public void addSeen() {
        timesSeen++;
    }

    public int getTimesSeen() {
        return timesSeen;
    }

    public void addTurn() {
        numTurns++;
    }

    public int getNumTurns() {
        return numTurns;
    }

    public void setFate(String fate) {
        this.fate = fate;
    }

    public String getFate() {
        return fate;
    }

    public void collectItem(String item) {
        collectedItems.add(item);
    }

    public List<String> getCollectedItems() {
        return collectedItems;
    }

    @Override
    public String toString() {
        return "GameResult:" +
                "\nitemsTotal: " + itemsTotal +
                "\nseed: " + seed +
                "\nfailed: " + failed +
                "\nfate: " + fate +
                "\nnumTurns: " + numTurns +
                "\ncollectedItems: " + collectedItems +
                "\nenemiesShot: " + enemiesShot +
                "\ntimesSeen: " + timesSeen;
    }

}
