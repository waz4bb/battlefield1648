package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

public class TextureSquare extends OpaqueSquare {

    private Bitmap texture;

    public TextureSquare(int x, int y, int width, Bitmap texture, Paint paint) {
        super(x, y, width, paint);
        this.texture = texture;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(texture, getX(), getY(), getPaint());
    }
}
