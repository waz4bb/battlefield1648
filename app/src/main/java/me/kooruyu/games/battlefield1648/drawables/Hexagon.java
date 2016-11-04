package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

public class Hexagon extends ShapeDrawable {

    private PathShape hexShape;
    private Paint paint;
    private float radius;
    private float centerX, centerY;
    private float width, height;

    public Hexagon(float centerX, float centerY, float radius, Paint paint) {
        super();
        this.paint = paint;
        calculateShape(centerX, centerY, radius);
    }

    public void calculateShape(float centerX, float centerY, float radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        float triangleHeight = (float) (Math.sqrt(3) * radius / 2);

        Path hexagonPath = new Path();

        hexagonPath.moveTo(centerX, centerY + radius);
        hexagonPath.lineTo(centerX - triangleHeight, centerY + radius / 2);
        hexagonPath.lineTo(centerX - triangleHeight, centerY - radius / 2);
        hexagonPath.lineTo(centerX, centerY - radius);
        hexagonPath.lineTo(centerX + triangleHeight, centerY - radius / 2);
        hexagonPath.lineTo(centerX + triangleHeight, centerY + radius / 2);
        hexagonPath.close();

        RectF size = new RectF();
        hexagonPath.computeBounds(size, false);

        width = size.width();
        height = size.height();

        hexShape = new PathShape(hexagonPath, width, height);
        setShape(hexShape);
        setBounds(0, 0, (int) width, (int) height);
        getPaint().set(paint);
    }

    public float getRadius() {
        return radius;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }
}
