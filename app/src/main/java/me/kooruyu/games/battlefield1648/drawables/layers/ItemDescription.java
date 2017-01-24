package me.kooruyu.games.battlefield1648.drawables.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import me.kooruyu.games.battlefield1648.cartography.Vertex;
import me.kooruyu.games.battlefield1648.drawables.Hexagon;
import me.kooruyu.games.battlefield1648.drawables.TextDrawable;
import me.kooruyu.games.battlefield1648.events.EventCallable;

public class ItemDescription extends Drawable implements EventCallable {

    public static final String EVENT_ID = "item";

    private static final int NUMBER_OF_ELEMENTS = 3;

    private final LayerDrawable layer;
    private final Layout descriptionLayout;
    private final Vertex descriptionStart;
    private TextDrawable itemName;

    private float width;
    private float height;

    public ItemDescription(String item, String descriptionText, float left, float top, float right, float bottom, Paint hexPaint) {
        Drawable[] elements = new Drawable[NUMBER_OF_ELEMENTS];


        width = right - left;
        height = bottom - top;

        float centerY = height / 2;

        Hexagon itemHex = new Hexagon((float) (left + (Math.sqrt(3) * (centerY / 2))), top + centerY, centerY, hexPaint);

        int containerStrokeSize = 10; //TODO: make this value dynamic eventually

        Rect descriptionBounds = new Rect(
                (int) (left + centerY + (centerY / 2)), (int) (top + (centerY * .1)),
                (int) right, (int) (bottom - (centerY * .1))
        );

        GradientDrawable descriptionContainer = new GradientDrawable();
        descriptionContainer.setBounds(descriptionBounds);

        descriptionContainer.setColor(Color.WHITE);
        descriptionContainer.setStroke(containerStrokeSize, Color.DKGRAY);

        Paint namePaint = new Paint(Color.BLACK);
        namePaint.setTextSize(centerY / 5);
        namePaint.setTextAlign(Paint.Align.CENTER);

        //TODO: optimize positioning e.g. with different text sizes and text lengths
        itemName = new TextDrawable(
                item, itemHex.getCenterX(),
                itemHex.getCenterY() + (namePaint.getTextSize() / 3),
                namePaint
        );


        TextPaint descriptionPaint = new TextPaint();
        descriptionPaint.setColor(Color.BLACK);
        descriptionPaint.setTextAlign(Paint.Align.LEFT);
        descriptionPaint.setTextSize(centerY / 10);

        this.descriptionLayout = new StaticLayout(descriptionText, descriptionPaint, (int) ((descriptionContainer.getBounds().width() - containerStrokeSize) * .99), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        descriptionStart = new Vertex(descriptionContainer.getBounds().left + containerStrokeSize, descriptionContainer.getBounds().top + containerStrokeSize);

        /*
        TextDrawable descriptionText = new TextDrawable(
                description, descriptionContainer.getBounds().left + containerStrokeSize,
                descriptionContainer.getBounds().top + containerStrokeSize + descriptionPaint.getTextSize(),
                descriptionPaint
        );
        */

        elements[0] = itemHex;
        elements[1] = itemName;
        elements[2] = descriptionContainer;

        layer = new LayerDrawable(elements);
    }

    public Vertex getDescriptionOffset() {
        return descriptionStart;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isVisible()) {
            layer.draw(canvas);
        }
    }

    public void drawText(@NonNull Canvas canvas) {
        descriptionLayout.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        layer.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        layer.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return layer.getOpacity();
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    @Override
    public void setActive(boolean active) {
        setVisible(active, false);
    }

    @Override
    public void trigger() {
        setVisible(!isVisible(), false);
    }

    @Override
    public Bundle getMetadata() {
        Bundle metadata = new Bundle();
        metadata.putString("ID", EVENT_ID);
        metadata.putString("ITEM", itemName.toString());
        return metadata;
    }
}