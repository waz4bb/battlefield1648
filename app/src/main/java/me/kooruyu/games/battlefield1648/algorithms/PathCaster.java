package me.kooruyu.games.battlefield1648.algorithms;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.Region;
import me.kooruyu.games.battlefield1648.cartography.Vertex;

public class PathCaster {
    private Graph mapGraph;

    public PathCaster(Graph mapGraph) {
        this.mapGraph = mapGraph;
    }

    public List<Set<Vertex>> castMaximumDirectionPaths(Vertex middle, int radius, Direction direction) {
        List<Set<Vertex>> maximumPaths = new ArrayList<>();

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

            if (currentLength >= maximumPaths.size()) {
                maximumPaths.add(new HashSet<Vertex>());
            }

            if (currentLength <= radius) {
                Vertex currentNodeVertex = currentNode.getVertex();
                if (direction == Direction.getDirection(middle.x, middle.y, currentNodeVertex.x, currentNodeVertex.y)) {
                    maximumPaths.get(currentLength).add(currentNode.getVertex());
                }
                if (currentLength == radius) {
                    continue;
                }
            }

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (!visited.contains(currentNeighbor)) {
                    if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                    if (currentLength > radius) continue;

                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength + 1);
                }
            }
        }

        return maximumPaths;
    }

    public Set<Vertex> castMaximumPaths(Vertex middle, int radius, Region bounds) {
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
                if (bounds.contains(currentNode.getVertex())) {
                    maximumPaths.add(currentNode.getVertex());
                }
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

    public List<List<Vertex>> castPathsByLevel(Vertex middle, int radius) {

        List<List<Vertex>> pathSteps = new ArrayList<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();
        Queue<Integer> pathLengths = new LinkedList<>();

        pathLengths.offer(0);
        nodes.offer(middle);

        visited.add(middle);

        pathSteps.add(new ArrayList<Vertex>());
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
                    if (currentLength > radius) continue;

                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength);

                    if (currentLength >= pathSteps.size()) {
                        pathSteps.add(new ArrayList<Vertex>());
                    }

                    pathSteps.get(currentLength).add(currentNeighbor);
                }
            }
        }

        return pathSteps;
    }

    public List<List<Vertex>> getPathTraversal(Vertex middle, int radius) {

        List<List<Vertex>> pathSteps = new ArrayList<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> nodes = new LinkedList<>();
        Queue<Integer> pathLengths = new LinkedList<>();

        pathLengths.offer(0);
        nodes.offer(middle);

        visited.add(middle);

        pathSteps.add(new ArrayList<Vertex>());
        pathSteps.get(0).add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        int lastNeighborIndex = 0;
        int neighborIndex = 0;

        while (!nodes.isEmpty()) {
            Vertex tempVertex = nodes.poll();
            int currentLength = pathLengths.poll() + 1;
            currentNode = mapGraph.getNode(tempVertex);

            neighborIndex++;
            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    if (currentLength > radius) continue;

                    visited.add(currentNeighbor);
                    nodes.offer(currentNeighbor);
                    pathLengths.offer(currentLength);

                    if (neighborIndex >= pathSteps.size()) {
                        if (neighborIndex > (lastNeighborIndex + 1)) {
                            neighborIndex = lastNeighborIndex + 1;
                        }

                        lastNeighborIndex = neighborIndex;
                        pathSteps.add(new ArrayList<Vertex>());
                    }

                    pathSteps.get(neighborIndex).add(currentNeighbor);
                }
            }
        }

        return pathSteps;
    }
}
