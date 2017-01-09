package me.kooruyu.games.battlefield1648.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class DijkstraPathfinder {

    private final Graph graph;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;

    public DijkstraPathfinder(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Vertex> getPathTo(Vertex source, Vertex target) {

        if (source.equals(target)) {
            return null;
        }

        //TODO: using the graph size might not be efficient
        unSettledNodes = new HashSet<>(graph.size());
        distance = new HashMap<>(graph.size());
        predecessors = new HashMap<>();

        distance.put(source, 0);
        unSettledNodes.add(source);

        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            if (node.equals(target)) {
                return getPath(target);
            }
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }

        //if target could not be found
        return null;
    }

    private void findMinimalDistances(Vertex node) {
        Vertex target;
        for (Edge e : graph.getNode(node).getNeighbors()) {
            target = e.getDestination();
            if (graph.getNode(target).isBlocked()) continue;
            if (getShortestDistance(target) > getShortestDistance(node) + e.getWeight()) {
                distance.put(target, getShortestDistance(node) + e.getWeight());
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;

        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private int getShortestDistance(Vertex destination) {
        Integer d = distance.get(destination);
        return (d == null) ? Integer.MAX_VALUE : d;
    }

    /*
     * Calculates the shortest path to the target
     * @return the path from the source to the selected target and null if no path exists
     */
    private ArrayList<Vertex> getPath(Vertex target) {
        ArrayList<Vertex> path = new ArrayList<>();
        Vertex step = target;

        path.add(step);
        while ((step = predecessors.get(step)) != null) {
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}