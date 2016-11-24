package me.kooruyu.games.battlefield1648.drawables.layers;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.Edge;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.drawables.Square;

public class GridMapDrawable extends Drawable {

    private Square[] squares;
    private LayerDrawable layer;

    private final Graph mapGraph;

    private final int originalSquareWidth;
    private int squareWidth;
    private int yOffset, xOffset;
    private int xSquares, ySquares;
    private int screenWidth, screenHeight;

    private Paint squarePaint;
    private Paint squareHlPaint;

    public GridMapDrawable(int xSquares, int ySquares, int width, int height) {
        //create squares array
        squares = new Square[xSquares * ySquares];
        this.xSquares = xSquares;
        this.ySquares = ySquares;
        screenWidth = width;
        screenHeight = height;

        //Paints
        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.STROKE);
        originalSquareWidth = squareWidth = Math.min(screenWidth / xSquares, screenHeight / ySquares);

        squareHlPaint = new Paint();
        squareHlPaint.setColor(Color.argb(180, 3, 192, 60));

        mapGraph = new Graph();
        createMap(screenWidth, screenHeight);

        layer = new LayerDrawable(squares);
    }

    private void createMap(int width, int height) {
        Paint unmovablePaint = new Paint();
        unmovablePaint.setColor(Color.BLUE);

        //Grid Geometry
        int gridHeight = ySquares * squareWidth;
        int gridWidth = xSquares * squareWidth;
        xOffset = (width - gridWidth) / 2;
        yOffset = (height - gridHeight) / 2;

        setBounds(xOffset, yOffset, gridWidth + xOffset, gridHeight + yOffset);

        //Populating the grid
        List<Vertex> vertices = new ArrayList<>();

        for (int i = 0, y = 0, yPos = yOffset;
             yPos < gridHeight + yOffset;
             yPos += squareWidth, y++
                ) {

            for (int x = 0, xPos = xOffset;
                 xPos < gridWidth + xOffset;
                 xPos += squareWidth, x++, i++
                    ) {

                Vertex v = new Vertex(x, y);
                vertices.add(v);

                squares[i] = new Square(xPos, yPos, squareWidth, squarePaint);
                //TODO: Remove debug walls for production
                if (x % 4 == 0 && y % 4 < 3) {
                    squares[i].setMovable(false);
                    squares[i].setPaint(unmovablePaint);
                }
            }
        }

        fillGraph(vertices);
    }

    /**
     * Calculate edges and fill the graph
     *
     * @param vertices the list of traversable verices
     */
    private void fillGraph(List<Vertex> vertices) {
        for (int i = 0, y = 0; i < vertices.size(); y++) {
            for (int x = 0; x < xSquares; x++, i++) {
                if (!getSquare(x, y).isMovable()) continue;

                List<Edge> currentEdges = new ArrayList<>();

                if (y > 0) {
                    if (getSquare(x, y - 1).isMovable())
                        currentEdges.add(new Edge(vertices.get(i - xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0 && getSquare(x - 1, y - 1).isMovable()) {
                        currentEdges.add(new Edge(vertices.get(i - xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1 && getSquare(x + 1, y - 1).isMovable()) {
                        currentEdges.add(new Edge(vertices.get(i - xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (y < ySquares - 1) {
                    if (getSquare(x, y + 1).isMovable())
                        currentEdges.add(new Edge(vertices.get(i + xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0 && getSquare(x - 1, y + 1).isMovable()) {
                        currentEdges.add(new Edge(vertices.get(i + xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1 && getSquare(x + 1, y + 1).isMovable()) {
                        currentEdges.add(new Edge(vertices.get(i + xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (x > 0 && getSquare(x - 1, y).isMovable()) {
                    currentEdges.add(new Edge(vertices.get(i - 1), Edge.DEFAULT_WEIGHT));
                }
                if (x < xSquares - 1 && getSquare(x + 1, y).isMovable()) {
                    currentEdges.add(new Edge(vertices.get(i + 1), Edge.DEFAULT_WEIGHT));
                }

                mapGraph.addVertex(vertices.get(i), currentEdges);
            }
        }
    }

    public void moveZoomed(int xOffset, int yOffset) {
        this.xOffset += xOffset;
        this.yOffset += yOffset;

        scaleMap(this.xOffset, this.yOffset);
    }

    public void setZoomFactor(float zoomFactor) {
        squareWidth = (int) (originalSquareWidth * zoomFactor);

        int gridHeight = ySquares * squareWidth;
        int gridWidth = xSquares * squareWidth;
        xOffset = (screenWidth - gridWidth) / 2;
        yOffset = (screenHeight - gridHeight) / 2;

        if (xOffset < 0) xOffset = 0;
        if (yOffset < 0) yOffset = 0;

        scaleMap(gridHeight, gridWidth, xOffset, yOffset);
    }

    private void scaleMap(int xOffset, int yOffset) {
        scaleMap(ySquares * squareWidth, xSquares * squareWidth, xOffset, yOffset);
    }

    private void scaleMap(int gridHeight, int gridWidth, int xOffset, int yOffset) {
        for (int i = 0, y = 0, yPos = yOffset;
             yPos < gridHeight + yOffset;
             yPos += squareWidth, y++
                ) {

            for (int x = 0, xPos = xOffset;
                 xPos < gridWidth + xOffset;
                 xPos += squareWidth, x++, i++
                    ) {

                squares[i].setRect(xPos, yPos, squareWidth);
            }
        }

        setBounds(xOffset, yOffset, gridWidth + xOffset, gridHeight + yOffset);
    }

    public Square getSquare(int x, int y) {
        int index = (y * xSquares) + x;

        try {
            return squares[index];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.format(
                    Locale.ENGLISH,
                    "Index doesn't point to a valid square. Out of range by %d at (%d|%d)",
                    index - squares.length - 1, x, y
            ));
        }
    }

    public Vertex getVertex(int x, int y) {
        x = Math.abs(x - xOffset) / squareWidth;
        y = Math.abs(y - yOffset) / squareWidth;
        int index = (y * xSquares) + x;

        if (index < 0 || index > squares.length - 1) {
            throw new IndexOutOfBoundsException(String.format(
                    Locale.ENGLISH,
                    "Index doesn't point to a valid square. Out of range by %d at (%d|%d)",
                    index - squares.length - 1, x, y
            ));
        }

        return new Vertex(x, y);
    }

    public void clearSquareBackgrounds(Set<Vertex> vertices) {
        for (Vertex v : vertices) {
            getSquare(v.getX(), v.getY()).disableBackground();
        }
    }

    public void drawSquareBackgrounds(Set<Vertex> vertices, Paint paint) {
        for (Vertex v : vertices) {
            getSquare(v.getX(), v.getY()).setBackground(paint);
        }
    }

    public Graph getMapGraph() {
        return mapGraph;
    }

    public Paint getSquarePaint() {
        return squarePaint;
    }

    public Paint getSquareHlPaint() {
        return squareHlPaint;
    }

    public int getSquareWidht() {
        return squareWidth;
    }

    public int getxSquares() {
        return xSquares;
    }

    public int getySquares() {
        return ySquares;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        layer.draw(canvas);
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
}
