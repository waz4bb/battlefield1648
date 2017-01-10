package me.kooruyu.games.battlefield1648.drawables.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.drawables.TextDrawable;


public class TextButton extends Drawable {

    private Paint paint;
    private Paint textPaint;
    private TextDrawable text;
    private String textString;
    private Rect rect;
    private int width, height;
    private int x, y;

    public TextButton(int x, int y, int maxX, int maxY, String text, Paint paint) {
        this.paint = paint;

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textString = text;
        resize(x, y, maxX, maxY);

        width = maxX;
        height = maxY;
        this.x = x;
        this.y = y;
        setBounds(x, y, maxX, maxY);
    }

    public void resize(int x, int y, int maxX, int maxY) {
        textPaint.setTextSize((maxY - y) / 2);

        text = new TextDrawable(textString, x + ((maxX - x) / 2), y + ((maxY - y) / 2), textPaint);
        rect = new Rect(x, y, maxX, maxY);
    }

    public void move(int xOffset, int yOffset) {
        setBounds(x + xOffset, y + yOffset, xOffset + width, yOffset + height);
    }

    public boolean contains(int x, int y) {
        return getBounds().contains(x, y);
    }

    public void setText(String string) {
        textString = string;
        text.setText(textString);
    }

    public String getText() {
        return textString;
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
