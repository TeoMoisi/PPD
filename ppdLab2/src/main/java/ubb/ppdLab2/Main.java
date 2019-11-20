package ubb.ppdLab2;

import ubb.ppdLab2.domain.Matrix;
import ubb.ppdLab2.threads.AddMatrixThread;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int THREADS_NUMBER = 15;

    public static void main(String[] args) throws InterruptedException {

        Matrix firstMatrix = new Matrix(50, 50);
        Matrix secondMatrix = new Matrix(50, 50);
        Matrix result = new Matrix(50, 50);

        List<AddMatrixThread> addMatrixThreadList = new ArrayList<AddMatrixThread>();


        for (int i = 0; i < THREADS_NUMBER; i++) {
                AddMatrixThread addMatrixThread = new AddMatrixThread(firstMatrix, secondMatrix, result);
                addMatrixThread.start();
                addMatrixThreadList.add(addMatrixThread);
        }

        for (int i = 0; i < THREADS_NUMBER; i++) {
            addMatrixThreadList.get(i).join();
        }

        System.out.println("First matrix: " + firstMatrix.toString());
        System.out.println("Second matrix: " + secondMatrix.toString());
        System.out.println("Result matrix: " + result.toString());
    }
}
