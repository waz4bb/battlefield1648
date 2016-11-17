package me.kooruyu.games.battlefield1648.layers;


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

    private int squareWidth;
    private int xSquares, ySquares;
    private int yOffset, xOffset;

    private Paint squarePaint;
    private Paint squareHlPaint;

    public GridMapDrawable(int xSquares, int ySquares, int width, int height) {
        //create squares array
        squares = new Square[xSquares * ySquares];
        this.xSquares = xSquares;
        this.ySquares = ySquares;

        //Paints
        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.STROKE);
        squareWidth = Math.min(width / xSquares, height / ySquares);

        squareHlPaint = new Paint();
        squareHlPaint.setColor(Color.argb(180, 3, 192, 60));

        mapGraph = new Graph();
        createMap(width, height);

        layer = new LayerDrawable(squares);
    }

    private void createMap(int width, int height) {
        Paint unmovablePaint = new Paint();
        unmovablePaint.setColor(Color.BLUE);

        //Grid Geometry
        int gridheight = ySquares * squareWidth;
        int gridwidth = xSquares * squareWidth;
        xOffset = (width - gridwidth) / 2;
        yOffset = (height - gridheight) / 2;

        setBounds(xOffset, yOffset, gridwidth + xOffset, gridheight + yOffset);

        //Populating the grid
        List<Vertex> vertexes = new ArrayList<>();

        for (int i = 0, y = 0, yPos = yOffset;
             yPos < gridheight + yOffset;
             yPos += squareWidth, y++
                ) {

            for (int x = 0, xPos = xOffset;
                 xPos < gridwidth + xOffset;
                 xPos += squareWidth, x++, i++
                    ) {

                Vertex v = new Vertex(x, y);
                vertexes.add(v);

                squares[i] = new Square(xPos, yPos, squareWidth, squarePaint);
                //TODO: Remove debug walls for production
                if (x % 2 != 0 && y % 4 < 3) {
                    squares[i].setMovable(false);
                    squares[i].setPaint(unmovablePaint);
                }
            }
        }

        //Calculating edges
        for (int i = 0, y = 0; i < vertexes.size(); y++) {
            for (int x = 0; x < xSquares; x++, i++) {
                List<Edge> currentEdges = new ArrayList<>();

                if (y > 0) {
                    if (getSquare(x, y - 1).isMovable())
                        currentEdges.add(new Edge(vertexes.get(i - xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0 && getSquare(x - 1, y - 1).isMovable()) {
                        currentEdges.add(new Edge(vertexes.get(i - xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1 && getSquare(x + 1, y - 1).isMovable()) {
                        currentEdges.add(new Edge(vertexes.get(i - xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (y < ySquares - 1) {
                    if (getSquare(x, y + 1).isMovable())
                        currentEdges.add(new Edge(vertexes.get(i + xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0 && getSquare(x - 1, y + 1).isMovable()) {
                        currentEdges.add(new Edge(vertexes.get(i + xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1 && getSquare(x + 1, y + 1).isMovable()) {
                        currentEdges.add(new Edge(vertexes.get(i + xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (x > 0 && getSquare(x - 1, y).isMovable()) {
                    currentEdges.add(new Edge(vertexes.get(i - 1), Edge.DEFAULT_WEIGHT));
                }
                if (x < xSquares - 1 && getSquare(x + 1, y).isMovable()) {
                    currentEdges.add(new Edge(vertexes.get(i + 1), Edge.DEFAULT_WEIGHT));
                }

                mapGraph.addVertex(vertexes.get(i), currentEdges);
            }
        }
    }

    public Square getSquare(int x, int y) {
        int index = (y * xSquares) + x;

        try {
            return squares[index];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.format(Locale.ENGLISH, "Square at position (%d|%d) doesn't exist!", x, y));
        }
    }

    public Vertex touchSquareAt(int x, int y) {
        x = (x - xOffset) / squareWidth;
        y = (y - yOffset) / squareWidth;
        int index = (y * xSquares) + x;

        if (index < 0 || index > squares.length - 1) return null;

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
