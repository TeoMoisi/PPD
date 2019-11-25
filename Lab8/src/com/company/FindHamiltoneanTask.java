package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FindHamiltoneanTask implements Runnable {

    private DirectedGraph graph;
    private int startingNode;
    private List<Integer> path;
    private Lock lock;
    private List<Integer> result;

    FindHamiltoneanTask(DirectedGraph graph, int node, List<Integer> result) {
        this.graph = graph;
        this.startingNode = node;
        this.path = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.result = result;
    }

    @Override
    public void run() {
        //we first visit the startingNode
        visit(this.startingNode);
    }

    private void visit(int node) {
        //we add the node to the path
        this.path.add(node);

        if (this.path.size() == this.graph.size()) {
            //if we have found a cycle
            if (this.graph.getNeighbours(node).contains(this.startingNode)) {
                //we lock the resource and add the path to the results list
                this.lock.lock();
                this.result.clear();
                this.result.addAll(this.path);
                this.lock.unlock();
            }
            return;
        }

        //for all the neighbours of a node, if it is not visited, we visit it
        for (int neighbour : this.graph.getNeighbours(node)) {
            if (!this.path.contains(neighbour)) {
                visit(neighbour);
            }
        }
    }
}
