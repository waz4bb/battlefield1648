package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;

public class Square extends Drawable implements Animatable {

    private Rect rect;
    private int x, y;
    private int middleX, middleY;
    private int width;
    private Paint paint;
    private Square background;
    private boolean movable;
    private boolean backgroundVisible;

    public Square(int x, int y, int width, Paint paint) {
        this.x = x;
        this.y = y;
        this.width = width;
        middleX = x + (width / 2);
        middleY = y + (width / 2);

        rect = new Rect(x, y, x + width, y + width);

        this.paint = paint;
        background = null;
        backgroundVisible = true;
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

        if (background != null) {
            background.setRect(x, y, width);
        }
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

    public void setBackgroundVisible(boolean visible) {
        backgroundVisible = visible;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (background != null && backgroundVisible) background.draw(canvas);
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

    @Override
    public void onAnimationUpdate(Animator animator) {
        backgroundVisible = (Boolean) animator.getAnimatedValue();
    }
}