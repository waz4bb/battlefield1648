package me.kooruyu.games.battlefield1648.drawables.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.drawables.TextDrawable;
import me.kooruyu.games.battlefield1648.events.EventObserver;


public class TextButton extends Drawable {

    private Paint paint;
    private TextDrawable text;
    private Rect rect;
    private EventObserver eventObserver;

    public TextButton(int x, int y, int width, int height, String text, Paint paint) {
        this.paint = paint;

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(height / 2);

        this.text = new TextDrawable(text, x + (width / 2), y + (height / 2), textPaint);
        rect = new Rect(x, y, x + width, y + height);
    }

    public boolean contains(int x, int y) {
        return rect.contains(x, y);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(rect, paint);
        text.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        text.setAlpha(i);
        paint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        text.setColorFilter(colorFilter);
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return text.getOpacity();
    }
}
