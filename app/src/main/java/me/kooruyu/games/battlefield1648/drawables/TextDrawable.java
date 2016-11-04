package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class TextDrawable extends Drawable {

    private float x;
    private float y;
    private String text;
    private Paint paint;

    public TextDrawable(String text, float x, float y, Paint paint) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.paint = paint;
    }

    public int length() {
        return text.length();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawText(text, x, y, paint);
    }

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
