package me.kooruyu.games.battlefield1648.cartography;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

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
    private int maximumMovementLength;

    private int squareWidht;
    private int xSquares, ySquares;
    private int yOffset, xOffset;

    private Paint squarePaint;
    private Paint squareHlPaint;

    private Set<Vertex> moveableSquares;

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

        Paint unmoveablePaint = new Paint();
        unmoveablePaint.setColor(Color.BLUE);

        //Populating the grid
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
                //TODO: Remove debug walls for production
                if (x % 2 != 0 && y % 4 < 3) {
                    squares[i].setMovable(false);
                    squares[i].setPaint(unmoveablePaint);
                }
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
            throw new IndexOutOfBoundsException(String.format(Locale.ENGLISH, "Square at position (%d|%d) doesn't exist!", x, y));
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

    public void clearStartingPosition(int x, int y) {
        Vertex position = new Vertex(x, y);
        clearSquareBackgrounds(moveableSquares);

        if (events.containsPosition(position)) {
            events.getEventAt(position).setAll(false);
        }
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

    public ArrayList<Vertex> getPathTo(int playerX, int playerY, int x, int y) {
        return pathfinder.settle(new Vertex(playerX, playerY), new Vertex(x, y));
    }

    public boolean isBlocked(int x, int y) {
        return !getSquare(x, y).isMovable();
    }

    public boolean isMovable(int playerX, int playerY, int x, int y) {
        Vertex target = new Vertex(x, y);

        return !(playerX == x && playerY == y) //return false if the target is the same as the current position
                && isReachable(target); //check if the target is in reach of the precomputed movable area
        //&& getSquare(x, y).isMovable()
        //&& !mapGraph.getNode(target).isBlocked();
    }

    private boolean isReachable(Vertex target) {
        return moveableSquares.contains(target);
    }

    /**
     * Draws movement indicator and updates events for the given player position
     *
     * @param target the position of the player
     */
    public void setPlayerDestination(Vertex target) {
        highlightSquares(target, maximumMovementLength);
        if (events.containsPosition(target)) {
            events.getEventAt(target).setAll(true);
        }
    }

    //TODO: possibly change this to private later
    public void highlightSquares(Vertex middle, int radius) {
        moveableSquares = new HashSet<>();
        Set<Vertex> visited = new HashSet<>();
        Queue<Stack<Vertex>> nodes = new LinkedList<>();

        Stack<Vertex> tempStack = new Stack<>();
        tempStack.push(middle);
        nodes.offer(tempStack);
        visited.add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty()) {
            tempStack = nodes.poll();
            currentNode = mapGraph.getNode(tempStack.peek());


            getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).setBackground(squareHlPaint);
            moveableSquares.add(currentNode.getVertex());

            //add 1 to disregard the middle
            if (tempStack.size() == (maximumMovementLength + 1)) continue;

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    Stack<Vertex> clone = (Stack<Vertex>) tempStack.clone();
                    clone.push(currentNeighbor);
                    nodes.offer(clone);
                }
            }
        }
    }

    public Set<Vertex> highlightFOVRadius(Vertex middle, Direction direction, Paint paint) {
        Set<Vertex> traversed = new HashSet<>();
        Set<Vertex> visited = new HashSet<>();
        Stack<Stack<Vertex>> nodes = new Stack<>();

        int middleY = middle.getY();
        int middleX = middle.getX();

        Graph.Node currentNode;
        Vertex currentNeighbor;

        Stack<Vertex> tempStack = new Stack<>();
        tempStack.push(middle);
        nodes.push(tempStack);

        boolean correctDirection = false;

        while (!nodes.isEmpty()) {
            tempStack = nodes.pop();
            currentNode = mapGraph.getNode(tempStack.peek());

            getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).setBackground(paint);
            traversed.add(currentNode.getVertex());

            //add 1 to disregard the middle
            if (tempStack.size() == (maximumMovementLength + 1)) continue;

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (visited.contains(currentNeighbor)) continue;
                visited.add(currentNeighbor);

                switch (direction) {
                    case SOUTH:
                        if (currentNeighbor.getY() >= middleY) {
                            correctDirection = true;
                        }
                        break;
                    case NORTH:
                        if (currentNeighbor.getY() <= middleY) {
                            correctDirection = true;
                        }
                        break;
                    case WEST:
                        if (currentNeighbor.getX() <= middleX) {
                            correctDirection = true;
                        }
                        break;
                    case EAST:
                        if (currentNeighbor.getX() >= middleX) {
                            correctDirection = true;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid Direction " + direction);
                }

                if (correctDirection) {
                    Stack<Vertex> clone = (Stack<Vertex>) tempStack.clone();
                    clone.push(currentNeighbor);
                    nodes.push(clone);
                    correctDirection = false;
                }
            }
        }

        return traversed;
    }

    public void setBlocked(Vertex vertex, boolean blocked) {
        mapGraph.getNode(vertex).setBlocked(blocked);
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
        private boolean movable;

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
            movable = true;
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

        public void setMovable(boolean movable) {
            this.movable = movable;
        }

        public boolean isMovable() {
            return movable;
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
