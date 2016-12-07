package me.kooruyu.games.battlefield1648.algorithms;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PathCaster {
    private Graph mapGraph;

    public PathCaster(Graph mapGraph) {
        this.mapGraph = mapGraph;
    }

    public Set<Vertex> castMaximumPaths(Vertex middle, int radius) {

        Set<Vertex> maximumPaths = new HashSet<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();
        Queue<Integer> pathLengths = new LinkedList<>();

        pathLengths.offer(0);
        nodes.offer(middle);

        visited.add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty()) {
            Vertex tempVertex = nodes.poll();
            int currentLength = pathLengths.poll();
            currentNode = mapGraph.getNode(tempVertex);

            if (currentLength == radius) {
                maximumPaths.add(currentNode.getVertex());
                continue;
            }

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength + 1);
                }
            }
        }

        return maximumPaths;
    }

    public Set<Vertex> castAllPaths(Vertex middle, int radius) {

        Set<Vertex> pathSteps = new HashSet<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();
        Queue<Integer> pathLengths = new LinkedList<>();

        pathLengths.offer(0);
        nodes.offer(middle);

        visited.add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty()) {
            Vertex tempVertex = nodes.poll();
            int currentLength = pathLengths.poll();
            currentNode = mapGraph.getNode(tempVertex);

            pathSteps.add(currentNode.getVertex());

            if (currentLength == radius) continue;

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength + 1);
                }
            }
        }

        return pathSteps;
    }

    public List<Set<Vertex>> castPathsByLevel(Vertex middle, int radius) {

        List<Set<Vertex>> pathSteps = new ArrayList<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();
        Queue<Integer> pathLengths = new LinkedList<>();

        pathLengths.offer(0);
        nodes.offer(middle);

        visited.add(middle);

        pathSteps.add(new HashSet<Vertex>());
        pathSteps.get(0).add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty()) {
            Vertex tempVertex = nodes.poll();
            int currentLength = pathLengths.poll() + 1;
            currentNode = mapGraph.getNode(tempVertex);


            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength);

                    if (currentLength == radius) continue;
                    if (currentLength <= pathSteps.size()) {
                        pathSteps.add(new HashSet<Vertex>());
                    }
                    pathSteps.get(currentLength).add(currentNeighbor);
                }
            }
        }

        return pathSteps;
    }
}
