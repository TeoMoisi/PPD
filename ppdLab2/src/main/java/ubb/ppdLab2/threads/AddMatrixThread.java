package ubb.ppdLab2.threads;

import ubb.ppdLab2.domain.Matrix;

public class AddMatrixThread extends Thread {

    private Matrix firstMatrix;
    private Matrix secondMatrix;
    private Matrix result;

    public AddMatrixThread(Matrix firstMatrix, Matrix secondMatrix, Matrix result) {
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.result = result;
    }

    @Override
    public void run() {

        for (int i = 0; i < this.firstMatrix.getRowsNumber(); i ++) {
            for (int j = 0; j < this.firstMatrix.getColumnsNumber(); j ++) {
                int value = this.firstMatrix.getCellValue(i, j) + this.secondMatrix.getCellValue(i, j);
                this.result.setCellValue(i, j, value);
            }
        }
    }
}
