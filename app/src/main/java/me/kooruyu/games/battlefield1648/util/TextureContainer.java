package me.kooruyu.games.battlefield1648.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import me.kooruyu.games.battlefield1648.R;


public class TextureContainer {
    public final Bitmap tree;
    public final Bitmap wood;
    public final Bitmap woodenFloor;

    public TextureContainer(Resources resources, int width) {
        //load bitmaps
        wood = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.mipmap.wood), width, width, false);
        woodenFloor = createTintedBitmap(wood, 0xFF7F7F7F);
        tree = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.mipmap.tree), width, width, false);
    }

    private Bitmap createTintedBitmap(Bitmap src, int color) {
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas c = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new LightingColorFilter(color, 0));
        c.drawBitmap(src, 0, 0, paint);
        return result;
    }
}
