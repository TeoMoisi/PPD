package com.company.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Matrix {

    private List<List<Integer>> values;
    private int rows, columns;

    public Matrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        Random random = new Random();

        this.values = new ArrayList<List<Integer>>(this.rows);
        for (int i = 0; i < this.rows; i ++) {
            this.values.add(new ArrayList<Integer>(this.columns));

            for (int j = 0; j < this.columns; j ++) {
                this.values.get(i).add(random.nextInt(5) + 1);
            }
        }
    }

    public int getColumnsNumber() {
        return this.columns;
    }

    public int getRowsNumber() {
        return this.rows;
    }

    public void setCellValue(int row, int col, int value){
        this.values.get(row).set(col, value);
    }

    public int getCellValue(int row, int col){
        return this.values.get(row).get(col);
    }

    public void setAllCellsToZero() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.setCellValue(i, j, 0);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.rows; i++){
            stringBuilder.append(this.values.get(i).toString()).append("\n");
        }

        return stringBuilder.toString();
    }
}
