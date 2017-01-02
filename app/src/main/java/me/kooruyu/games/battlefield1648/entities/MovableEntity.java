package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Vertex;

public abstract class MovableEntity extends Drawable {
    public static int MOVEMENT_SOUND = 25;

    private int x, y;
    private int previousX, previousY;
    private Vertex screenLocation;
    private Paint paint;
    private Set<Vertex> fieldOfView;

    public MovableEntity(Vertex location, Paint paint) {
        this(location.x, location.y, paint);
    }

    public MovableEntity(int x, int y, Paint paint) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.paint = paint;

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
