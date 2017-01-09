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

    public boolean onBorder(Vertex vertex) {
        return contains(vertex)
                && this.x == vertex.x
                || this.y == vertex.y
                || (this.x + this.width - 1) == vertex.x
                || (this.y + this.height - 1) == vertex.y;
    }

    public boolean contains(Vertex vertex) {
        return vertex.x >= this.x
                && vertex.y >= this.y
                && vertex.x <= (this.x + width)
                && vertex.y <= (this.y + height);
    }
}
