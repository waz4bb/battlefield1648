package me.kooruyu.games.battlefield1648.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.DijkstraPathfinder;
import me.kooruyu.games.battlefield1648.algorithms.Edge;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.events.EventMap;

public class GridMap extends Drawable {

    private Square[] squares;
    private LayerDrawable layer;
    private Graph mapGraph;
    private DijkstraPathfinder pathfinder;

    private EventMap events;
    private Map<Vertex, Square> squareMap;
    private int maximumMovementLength;

    private int squareWidht;
    private int xSquares, ySquares;
    private int yOffset, xOffset;

    private Paint squarePaint;
    private Paint squareHlPaint;

    public GridMap(int xSquares, int ySquares, int width, int height, int maximumMovementLength, EventMap events) {
        this.maximumMovementLength = maximumMovementLength;
        this.events = events;

        //create squares array
        squares = new Square[xSquares * ySquares];
        this.xSquares = xSquares;
        this.ySquares = ySquares;

        //Paints
        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.STROKE);
        squareWidht = Math.min(width / xSquares, height / ySquares);

        squareHlPaint = new Paint();
        squareHlPaint.setColor(Color.argb(180, 3, 192, 60));

        //Grid Geometry
        int gridheight = ySquares * squareWidht;
        int gridwidth = xSquares * squareWidht;
        xOffset = (width - gridwidth) / 2;
        yOffset = (height - gridheight) / 2;

        setBounds(xOffset, yOffset, gridwidth + xOffset, gridheight + yOffset);

        //Populating the grid
        squareMap = new HashMap<>();
        List<Vertex> vertexes = new ArrayList<>();

        for (int i = 0, y = 0, yPos = yOffset;
             yPos < gridheight + yOffset;
             yPos += squareWidht, y++
                ) {

            for (int x = 0, xPos = xOffset;
                 xPos < gridwidth + xOffset;
                 xPos += squareWidht, x++, i++
                    ) {

                Vertex v = new Vertex(x, y);
                vertexes.add(v);
                squares[i] = new Square(xPos, yPos, squareWidht, squarePaint);
                squareMap.put(v, squares[i]);
            }
        }

        mapGraph = new Graph();
        List<Edge> currentEdges;
        Vertex currentVertex;
        //Calculating edges
        for (int i = 0, y = 0; i < vertexes.size(); y++) {
            for (int x = 0; x < xSquares; x++, i++) {
                currentEdges = new ArrayList<>();
                currentVertex = vertexes.get(i);

                if (y > 0) {
                    currentEdges.add(new Edge(currentVertex, vertexes.get(i - xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0) {
                        currentEdges.add(new Edge(currentVertex, vertexes.get(i - xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1) {
                        currentEdges.add(new Edge(currentVertex, vertexes.get(i - xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (y < ySquares - 1) {
                    currentEdges.add(new Edge(currentVertex, vertexes.get(i + xSquares), Edge.DEFAULT_WEIGHT));

                    if (x > 0) {
                        currentEdges.add(new Edge(currentVertex, vertexes.get(i + xSquares - 1), Edge.DIAGONAL_WEIGHT));
                    }
                    if (x < xSquares - 1) {
                        currentEdges.add(new Edge(currentVertex, vertexes.get(i + xSquares + 1), Edge.DIAGONAL_WEIGHT));
                    }
                }
                if (x > 0) {
                    currentEdges.add(new Edge(currentVertex, vertexes.get(i - 1), Edge.DEFAULT_WEIGHT));
                }
                if (x < xSquares - 1) {
                    currentEdges.add(new Edge(currentVertex, vertexes.get(i + 1), Edge.DEFAULT_WEIGHT));
                }

                mapGraph.addVertex(currentVertex, currentEdges);
            }
        }


        pathfinder = new DijkstraPathfinder(mapGraph);
        layer = new LayerDrawable(squares);
    }

    public Square getSquare(int x, int y) {
        int index = (y * xSquares) + x;

        try {
            return squares[index];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.format("Square at position (%d|%d) doesn't exist!", x, y));
        }
    }

    public int getSquareWidht() {
        return squareWidht;
    }

    public Vertex touchSquareAt(int x, int y) {
        //get touched square
        x = (x - xOffset) / squareWidht;
        y = (y - yOffset) / squareWidht;
        int index = (y * xSquares) + x;

        if (index < 0 || index > squares.length - 1) return null;

        return new Vertex(x, y);
    }

    public void setStartingPosition(int x, int y) {
        Vertex start = new Vertex(x, y);
        highlightSquares(start, maximumMovementLength, false);

        if (events.containsPosition(start)) {
            events.getEventAt(start).setAll(false);
        }
    }

    public ArrayList<Vertex> getPathTo(int playerX, int playerY, int x, int y) {
        Vertex target = new Vertex(x, y);
        ArrayList<Vertex> path = pathfinder.settle(new Vertex(playerX, playerY), target);

        highlightSquares(target, maximumMovementLength, true);
        if (events.containsPosition(target)) {
            events.getEventAt(target).setAll(true);
        }

        return path;
    }

    public boolean isMovable(int playerX, int playerY, int x, int y) {
        return Math.max(Math.abs(playerX - x), Math.abs(playerY - y)) <= maximumMovementLength;
    }

    //TODO: possibly change this to private later
    public void highlightSquares(Vertex middle, int radius, boolean enable) {
        Paint newPaint = (enable) ? squareHlPaint : squarePaint;
        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();

        nodes.offer(middle);
        visited.add(middle);

        int middleY = middle.getY();
        int middleX = middle.getX();

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty() && Math.abs(nodes.peek().getY() - middleY) <= radius && Math.abs(nodes.peek().getX() - middleX) <= radius) {
            currentNode = mapGraph.getNode(nodes.poll());
            //getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).setPaint(newPaint);
            if (enable)
                getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).setBackground(newPaint);
            else
                getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).disableBackground();

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                }
            }
        }
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

    public class Square extends Drawable {

        private final Rect rect;
        private final int x, y;
        private final int middleX, middleY;
        private final int width;
        private Paint paint;
        private Square background;

        private Square(int x, int y, int width, Paint paint) {
            //left top right bottom
            this.x = x;
            this.y = y;
            this.width = width;
            rect = new Rect(x, y, x + width, y + width);
            middleX = x + (width / 2);
            middleY = y + (width / 2);
            this.paint = paint;
            background = null;
        }

        private void setBackground(Paint paint) {
            background = new Square(x, y, width, paint);
        }

        private void disableBackground() {
            background = null;
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
            if (background != null) background.draw(canvas);
            canvas.drawRect(rect, paint);
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            for (Square s : squares) {
                s.setColorFilter(colorFilter);
            }
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }
}
