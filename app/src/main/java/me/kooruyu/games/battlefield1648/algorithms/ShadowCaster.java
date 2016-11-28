package me.kooruyu.games.battlefield1648.algorithms;


import java.util.HashSet;
import java.util.Set;

import me.kooruyu.games.battlefield1648.drawables.layers.GridMapDrawable;

public class ShadowCaster {
    private static final int[][] OCTANT_MULTIPLIERS = {
            {1, 0, 0, -1, -1, 0, 0, 1},
            {0, 1, -1, 0, 0, -1, 1, 0},
            {0, 1, 1, 0, 0, -1, -1, 0},
            {1, 0, 0, 1, -1, 0, 0, -1}
    };

    private GridMapDrawable map;
    private Set<Vertex> shadow;
    private int widht, height;

    public ShadowCaster(GridMapDrawable map, int xSquares, int ySquares) {
        this.map = map;
        widht = xSquares;
        height = ySquares;
    }

    public Set<Vertex> castShadow(int x, int y, int radius) {
        shadow = new HashSet<>();

        for (int i = 0; i < 8; i++) {
            cast_light(x, y, radius, 1, 1.0f, 0.0f, OCTANT_MULTIPLIERS[0][i],
                    OCTANT_MULTIPLIERS[1][i], OCTANT_MULTIPLIERS[2][i], OCTANT_MULTIPLIERS[3][i]);
        }

        return shadow;
    }

    private void cast_light(int x, int y, int radius, int row,
                            float start_slope, float end_slope, int xx, int xy, int yx, int yy) {

        if (start_slope < end_slope) {
            return;
        }

        float next_start_slope = start_slope;
        for (int i = row; i <= radius; i++) {
            boolean blocked = false;
            for (int dx = -i, dy = -i; dx <= 0; dx++) {
                float l_slope = (dx - 0.5f) / (dy + 0.5f);
                float r_slope = (dx + 0.5f) / (dy - 0.5f);

                if (start_slope < r_slope) {
                    continue;
                } else if (end_slope > l_slope) {
                    break;
                }

                int sax = dx * xx + dy * xy;
                int say = dx * yx + dy * yy;

                if ((sax < 0 && Math.abs(sax) > x) || (say < 0 && Math.abs(say) > y)) {
                    continue;
                }

                int ax = x + sax;
                int ay = y + say;

                if (ax >= widht || ay >= height) {
                    continue;
                }

                int radius2 = radius * radius;

                if (dx * dx + dy * dy < radius2) {
                    shadow.add(new Vertex(ax, ay));
                }

                if (blocked) {
                    if (!map.getSquare(ax, ay).isMovable()) {
                        next_start_slope = r_slope;
                    } else {
                        blocked = false;
                        start_slope = next_start_slope;
                    }

                } else if (!map.getSquare(ax, ay).isMovable()) {
                    blocked = true;
                    next_start_slope = r_slope;
                    cast_light(x, y, radius, i + 1, start_slope, l_slope, xx,
                            xy, yx, yy);
                }
            }
            if (blocked) {
                break;
            }
        }
    }
}
