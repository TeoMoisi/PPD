package com.company.threads;

import com.company.domain.Matrix;
import com.company.domain.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MatrixOperation implements Callable {

    private Matrix firstMatrix;
    private Matrix secondMatrix;
    private Matrix result;
    private List<Pair<Integer, Integer>> workingPoints;
    private String operation;

    public MatrixOperation(Matrix firstMatrix, Matrix secondMatrix, Matrix result, String operation) {
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.result = result;
        this.workingPoints = new ArrayList<>();
        this.operation = operation;
    }

    public void addWorkingPoint(int row, int col){
        this.workingPoints.add(new Pair<>(row, col));
    }

    @Override
    public Integer call() {
        for (Pair<Integer, Integer> point : this.workingPoints) {
            int row = point.getFirstValue();
            int column = point.getSecondValue();

            if (this.operation.equals("ADD")) {
                int value = this.firstMatrix.getCellValue(row, column)
                        + this.secondMatrix.getCellValue(row, column);
                this.result.setCellValue(row, column, value);
            } else {
                int mutiplication = 0;
                for (int i = 0; i < this.result.getRowsNumber(); i ++) {
                    mutiplication += this.firstMatrix.getCellValue(row, i) * this.secondMatrix.getCellValue(i, column);
                }
                this.result.setCellValue(row, column, mutiplication);
            }
        }
        return 0;
    }
}
