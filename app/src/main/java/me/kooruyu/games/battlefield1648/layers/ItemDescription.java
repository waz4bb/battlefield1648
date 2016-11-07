package me.kooruyu.games.battlefield1648.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;

import me.kooruyu.games.battlefield1648.drawables.Hexagon;
import me.kooruyu.games.battlefield1648.drawables.TextDrawable;
import me.kooruyu.games.battlefield1648.events.EventCallable;

public class ItemDescription extends Drawable implements EventCallable {

    private LayerDrawable layer;
    private TextDrawable itemName;
    private TextDrawable descriptionText;
    private Hexagon itemHex;
    private GradientDrawable descriptionContainer;
    private GradientDrawable levelStartButton;

    private float width;
    private float height;

    public ItemDescription(String item, String description, float left, float top, float right, float bottom) {
        Drawable[] elements = new Drawable[5];

        width = right - left;
        height = bottom - top;

        Paint hexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hexPaint.setColor(Color.CYAN);
        hexPaint.setStyle(Paint.Style.FILL);

        float centerY = height / 2;

        itemHex = new Hexagon((float) (left + (Math.sqrt(3) * (centerY / 2))), top + centerY, centerY, hexPaint);

        int containerStrokeSize = 10; //TODO: make this value dynamic eventually

        Rect descriptionBounds = new Rect(
                (int) (left + centerY + (centerY / 2)), (int) (top + (centerY * .1)),
                (int) right, (int) (bottom - (centerY * .7))
        );

        descriptionContainer = new GradientDrawable();
        descriptionContainer.setBounds(descriptionBounds);

        descriptionContainer.setColor(Color.WHITE);
        descriptionContainer.setStroke(containerStrokeSize, Color.DKGRAY);

        levelStartButton = new GradientDrawable();
        levelStartButton.setBounds(
                (int) (descriptionBounds.right - (descriptionBounds.width() * .8)), (int) (bottom - (centerY * .6)),
                descriptionBounds.right, (int) (bottom - (centerY * .1))
        );

        levelStartButton.setColor(Color.WHITE);
        levelStartButton.setStroke(containerStrokeSize, Color.DKGRAY);

        Paint namePaint = new Paint(Color.BLACK);
        namePaint.setTextSize(centerY / 5);
        namePaint.setTextAlign(Paint.Align.CENTER);

        //TODO: optimize positioning e.g. with different text sizes and text lengths
        itemName = new TextDrawable(
                item, itemHex.getCenterX(),
                itemHex.getCenterY() + (namePaint.getTextSize() / 3),
                namePaint
        );


        Paint descriptionPaint = new Paint(Color.BLACK);
        descriptionPaint.setTextAlign(Paint.Align.LEFT);
        descriptionPaint.setTextSize(centerY / 10);

        descriptionText = new TextDrawable(
                description, descriptionContainer.getBounds().left + containerStrokeSize,
                descriptionContainer.getBounds().top + containerStrokeSize + descriptionPaint.getTextSize(),
                descriptionPaint
        );

        elements[0] = itemHex;
        elements[1] = itemName;
        elements[2] = descriptionContainer;
        elements[3] = descriptionText;
        elements[4] = levelStartButton;

        layer = new LayerDrawable(elements);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isVisible()) layer.draw(canvas);
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
}