package me.kooruyu.games.battlefield1648.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<Vertex, Node> vertexMap;
    private final List<Edge> edges;

    public Graph() {
        vertexMap = new HashMap<>();
        edges = new ArrayList<>();
    }

    public void addVertex(Vertex v, List<Edge> neighbors) {
        edges.addAll(neighbors);
        Node tmp = new Node(v, neighbors);
        vertexMap.put(v, tmp);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public boolean containsVertex(Vertex v) {
        return vertexMap.containsKey(v);
    }

    public Node getNode(Vertex v) {
        return vertexMap.get(v);
    }

    public class Node {
        private Vertex vertex;
        private List<Edge> neighbors;
        private boolean isBlocked;

        private Node(Vertex vertex, List<Edge> neighbors) {
            this.vertex = vertex;
            this.neighbors = neighbors;
            isBlocked = false;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public List<Edge> getNeighbors() {
            return neighbors;
        }

        public boolean isBlocked() {
            return isBlocked;
        }

        public void setBlocked(boolean blocked) {
            isBlocked = blocked;
        }
    }
}
