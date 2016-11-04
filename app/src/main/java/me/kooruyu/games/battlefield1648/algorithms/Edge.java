package me.kooruyu.games.battlefield1648.algorithms;

public class Edge {

    public static final int DEFAULT_WEIGHT = 2;
    public static final int DIAGONAL_WEIGHT = 3;

    private final Vertex source;
    private final Vertex destination;
    private final int weight;

    public Edge(Vertex source, Vertex destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " -> " + destination;
    }
}
