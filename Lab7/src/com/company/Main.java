package com.company;

import com.company.algorithms.Algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
	// write your code here

        Algorithms algorithms = new Algorithms();

        List<Integer> input = Arrays.asList(1, 5, 2, 4);
        List<Integer> result = algorithms.logaritmicAddition(input, 4);

        System.out.println("input              = " + input.toString());
        System.out.println("result             = " + result.toString());

        measurePerformance(1, 4, algorithms);
    }

    public static void measurePerformance(int threadNumber1, int threadNumber2, Algorithms algorithms) {

        List<List<Integer>> testBatch = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            List<Integer> result = new ArrayList<>(10);

            Random random = new Random();

            for (int j = 0; j < 10; j++) {
                result.add(random.nextInt(900) + 100);
            }
            testBatch.add(result);
        }

        for (int  i = 0; i < testBatch.size(); i++) {
            long startTime = System.nanoTime();
            List<Integer> res = algorithms.logaritmicAddition(testBatch.get(i), threadNumber2);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            if (i == 1 || i == 50 || i == 99) {
                System.out.println("Level " + i + ": " + duration + " ms");
                System.out.println(res.toString());
            }

        }
    }
}
