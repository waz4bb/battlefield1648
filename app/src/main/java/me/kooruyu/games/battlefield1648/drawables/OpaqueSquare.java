package me.kooruyu.games.battlefield1648.drawables;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

public class OpaqueSquare extends Square {

    public OpaqueSquare(int x, int y, int width, Paint paint) {
        super(x, y, width, paint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isVisible()) super.draw(canvas);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}
