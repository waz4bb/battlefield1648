package me.kooruyu.games.battlefield1648.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.kooruyu.games.battlefield1648.algorithms.DijkstraPathfinder;
import me.kooruyu.games.battlefield1648.algorithms.Edge;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;

public class GridMap extends Drawable {

    private Square[] squares;
    private LayerDrawable layer;
    private Graph mapGraph;
    private DijkstraPathfinder pathfinder;

    private int squareWidht;
    private int xSquares, ySquares;
    private int yOffset, xOffset;

    private Paint squarePaint;

    public GridMap(int xSquares, int ySquares, int width, int height) {
        List<Vertex> vertexes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        //create squares array
        squares = new Square[xSquares * ySquares];
        this.xSquares = xSquares;
        this.ySquares = ySquares;

        //Paints
        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.STROKE);
        squareWidht = Math.min(width / xSquares, height / ySquares);

        //Grid Geometry
        int gridheight = ySquares * squareWidht;
        int gridwidth = xSquares * squareWidht;
        xOffset = (width - gridwidth) / 2;
        yOffset = (height - gridheight) / 2;

        setBounds(xOffset, yOffset, gridwidth + xOffset, gridheight + yOffset);

        //Populating the grid
        for (int i = 0, y = 0, yPos = yOffset;
             yPos < gridheight + yOffset;
             yPos += squareWidht, y++
                ) {

            for (int x = 0, xPos = xOffset;
                 xPos < gridwidth + xOffset;
                 xPos += squareWidht, x++, i++
                    ) {

                vertexes.add(new Vertex(x, y));
                squares[i] = new Square(xPos, yPos, squareWidht, squarePaint);
            }
        }

        //Calculating edges
        for (int i = 0, y = 0; i < vertexes.size(); y++) {
            for (int x = 0; x < xSquares; x++, i++) {
                if (y > 0) {
                    edges.add(new Edge(vertexes.get(i), vertexes.get(i - xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0) {
                        edges.add(new Edge(vertexes.get(i), vertexes.get(i - xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1) {
                        edges.add(new Edge(vertexes.get(i), vertexes.get(i - xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (y < ySquares - 1) {
                    edges.add(new Edge(vertexes.get(i), vertexes.get(i + xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0) {
                        edges.add(new Edge(vertexes.get(i), vertexes.get(i + xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1) {
                        edges.add(new Edge(vertexes.get(i), vertexes.get(i + xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (x > 0) {
                    edges.add(new Edge(vertexes.get(i), vertexes.get(i - 1), Edge.DEFAULT_WEIGHT));
                }
                if (x < xSquares - 1) {
                    edges.add(new Edge(vertexes.get(i), vertexes.get(i + 1), Edge.DEFAULT_WEIGHT));
                }
            }
        }

        mapGraph = new Graph(vertexes, edges);
        pathfinder = new DijkstraPathfinder(mapGraph);
        layer = new LayerDrawable(squares);
    }

    public Square getSquare(int x, int y) {
        int index = (y * xSquares) + x;

        try {
            return squares[index];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.format("Square at position (%d|%d) is doesn't exist!", x, y));
        }
    }

    public int getSquareWidht() {
        return squareWidht;
    }

    public Vertex touchSquareAt(int x, int y) {
        //get touched square
        x = (x - xOffset) / squareWidht;
        y = ((y - yOffset) / squareWidht);
        int index = (y * xSquares) + x;

        if (index < 0 || index > squares.length - 1) return null;

        return new Vertex(x, y);
    }

    public void setStartingPosition(int x, int y) {
        pathfinder.settle(new Vertex(x, y));
    }

    public LinkedList<Vertex> getPathTo(int x, int y) {
        return pathfinder.getPath(new Vertex(x, y));
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

    public class Square extends Drawable {

        private final Rect rect;
        private final int middleX, middleY;
        private Paint paint;

        private Square(int x, int y, int width, Paint paint) {
            //left top right bottom
            rect = new Rect(x, y, x + width, y + width);
            middleX = x + (width / 2);
            middleY = y + (width / 2);
            this.paint = paint;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        public int getMiddleX() {
            return middleX;
        }

        public int getMiddleY() {
            return middleY;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawRect(rect, paint);
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
}
