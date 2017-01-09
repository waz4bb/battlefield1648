package me.kooruyu.games.battlefield1648.drawables;


import android.graphics.Paint;

public class OpaqueSquare extends Square {

    public OpaqueSquare(int x, int y, int width, Paint paint) {
        super(x, y, width, paint);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}
