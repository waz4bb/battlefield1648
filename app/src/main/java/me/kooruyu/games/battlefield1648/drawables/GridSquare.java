package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.Animator;


public class GridSquare extends Square implements Animatable {

    private boolean backgroundVisible;
    private Square background;
    private Bitmap baseTexture;
    private boolean textureRendering;

    public GridSquare(int x, int y, int width, Paint paint, Bitmap baseTexture) {
        super(x, y, width, paint);

        backgroundVisible = true;
        textureRendering = true;
        background = null;
        this.baseTexture = baseTexture;
    }

    @Override
    public void setRect(int x, int y, int width) {
        super.setRect(x, y, width);

        if (background != null) {
            background.setRect(x, y, width);
        }
    }

    public void setTextureRendering(boolean enable) {
        textureRendering = enable;
    }

    public void setBackground(Paint paint) {
        background = new Square(getX(), getY(), getWidth(), paint);
    }

    public void disableBackground() {
        background = null;
    }

    public void setBackgroundVisible(boolean visible) {
        backgroundVisible = visible;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (background != null && backgroundVisible) {
            background.draw(canvas);
        } else if (textureRendering && baseTexture != null) {
            canvas.drawBitmap(baseTexture, getX(), getY(), null);
        }
        super.draw(canvas);
    }

    @Override
    public void onAnimationUpdate(Animator animator) {
        backgroundVisible = (Boolean) animator.getAnimatedValue();
    }
}
