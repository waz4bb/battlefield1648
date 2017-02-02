package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;
import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class Enemy extends MovableEntity implements Animatable {

    public static int MOVEMENT_SOUND = 35;
    public static int LOUD_NOISE_THRESHOLD = 25;
    public static int ALERTED_NOISE_LEVEL = 40;

    private static int ALERT_COOLDOWN = 6;


    public final int ID;

    private List<Vertex> path;
    private List<Vertex> pathSnippet;
    private int lastPathIndex;

    private AlertStatus status;
    private boolean isDead;
    private boolean isHeard;
    private int alertTimer;
    private SearchState lostPlayerState;

    private boolean isStopped;

    private Paint firstPaint;
    private Paint secondPaint;

    private Set<Integer> discoveredBodies;

    public Enemy(Vertex location, int squareWidth, Map<Direction, Bitmap> characterImages, Paint paint, Paint secondPaint, int id) {
        this(location.x, location.y, squareWidth, characterImages, paint, secondPaint, id);
    }

    public Enemy(int x, int y, int squareWidth, Map<Direction, Bitmap> characterImages, Paint paint, Paint secondPaint, int id) {
        super(x, y, squareWidth, Direction.ALL, characterImages, paint);

        ID = id;

        path = null;
        pathSnippet = null;

        status = AlertStatus.IDLE;
        isDead = false;
        isHeard = false;
        lastPathIndex = -1;

        this.firstPaint = paint;
        this.secondPaint = secondPaint;

        isStopped = false;
        alertTimer = 0;

        lostPlayerState = null;
        discoveredBodies = new HashSet<>();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //TODO: make size dynamic
        if (isHeard || isVisible()) {
            canvas.drawRect(
                    getScreenLocation().x - (squareWidth / 2), getScreenLocation().y - (squareWidth / 2),
                    getScreenLocation().x + (squareWidth / 2), getScreenLocation().y + (squareWidth / 2),
                    (isDead) ? getPaint() : (isVisible()) ? firstPaint : secondPaint);

            drawCharacterImage(canvas);
        }
    }

    public void markHeard(boolean heard) {
        isHeard = heard;
    }

    public void setPathSnippet(List<Vertex> pathSnippet) {
        this.pathSnippet = pathSnippet;
    }

    public List<Vertex> getPathSnippet() {
        return pathSnippet;
    }

    public void setPath(List<Vertex> path) {
        this.path = path;
        lastPathIndex = (path == null) ? -1 : 0;
    }

    public List<Vertex> getPath() {
        return path;
    }

    public boolean hasPath() {
        return path != null;
    }

    public boolean hasTraversed() {
        return lastPathIndex == -1 || lastPathIndex >= path.size() || (lastPathIndex + status.movementSpeed) > path.size();
    }

    public void increasePathIndexBy(int steps) {
        lastPathIndex += steps;
    }

    public int getLastPathIndex() {
        return lastPathIndex;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public boolean isCooledDown() {
        if (status == AlertStatus.SEARCHING) {
            if (alertTimer >= ALERT_COOLDOWN) {
                alertTimer = 0;
                return true;
            }
            alertTimer++;
        }
        return false;
    }

    public void startSearch(Direction previousPlayerDirection, Random rand) {
        lostPlayerState = new SearchState(previousPlayerDirection, rand);
    }

    public void stopSearch() {
        lostPlayerState = null;
    }

    public SearchState getSearchState() {
        return lostPlayerState;
    }

    public void kill() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    public void discoverBody(int enemyID) {
        if (!discoveredBodies.add(enemyID)) {
            status = AlertStatus.SEARCHING;
        }
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        setScreenLocation((Vertex) animator.getAnimatedValue());
    }


    public class SearchState {
        public static final int FOLLOWING_DIRECTION = 0;
        public static final int LOOKING = 1;
        public static final int BACK_TO_ALERT = 2;

        private int followingLength;
        private int lookingLength;

        public final Direction previousPlayerDirection;

        private int index;

        private SearchState(Direction previousPlayerDirection, Random rand) {
            index = 0;
            this.previousPlayerDirection = previousPlayerDirection;

            followingLength = rand.nextInt(4) + 2;
            lookingLength = rand.nextInt(2) + 1;
        }

        public int getState() {
            if (index < followingLength) return FOLLOWING_DIRECTION;
            if (index < lookingLength) return LOOKING;
            return BACK_TO_ALERT;
        }

        public void advance() {
            index++;
        }
    }
}
