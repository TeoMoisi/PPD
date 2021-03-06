package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        int GRAPHS_COUNT = 21;

        //we create a list with 10 random graphs
        List<DirectedGraph> graphs = new ArrayList<>();
        for (int i = 1; i <= GRAPHS_COUNT; i++) {
            graphs.add(generateRandomHamiltonian(i * 10));
        }

        //the sequential version
        System.out.println("Sequential");
        batchTesting(graphs, 1);

        //the parallel version
        System.out.println("Parallel");
        batchTesting(graphs, 4);

    }

    private static void findHamiltonian(DirectedGraph graph, int threadCount) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Integer> result = new ArrayList<>(graph.size());

        //we construct the paths for each node
        for (int i = 0; i < graph.size(); i++){
            pool.execute(new FindHamiltoneanTask(graph, i, result));
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static void batchTesting(List<DirectedGraph> graphs, int threadCount) throws InterruptedException {
        for (int i = 0; i < graphs.size(); i++) {
            test(i, graphs.get(i), threadCount);
        }
    }

    private static void test(int level, DirectedGraph graph, int threadCount) throws InterruptedException {
        long startTime = System.nanoTime();
        findHamiltonian(graph, threadCount);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        if (level == 1 || level == 10 || level == 20)
            System.out.println("Level " + level + ": " + duration + " ms");
    }

    private static DirectedGraph generateRandomHamiltonian(int size) {
        DirectedGraph graph = new DirectedGraph(size);

        List<Integer> nodes = graph.getNodes();

        java.util.Collections.shuffle(nodes);

        for (int i = 1; i < nodes.size(); i++){
            graph.addEdge(nodes.get(i - 1),  nodes.get(i));
        }

        graph.addEdge(nodes.get(nodes.size() -1), nodes.get(0));

        Random random = new Random();

        for (int i = 0; i < size / 2; i++){
            int nodeA = random.nextInt(size - 1);
            int nodeB = random.nextInt(size - 1);

            graph.addEdge(nodeA, nodeB);
        }

        return graph;
    }
}
