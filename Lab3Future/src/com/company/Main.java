package com.company;


import com.company.domain.Matrix;
import com.company.threads.MatrixOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

public class Main {

    private static final int THREADS_NUMBER = 4;
    private static final int ROWS_NUMBER = 50;
    private static final int COLUMNS_NUMBER = 50;
    private static final String ADD_OPERATION = "ADD";
    private static final String MULTIPLY_OPERATION = "MULTIPLY";


    private static void matrixOperation(Matrix firstMatrix, Matrix secondMatrix, Matrix result, String operation) throws Exception {

        List<MatrixOperation> matrixOperationList = new ArrayList<>();

        float start =  System.nanoTime() / 1000000;

        for (int i = 0; i < THREADS_NUMBER; i++) {
            MatrixOperation matrixOperation = new MatrixOperation(firstMatrix, secondMatrix, result, operation);
            matrixOperationList.add(matrixOperation);
        }

        for (int row = 0; row < result.getRowsNumber(); row ++) {
            for (int column = 0; column < result.getColumnsNumber(); column ++) {
                matrixOperationList.get((row + column) % THREADS_NUMBER).addWorkingPoint(row, column);
            }
        }

        List<FutureTask> tasks = new ArrayList<>();

        for (MatrixOperation callable : matrixOperationList) {
            tasks.add(new FutureTask(callable));
        }

        for (FutureTask task : tasks) {
            Thread thread = new Thread(task);
            thread.start();
        }

        for (FutureTask task : tasks) {
            task.get();
        }

        System.out.println("First matrix: \n" + firstMatrix.toString());
        System.out.println("Second matrix: \n" + secondMatrix.toString());
        System.out.println("Result matrix: \n" + result.toString());

        float end = System.nanoTime() / 1000000;

        float total = (end - start) / 1000;
        System.out.println(total);

    }

    public static void main(String[] args) throws Exception {

        Matrix firstMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix secondMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);
        Matrix result = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);

        Matrix multiplyMatrix = new Matrix(ROWS_NUMBER, COLUMNS_NUMBER);

        //matrixOperation(firstMatrix, secondMatrix, result, ADD_OPERATION);

        matrixOperation(firstMatrix, secondMatrix, multiplyMatrix, MULTIPLY_OPERATION);
    }
}
