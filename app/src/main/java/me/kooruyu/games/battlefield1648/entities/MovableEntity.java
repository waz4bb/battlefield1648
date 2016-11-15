package me.kooruyu.games.battlefield1648.entities;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public abstract class MovableEntity extends Drawable {
    private int x, y;
    private int previousX, previousY;
    private Vertex screenLocation;
    private Paint paint;

    public MovableEntity(int x, int y, Paint paint) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
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
        moveTo(target.getX(), target.getY());
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
