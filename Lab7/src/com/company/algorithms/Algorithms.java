package com.company.algorithms;

import com.company.model.Task;
import sun.awt.Mutex;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Algorithms {

    public List<Integer> classicAddition(List<Integer> input) {
        List<Integer> result = new ArrayList<>(input.size());

        result.add(input.get(0));

        for (int i = 1; i < input.size(); i++) {
            result.add(input.get(i) + result.get(i - 1));
        }

        return result;
    }

    public List<Integer> logaritmicAddition(List<Integer> input, int threadCount) {

        List<Integer> result = new ArrayList<>();
        List<Mutex> mutexes = new ArrayList<>();

        for (Integer integer : input) {
            result.add(integer);
            mutexes.add(new Mutex());
        }
        ExecutorService pool;

        int k;
        for(k = 1; k < result.size(); k *= 2) {
            pool = Executors.newFixedThreadPool(threadCount);

            for(int i = 2 * k - 1; i < result.size() ; i += 2 * k) {
                pool.execute(new Task(result, mutexes, k, i));
            }

            pool.shutdown();

            try {
                pool.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        for(k = k/4; k > 0 ; k = k/2) {
            pool = Executors.newFixedThreadPool(threadCount);

            for(int i = 3 * k - 1 ; i < result.size() ; i += 2 * k) {
                pool.execute(new Task(result, mutexes, k, i));
            }
            pool.shutdown();
            try {
                pool.awaitTermination(100, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

