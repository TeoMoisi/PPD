//
//  Matrix.hpp
//  Lab3
//
//  Created by Teofana Moisi on 27/10/2019.
//  Copyright © 2019 Teofana Moisi. All rights reserved.
//

#ifndef Matrix_hpp
#define Matrix_hpp

#include <stdio.h>
#include <vector>
#include <string>

using namespace std;

class Matrix {
    private:
        vector<vector<int>> values;
        int rows, columns;
    
    public:
        Matrix();
        Matrix(int rows, int columns);
        string printMatrix();
        int getColumnsNumber();
        int getRowsNumber();
        void setCellValue(int row, int column, int value);
        int getCellValue(int row, int column);
    
        ~Matrix();
    
    
};

#endif /* Matrix_hpp */
