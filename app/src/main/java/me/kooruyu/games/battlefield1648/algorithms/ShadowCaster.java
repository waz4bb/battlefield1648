package me.kooruyu.games.battlefield1648.algorithms;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.drawables.layers.GridMapDrawable;

public class ShadowCaster {

    private GridMapDrawable map;
    private Set<Vertex> shadow;
    private List<List<Vertex>> shadowLevels;
    private int width, height;
    private int radius;

    public ShadowCaster(GridMapDrawable map, int xSquares, int ySquares) {
        this.map = map;
        width = xSquares;
        height = ySquares;
    }

    public Set<Vertex> castShadow(int x, int y, int radius, Direction direction) {
        shadow = new HashSet<>();
        this.radius = radius;


        for (int i = 0; i < direction.octants[0].length; i++) {
            castLight(x, y, 1, 1.0f, 0.0f,
                    direction.octants[0][i], direction.octants[1][i], direction.octants[2][i], direction.octants[3][i]);
        }

        return shadow;
    }

    public List<List<Vertex>> castShadowLevels(int x, int y, int radius, Direction direction) {
        shadowLevels = new ArrayList<>();
        this.radius = radius;


        for (int i = 0; i < direction.octants[0].length; i++) {
            castLightByLevel(x, y, 1, 1.0f, 0.0f,
                    direction.octants[0][i], direction.octants[1][i], direction.octants[2][i], direction.octants[3][i]);
        }

        return shadowLevels;
    }

    private void castLightByLevel(int x, int y, int row, float start_slope, float end_slope, int xx, int xy, int yx, int yy) {

        if (start_slope < end_slope) {
            return;
        }

        boolean blocked = false;
        float next_start_slope = 0;

        for (int i = row; i <= radius && !blocked; i++) {
            for (int dx = -i, dy = -i; dx <= 0; dx++) {
                int currentX = x + dx * xx + dy * xy;
                int currentY = y + dx * yx + dy * yy;
                float leftSlope = (dx - 0.5f) / (dy + 0.5f);
                float rightSlope = (dx + 0.5f) / (dy - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start_slope < rightSlope) {
                    continue;
                } else if (end_slope > leftSlope) {
                    break;
                }

                if (dx * dx + dy * dy <= (radius * radius)) {
                    if (i >= shadowLevels.size()) {
                        shadowLevels.add(new ArrayList<Vertex>());
                    }
                    shadowLevels.get(i - 1).add(new Vertex(currentX, currentY));
                }

                if (blocked) {
                    if (map.getSquare(currentX, currentY).isMovable()) {
                        blocked = false;
                        start_slope = next_start_slope;
                    } else {
                        next_start_slope = rightSlope;
                    }
                } else if (!map.getSquare(currentX, currentY).isMovable() && i < radius) {
                    blocked = true;
                    castLightByLevel(x, y, i + 1, start_slope, leftSlope, xx, xy, yx, yy);
                    next_start_slope = rightSlope;
                }
            }
        }
    }

    private void castLight(int x, int y, int row, float start_slope, float end_slope, int xx, int xy, int yx, int yy) {

        if (start_slope < end_slope) {
            return;
        }

        boolean blocked = false;
        float next_start_slope = 0;

        for (int i = row; i <= radius && !blocked; i++) {
            for (int dx = -i, dy = -i; dx <= 0; dx++) {
                int currentX = x + dx * xx + dy * xy;
                int currentY = y + dx * yx + dy * yy;
                float leftSlope = (dx - 0.5f) / (dy + 0.5f);
                float rightSlope = (dx + 0.5f) / (dy - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start_slope < rightSlope) {
                    continue;
                } else if (end_slope > leftSlope) {
                    break;
                }

                if (dx * dx + dy * dy <= (radius * radius)) {
                    shadow.add(new Vertex(currentX, currentY));
                }

                if (blocked) {
                    if (map.getSquare(currentX, currentY).isMovable()) {
                        blocked = false;
                        start_slope = next_start_slope;
                    } else {
                        next_start_slope = rightSlope;
                    }
                } else if (!map.getSquare(currentX, currentY).isMovable() && i < radius) {
                    blocked = true;
                    castLight(x, y, i + 1, start_slope, leftSlope, xx, xy, yx, yy);
                    next_start_slope = rightSlope;
                }
            }
        }
    }
}
