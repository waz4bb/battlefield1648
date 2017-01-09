package me.kooruyu.games.battlefield1648.algorithms;

import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class Edge {

    public static final int DEFAULT_WEIGHT = 2;
    public static final int DIAGONAL_WEIGHT = 3;

    private final Vertex destination;
    private final int weight;

    public Edge(Vertex destination, int weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public Vertex getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return " -> " + destination;
    }
}
