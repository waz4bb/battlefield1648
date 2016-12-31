package me.kooruyu.games.battlefield1648.cartography;

public class Region {
    public final int x, y;
    public final int width, height;
    public final int area;

    public Region(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.area = width * height;
    }
}
