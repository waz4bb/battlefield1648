package me.kooruyu.games.battlefield1648.drawables.layers;


import android.content.res.Resources;
import android.graphics.Bitmap;
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
import me.kooruyu.games.battlefield1648.cartography.CampData;
import me.kooruyu.games.battlefield1648.cartography.Vertex;
import me.kooruyu.games.battlefield1648.drawables.GridSquare;
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.TextureSquare;
import me.kooruyu.games.battlefield1648.util.TextureContainer;

public class GridMapDrawable extends Drawable {

    private Square[] squares;
    private LayerDrawable layer;
    private TextureContainer textures;

    private final Graph mapGraph;

    private final int originalSquareWidth;
    private int squareWidth;
    private int originalXOffset, originalYOffset;
    private int xOffset, yOffset;
    private int xSquares, ySquares;
    private int screenWidth, screenHeight;

    private final Bitmap backgroundTextures;

    private Paint squarePaint;

    private float zoomFactor;

    public GridMapDrawable(int xSquares, int ySquares, int width, int height, CampData mapData, Resources resources) {
        //create squares array
        squares = new Square[xSquares * ySquares];
        this.xSquares = xSquares;
        this.ySquares = ySquares;
        screenWidth = width;
        screenHeight = height;

        //Paints
        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.STROKE);
        squarePaint.setColor(Color.WHITE);
        originalSquareWidth = squareWidth = Math.max(screenWidth / xSquares, screenHeight / ySquares);

        //load textures
        textures = new TextureContainer(resources, squareWidth);

        mapGraph = new Graph();

        backgroundTextures = Bitmap.createBitmap(squareWidth * xSquares,
                squareWidth * ySquares,
                Bitmap.Config.ARGB_8888);

        layer = new LayerDrawable(createMap(mapData.cells));

        xOffset = yOffset = 0;
        originalXOffset = originalYOffset = 0;

        zoomFactor = 1;
    }

    private Drawable[] createMap(char[][] mapData) {
        Canvas backgroundCanvas = new Canvas(backgroundTextures);

        Paint wallPaint = new Paint();
        wallPaint.setColor(Color.BLUE);

        Paint treePaint = new Paint();
        treePaint.setColor(Color.GREEN);

        Paint palisadePaint = new Paint();
        palisadePaint.setColor(Color.rgb(153, 76, 0));

        //Grid Geometry
        int gridHeight = ySquares * squareWidth;
        int gridWidth = xSquares * squareWidth;

        setBounds(0, 0, gridWidth, gridHeight);

        //Populating the grid
        List<Vertex> vertices = new ArrayList<>();

        List<Drawable> drawableSquares = new ArrayList<>();

        for (int i = 0, y = 0, yPos = 0;
             yPos < gridHeight;
             yPos += squareWidth, y++
                ) {

            for (int x = 0, xPos = 0;
                 xPos < gridWidth;
                 xPos += squareWidth, x++, i++
                    ) {

                Vertex v = new Vertex(x, y);
                vertices.add(v);

                if (mapData[y][x] == '#' || mapData[y][x] == '|') {
                    //squares[i] = new OpaqueSquare(xPos, yPos, squareWidth, wallPaint);
                    TextureSquare square = new TextureSquare(xPos, yPos, squareWidth, textures.wood, null);
                    square.draw(backgroundCanvas);
                    square.setTextureRendering(false);
                    square.setMovable(false);
                    squares[i] = square;
                } else if (mapData[y][x] == 'o') {
                    //squares[i] = new OpaqueSquare(xPos, yPos, squareWidth, treePaint);
                    TextureSquare square = new TextureSquare(xPos, yPos, squareWidth, textures.tree, null);
                    square.setMovable(false);
                    square.draw(backgroundCanvas);
                    square.setTextureRendering(false);
                    squares[i] = square;
                } else if (mapData[y][x] == '.' || mapData[y][x] == '+') {
                    GridSquare square = new GridSquare(xPos, yPos, squareWidth, squarePaint, textures.woodenFloor);
                    square.draw(backgroundCanvas);
                    square.setTextureRendering(false);
                    squares[i] = square;
                    drawableSquares.add(square);
                } else {
                    GridSquare square = new GridSquare(xPos, yPos, squareWidth, squarePaint, null);
                    square.setTextureRendering(false);
                    squares[i] = square;
                    drawableSquares.add(square);
                }
            }
        }
        Drawable[] drawables = new Drawable[drawableSquares.size()];

        for (int i = 0; i < drawables.length; i++) {
            drawables[i] = drawableSquares.get(i);
        }

        fillGraph(vertices);

        return drawables;
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

    public void setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;

        squareWidth = (int) (originalSquareWidth * zoomFactor);

        moveTo(originalXOffset, originalYOffset);
    }

    public void moveTo(int xOffset, int yOffset) {
        originalXOffset = xOffset;
        originalYOffset = yOffset;

        xOffset = (int) (xOffset * zoomFactor);
        yOffset = (int) (yOffset * zoomFactor);
        this.xOffset = xOffset;
        this.yOffset = yOffset;


        int gridHeight = ySquares * squareWidth;
        int gridWidth = xSquares * squareWidth;

        setBounds(-xOffset, -yOffset, gridWidth - xOffset, gridHeight - yOffset);
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
        x = (x + xOffset) / squareWidth;
        y = (y + yOffset) / squareWidth;
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
            Square s = getSquare(v.x, v.y);
            if (!s.isOpaque()) {
                ((GridSquare) s).disableBackground();
            }
        }
    }

    public void drawSquareBackgrounds(Set<Vertex> vertices, Paint paint) {
        for (Vertex v : vertices) {
            Square s = getSquare(v.x, v.y);
            if (!s.isOpaque()) {
                ((GridSquare) s).setBackground(paint);
            }
        }
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public Graph getMapGraph() {
        return mapGraph;
    }

    public Paint getSquarePaint() {
        return squarePaint;
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
        canvas.drawBitmap(backgroundTextures, 0, 0, null);
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
