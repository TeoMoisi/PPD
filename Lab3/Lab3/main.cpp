//
//  main.cpp
//  Lab3
//
//  Created by Teofana Moisi on 27/10/2019.
//  Copyright © 2019 Teofana Moisi. All rights reserved.
//

#include <iostream>
#include "Matrix.hpp"
#include "ThreadPool.h"

using namespace std;

void addition(Matrix firstMatrix, Matrix secondMatrix, Matrix* result) {
    ThreadPool pool(5);
    std::vector<std::future<int>> f;
    
 
    for (int i = 0; i < firstMatrix.getRowsNumber(); i++)
    {
        f.push_back(pool.enqueue([&firstMatrix, &secondMatrix, &result](int line, Matrix a, Matrix b, Matrix* res) {
            for (int j = 0; j < firstMatrix.getColumnsNumber(); j++)
                result->setCellValue(line, j, (firstMatrix.getCellValue(line, j) + secondMatrix.getCellValue(line, j)));
            return line;
        }, i, firstMatrix, secondMatrix, result));
    }
}

void multiplication(Matrix firstMatrix, Matrix secondMatrix, Matrix* result)
{
    ThreadPool pool(5);
    std::vector<std::future<int>> f;

    for (int i = 0; i < firstMatrix.getRowsNumber(); i++)
        for (int j = 0; j < firstMatrix.getColumnsNumber(); j++)
        {
            f.push_back(pool.enqueue([&firstMatrix, &secondMatrix, &result](int line, int column, Matrix a, Matrix b, Matrix* res) {
                int mul = 0;
                for (int k = 0; k < a.getRowsNumber(); k++)
                    mul += firstMatrix.getCellValue(line, k) * secondMatrix.getCellValue(k, column);
                result->setCellValue(line, column, mul);
                return line;
            }, i, j, firstMatrix, secondMatrix, result));
        }
}

void asyncAddition(Matrix firstMatrix, Matrix secondMatrix, Matrix* result) {
    std::vector<std::future<int>> f;
    
    for (int i = 0; i < firstMatrix.getRowsNumber(); i++)
    {
        f.push_back(async([&firstMatrix, &secondMatrix, &result](int line, Matrix a, Matrix b, Matrix* res) {
            for (int j = 0; j < firstMatrix.getColumnsNumber(); j++)
                result->setCellValue(line, j, (firstMatrix.getCellValue(line, j) + secondMatrix.getCellValue(line, j)));
            return line;
        }, i, firstMatrix, secondMatrix, result));
    }
}

void asyncMultiplication(Matrix firstMatrix, Matrix secondMatrix, Matrix* result)
{
    std::vector<std::future<int>> f;
    
    for (int i = 0; i < firstMatrix.getRowsNumber(); i++)
        for (int j = 0; j < firstMatrix.getColumnsNumber(); j++)
        {
            f.push_back(async([&firstMatrix, &secondMatrix, &result](int line, int column, Matrix a, Matrix b, Matrix* res) {
                int mul = 0;
                for (int k = 0; k < a.getRowsNumber(); k++)
                    mul += firstMatrix.getCellValue(line, k) * secondMatrix.getCellValue(k, column);
                result->setCellValue(line, column, mul);
                return line;
            }, i, j, firstMatrix, secondMatrix, result));
        }
}

int main(int argc, const char * argv[]) {
    const int elementsNumber = 50;
    Matrix firstMatrix = Matrix(elementsNumber, elementsNumber);
    Matrix secondMatrix = Matrix(elementsNumber, elementsNumber);
    Matrix result = Matrix(elementsNumber, elementsNumber);
    
    std::cout << "Matrix a: \n";
    std::cout << firstMatrix.printMatrix();
    
    std::cout << "\nMatrix b: \n";
    std::cout << secondMatrix.printMatrix();
    
    auto start = std::chrono::high_resolution_clock::now();
    
    
    std::cout << "\nMatrix sum: \n";
    //addition(firstMatrix, secondMatrix, &result);
    //multiplication(firstMatrix, secondMatrix, &result);
    asyncAddition(firstMatrix, secondMatrix, &result);
    //asyncMultiplication(firstMatrix, secondMatrix, &result);
    //std::cout << res.printMatrix();
    
    
    auto finish = std::chrono::high_resolution_clock::now();
    std::cout << "\nAddition " << std::chrono::duration<double, std::milli>(finish-start).count() / 1000 << "s\n\n";
    cout << result.printMatrix();
    return 0;
}
