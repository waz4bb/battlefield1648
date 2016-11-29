package me.kooruyu.games.battlefield1648.algorithms;


import java.util.HashSet;
import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.drawables.layers.GridMapDrawable;

public class ShadowCaster {

    private GridMapDrawable map;
    private Set<Vertex> shadow;
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
            cast_light(x, y, 1, 1.0f, 0.0f,
                    direction.octants[0][i], direction.octants[1][i], direction.octants[2][i], direction.octants[3][i]);
        }

        return shadow;
    }

    private void cast_light(int x, int y, int row, float start_slope, float end_slope, int xx, int xy, int yx, int yy) {

        if (start_slope < end_slope) {
            return;
        }

        boolean blocked = false;
        float next_start_slope = 0;

        for (int i = row; i <= radius && !blocked; i++) {
            for (int currentCell = 1, dx = -i, dy = -i; dx <= 0; dx++, currentCell++) {
                int currentX = x + dx * xx + dy * xy;
                int currentY = y + dx * yx + dy * yy;
                float leftSlope = (dx - 0.5f) / (dy + 0.5f);
                float rightSlope = (dx + 0.5f) / (dy - 0.5f);

                /*
                float angleRange = 1.0f / cellCount;
                float leftAngle = currentCell * angleRange;
                float middleAngle = leftAngle + (angleRange / 2);
                float rightAngle = leftAngle + angleRange;
                */

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
                    cast_light(x, y, i + 1, start_slope, leftSlope, xx, xy, yx, yy);
                    next_start_slope = rightSlope;
                }
            }
        }
    }
}
