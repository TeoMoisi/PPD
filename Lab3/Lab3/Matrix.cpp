//
//  Matrix.cpp
//  Lab3
//
//  Created by Teofana Moisi on 27/10/2019.
//  Copyright © 2019 Teofana Moisi. All rights reserved.
//

#include "Matrix.hpp"

Matrix::Matrix() {
    this->rows = 0;
    this->columns = 0;
}

Matrix::Matrix(int rows, int columns) : values(rows, vector<int>(columns)) {
    this->rows = rows;
    this->columns = columns;
    
    for (int i = 0; i < this->rows; i++) {
        for (int j = 0; j < this->columns; j++) {
            int random = rand() % 400 + 100;
            this->values[i][j] = random;
        }
    }
}

Matrix::~Matrix() {
}

string Matrix::printMatrix() {
    string result = "";
    
    for (vector<int> vec: values) {
        for (int val: vec) {
            result += to_string(val) + ' ';
        }
        result += '\n';
    }
    
    return result;
}

int Matrix::getRowsNumber() {
    return this->rows;
}

int Matrix::getColumnsNumber() {
    return this->columns;
}

void Matrix::setCellValue(int row, int column, int value) {
    this->values[row][column] = value;
}

int Matrix::getCellValue(int row, int column) {
    return this->values[row][column];
}
