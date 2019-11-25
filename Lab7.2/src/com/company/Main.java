package com.company;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
    private static int TESTS_NUMBER = 15;
    private static int QUEUE_CAPACITY = 20;

    public static void main(String[] args) {
        List<BigInteger> numbers;

        for (int i = 1; i <= TESTS_NUMBER; i++){
            numbers = generateNumbers(i * 10, i);
            addParallel(i, numbers);
            addSequential(i, numbers);
        }
    }

    private static void addNumbersSequential(List<BigInteger> numbers) {
        //classic addition
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger number : numbers) {
            sum = sum.add(number);
        }
    }

    private static void addNumbersParallel(List<BigInteger> numbers) {
        //parallel addition
        int N = numbers.size();

        List<ArrayBlockingQueue<BigInteger>> queues = new ArrayList<>(N - 1);

        for (int i = 0; i < N - 1; i++){
            queues.add(new ArrayBlockingQueue<>(QUEUE_CAPACITY));
        }

        //we create N-1 threads
        List<Thread> threads = new ArrayList<>(N - 1);

        //each thread makes an addition
        threads.add(new Thread(new AddOperation(numbers.get(0), numbers.get(1), queues.get(0))));

        for (int i = 2; i < N; i++){
            threads.add(new Thread(new AddOperation(numbers.get(i), queues.get(i - 2), queues.get(i - 1))));
        }

        for (int i = 0; i < N - 1; i++){
            threads.get(i).start();
        }

        for (int i = 0; i < N - 1; i++){
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addParallel(int level, List<BigInteger> numbers) {
        long startTime = System.nanoTime();
        addNumbersParallel(numbers);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        if (level == 1 || level == 10 || level == 15)
            System.out.println("Parallel: Level " + level + ": " + duration + " ms");

    }

    private static void addSequential(int level, List<BigInteger> numbers) {
        long startTime = System.nanoTime();
        addNumbersSequential(numbers);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        if (level == 1 || level == 10 || level == 15)
            System.out.println("Sequential: Level " + level + ": " + duration + " ms");

    }

    private static List<BigInteger> generateNumbers(int count, int digitSize) {
        List<BigInteger> numbers = new ArrayList<>(count);

        for (int i = 0; i < count; i++){
            BigInteger number = new BigInteger(generateRandomNumber(digitSize));
            numbers.add(number);
            //System.out.println(number);
        }
        return numbers;
    }

    private static String generateRandomNumber(int length) {
        StringBuilder result = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            //we append a random digit to the stringbuilder
            result.append(random.nextInt(9) + 1);
        }
        return result.toString();
    }
}
