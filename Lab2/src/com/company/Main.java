package com.company;


import com.company.domain.Matrix;
import com.company.threads.MatrixOperationThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private static final int THREADS_NUMBER = 20;
    private static final int ROWS_NUMBER = 50;
    private static final int COLUMNS_NUMBER = 50;
    private static final String ADD_OPERATION = "ADD";
    private static final String MULTIPLY_OPERATION = "MULTIPLY";


    private static void matrixOperation(Matrix firstMatrix, Matrix secondMatrix, Matrix result, String operation) throws InterruptedException {

        List<MatrixOperationThread> matrixOperationThreadList = new ArrayList<>();

        float start =  System.nanoTime() / 1000000;

        for (int i = 0; i < THREADS_NUMBER; i++) {
            MatrixOperationThread matrixOperationThread = new MatrixOperationThread(firstMatrix, secondMatrix, result, operation);
            matrixOperationThreadList.add(matrixOperationThread);
        }

        Random rand = new Random();

        for (int row = 0; row < result.getRowsNumber(); row ++) {
            for (int column = 0; column < result.getColumnsNumber(); column ++) {
                matrixOperationThreadList.get((row + column) % THREADS_NUMBER).addWorkingPoint(row, column);
            }
        }

        for (int i = 0; i < THREADS_NUMBER; i++) {
            matrixOperationThreadList.get(i).start();
        }

        for (int i = 0; i < THREADS_NUMBER; i++) {
            matrixOperationThreadList.get(i).join();
        }

        System.out.println("First matrix: \n" + firstMatrix.toString());
        System.out.println("Second matrix: \n" + secondMatrix.toString());
        System.out.println("Result matrix: \n" + result.toString());

        float end = System.nanoTime() / 1000000;

        float total = (end - start) / 1000;
        System.out.println(total);

    }

    public static void main(String[] args) throws InterruptedException {

        Matrix firstMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix secondMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix result = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);

        Matrix multiplyMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);

        matrixOperation(firstMatrix, secondMatrix, result, ADD_OPERATION);

        //matrixOperation(firstMatrix, secondMatrix, multiplyMatrix, MULTIPLY_OPERATION);
    }
}
