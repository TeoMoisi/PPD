package com.company;

import com.company.domain.Matrix;
import com.company.threads.FirstMultMatrix;
import com.company.threads.SecondMultMatrix;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int THREADS_NUMBER = 6;
    private static final int ROWS_NUMBER = 3;
    private static final int COLUMNS_NUMBER = 3;

    private static void tripleMultiplication(Matrix firstMatrix, Matrix secondMatrix, Matrix thirdMatrix, Matrix intermmediateMatrix, Matrix resultMatrix) throws InterruptedException {

        long startTime = System.currentTimeMillis();

        ExecutorService executorServiceFirstMult = Executors.newFixedThreadPool(THREADS_NUMBER);
        for (int rows = 0; rows < ROWS_NUMBER; rows++) {
            FirstMultMatrix thr = new FirstMultMatrix(rows, firstMatrix, secondMatrix, intermmediateMatrix);
            executorServiceFirstMult.submit(thr);
        }

        ExecutorService executorServiceSecondMult = Executors.newFixedThreadPool(THREADS_NUMBER);
        for (int rows = 0; rows < ROWS_NUMBER; rows++) {
            SecondMultMatrix thr = new SecondMultMatrix(rows, intermmediateMatrix, thirdMatrix, resultMatrix);
            executorServiceSecondMult.submit(thr);
        }

        if (!executorServiceFirstMult.awaitTermination(10, TimeUnit.MILLISECONDS))
            executorServiceFirstMult.shutdown();
        if (!executorServiceSecondMult.awaitTermination(10, TimeUnit.MILLISECONDS))
            executorServiceSecondMult.shutdown();


        long stopTime = System.currentTimeMillis();
        long totalTime = stopTime - startTime;
        System.out.println("Elapsed time for product computation: " + totalTime);


        System.out.println("First Matrix: ");
        System.out.println(firstMatrix.toString() + '\n');

        System.out.println("Second Matrix: ");
        System.out.println(secondMatrix.toString());

        System.out.println("\nIntermmediate result: ");
        System.out.println(intermmediateMatrix.toString());

        System.out.println("Third Matrix: ");
        System.out.println(thirdMatrix.toString());

        System.out.println("\nFinal result: ");
        System.out.println(resultMatrix.toString());
    }

    public static void main(String[] args) throws InterruptedException {

        Matrix firstMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix secondMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix thirdMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix intermmediateMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        intermmediateMatrix.setAllCellsToZero();
        Matrix result = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        result.setAllCellsToZero();

        tripleMultiplication(firstMatrix, secondMatrix, thirdMatrix, intermmediateMatrix, result);
    }
}
