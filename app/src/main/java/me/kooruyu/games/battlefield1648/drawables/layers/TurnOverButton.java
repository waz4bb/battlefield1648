package me.kooruyu.games.battlefield1648.drawables.layers;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.drawables.TextDrawable;

public class TurnOverButton extends Drawable {

    private ShapeDrawable buttonShape;
    private TextDrawable turnCounter;
    private int turns;
    private Paint paint;
    private RectF absoluteBounds;

    public TurnOverButton(float x, float y, float width, float height, Paint paint) {
        Path path = new Path();

        path.moveTo(x, y);
        path.lineTo(x + ((width / 4) * 3), y);
        path.lineTo(x + width, y + (height / 2));
        path.lineTo(x + ((width / 4) * 3), y + height);
        path.lineTo(x, y + height);
        path.close();

        absoluteBounds = new RectF();
        path.computeBounds(absoluteBounds, false);

        PathShape buttonPath = new PathShape(path, width, height);
        buttonShape = new ShapeDrawable(buttonPath);
        buttonShape.setBounds(0, 0, (int) width, (int) height);

        this.paint = paint;
        buttonShape.getPaint().set(paint);
        turns = 0;

        Paint counterPaint = new Paint(Color.BLACK);
        counterPaint.setTextSize(height / 2);
        counterPaint.setTextAlign(Paint.Align.CENTER);

        turnCounter = new TextDrawable("0", x + (width / 2), y + ((height / 3) * 2), counterPaint);
    }

    public boolean contains(int x, int y) {
        return absoluteBounds.contains(x, y);
    }

    public void addTurn() {
        turns++;
        turnCounter.setText(Integer.toString(turns));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        buttonShape.draw(canvas);
        turnCounter.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        buttonShape.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        buttonShape.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return buttonShape.getOpacity();
    }
}
