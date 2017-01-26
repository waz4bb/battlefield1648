package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public abstract class MovableEntity extends Drawable {

    private static final Direction DEFAULT_LOOKING_DIRECTION = Direction.NORTH;

    private int x, y;
    private int previousX, previousY;
    private Vertex screenLocation;
    private Paint paint;
    private Set<Vertex> fieldOfView;
    private Direction direction;

    protected int squareWidth;

    private Map<Direction, Bitmap> characterImages;

    public MovableEntity(Vertex location, int squareWidth, Direction initialDirection, Map<Direction, Bitmap> characterImages, Paint paint) {
        this(location.x, location.y, squareWidth, initialDirection, characterImages, paint);
    }

    public MovableEntity(int x, int y, int squareWidth, Direction initialDirection, Map<Direction, Bitmap> characterImages, Paint paint) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.paint = paint;
        this.characterImages = characterImages;
        this.direction = initialDirection;
        this.squareWidth = squareWidth;

        fieldOfView = null;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Vertex getPosition() {
        return new Vertex(x, y);
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public void moveTo(Vertex target) {
        moveTo(target.x, target.y);
    }

    public void moveTo(int x, int y) {
        previousX = this.x;
        previousY = this.y;
        this.x = x;
        this.y = y;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Vertex getScreenLocation() {
        return screenLocation;
    }

    public void setScreenLocation(Vertex screenLocation) {
        this.screenLocation = screenLocation;
    }

    public Set<Vertex> getFieldOfView() {
        return fieldOfView;
    }

    public boolean hasFieldOfView() {
        return fieldOfView != null;
    }

    public boolean inFieldOfView(Vertex position) {
        return fieldOfView.contains(position);
    }

    public void setFieldOfView(Set<Vertex> fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    void drawCharacterImage(@NonNull Canvas canvas) {
        Direction movementDirection = direction;
        if (direction == Direction.ALL) {
            movementDirection = DEFAULT_LOOKING_DIRECTION;
        }
        canvas.drawBitmap(characterImages.get(movementDirection), screenLocation.x - (characterImages.get(direction).getWidth() / 2), screenLocation.y - (characterImages.get(direction).getHeight() / 2), null);
    }

    @Override
    public abstract void draw(@NonNull Canvas canvas);

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
