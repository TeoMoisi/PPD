package com.company.threads;

import com.company.domain.Matrix;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FirstMultMatrix extends Thread {

    private int row;
    private Lock lock;
    private Condition filledRow;
    private Matrix firstMatrix;
    private Matrix secondMatrix;
    private Matrix resultMatrix;

    public FirstMultMatrix(int row, Matrix firstMatrix, Matrix secondMatrix, Matrix resultMatrix) {
        this.row = row;
        this.lock = new ReentrantLock();
        this.filledRow = lock.newCondition();
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
    }

    @Override
    public void run() {

        lock.lock();

        for (int j = 0; j < this.secondMatrix.getRowsNumber(); j++) {
            for (int k = 0; k < this.secondMatrix.getRowsNumber(); k++) {
                int value = this.resultMatrix.getCellValue(row, j) + this.firstMatrix.getCellValue(row, k) * this.secondMatrix.getCellValue(k, j);
                this.resultMatrix.setCellValue(row, j, value);
            }
        }
        filledRow.signal();
        lock.unlock();
    }


}
