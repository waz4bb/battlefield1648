package me.kooruyu.games.battlefield1648.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class ZoomBar extends Drawable {
    private float minimumZoom, maximumZoom;
    private float currentZoomLevel;

    private float middleLineY;

    private Paint barPaint;

    public ZoomBar(int left, int top, int right, int bottom, float minimumZoom, float maximumZoom, float startingPoint) {
        this.minimumZoom = minimumZoom;
        this.maximumZoom = maximumZoom;
        this.currentZoomLevel = startingPoint;

        setBounds(left, top, right, bottom);

        middleLineY = top + (getBounds().height() / 2);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setColor(Color.WHITE);
        barPaint.setStrokeWidth(getBounds().height() / 8);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawLine(getBounds().left, getBounds().top, getBounds().left, getBounds().bottom, barPaint);
        canvas.drawLine(getBounds().right, getBounds().top, getBounds().right, getBounds().bottom, barPaint);

        canvas.drawLine(getBounds().left, middleLineY, getBounds().right, middleLineY, barPaint);

        float fillLevel = ((getBounds().width() / (maximumZoom - minimumZoom)) * (currentZoomLevel - minimumZoom));
        canvas.drawCircle(getBounds().left + fillLevel, middleLineY, getBounds().height() / 2, barPaint);
    }

    public void setZoomLevel(float zoomLevel) {
        currentZoomLevel = zoomLevel;
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
