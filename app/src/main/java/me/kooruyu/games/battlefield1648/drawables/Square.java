package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class Square extends Drawable {

    private final Rect rect;
    private final int x, y;
    private final int middleX, middleY;
    private final int width;
    private Paint paint;
    private Square background;
    private boolean movable;

    public Square(int x, int y, int width, Paint paint) {
        //left top right bottom
        this.x = x;
        this.y = y;
        this.width = width;
        rect = new Rect(x, y, x + width, y + width);
        middleX = x + (width / 2);
        middleY = y + (width / 2);
        this.paint = paint;
        background = null;
        movable = true;
    }

    public void setBackground(Paint paint) {
        background = new Square(x, y, width, paint);
    }

    public void disableBackground() {
        background = null;
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

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isMovable() {
        return movable;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (background != null) background.draw(canvas);
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