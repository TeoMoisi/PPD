package com.company;

import java.util.ArrayList;
import java.util.List;

public class DirectedGraph {

    //adjacency list for the neighbours
    private List<List<Integer>> neighbours;
    //nodes list
    private List<Integer> nodes;

    DirectedGraph(int nodeCount) {
        this.neighbours = new ArrayList<>(nodeCount);
        this.nodes = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            this.neighbours.add(new ArrayList<>());
            this.nodes.add(i);
        }
    }

    void addEdge(int nodeA, int nodeB) {
        this.neighbours.get(nodeA).add(nodeB);
    }

    List<Integer> getNeighbours(int node) {
        return this.neighbours.get(node);
    }

    List<Integer> getNodes(){
        return nodes;
    }

    int size() {
        return this.neighbours.size();
    }
}
