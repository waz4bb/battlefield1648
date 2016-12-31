package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;


public class Square extends Drawable {

    private Rect rect;
    private int x, y;
    private int middleX, middleY;
    private int width;
    private Paint paint;
    private boolean movable;

    public Square(int x, int y, int width, Paint paint) {
        this.x = x;
        this.y = y;
        this.width = width;
        middleX = x + (width / 2);
        middleY = y + (width / 2);

        rect = new Rect(x, y, x + width, y + width);

        this.paint = paint;
        movable = true;
    }

    public void setRect(int x, int y, int width) {
        //left top right bottom
        this.x = x;
        this.y = y;
        this.width = width;
        middleX = x + (width / 2);
        middleY = y + (width / 2);

        rect = new Rect(x, y, x + width, y + width);

    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public int getMiddleX() {
        return middleX;
    }

    public int getMiddleY() {
        return middleY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isMovable() {
        return movable;
    }

    public boolean isOpaque() {
        return false;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}