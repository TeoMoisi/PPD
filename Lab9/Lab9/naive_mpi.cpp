//
//  naive_mpi.cpp
//  Lab9
//
//  Created by Teofana Moisi on 16/12/2019.
//  Copyright © 2019 Teofana Moisi. All rights reserved.
//

#include <iostream>
#include <mpi.h>
#include <vector>
#include <time.h>
#include <stdint.h>
#include <stdio.h>
#include <assert.h>
#include <chrono>
using namespace std::chrono;

using namespace std;

const int MAXLENGHT = 5;

pair<vector<int>, vector<int> > generatePolynoms(int n) {
    vector<int> polynom1;
    vector<int> polynom2;

    for (int i = 0; i < n; i++) {
        polynom1.push_back(rand() % MAXLENGHT);
        polynom2.push_back(rand() % MAXLENGHT);
    }

    return make_pair(polynom1, polynom2);
}

void split_work(vector<int> &polynom1, vector<int> &polynom2, int porcessNumber) {
    cout << "Master sends work: \n";

    int lower_bound = polynom1.size();
    int upper_bound = polynom1.size() + polynom2.size() - 1;

    for(int i = 1; i < porcessNumber; ++ i) {
      int left = i * upper_bound / porcessNumber;
      int right = min(upper_bound, (i + 1) * upper_bound / porcessNumber);
      MPI_Bsend(&lower_bound, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
      MPI_Bsend(&left, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
      MPI_Bsend(&right, 1, MPI_INT, i, 2, MPI_COMM_WORLD);
      MPI_Bsend(polynom1.data(), min(right, lower_bound), MPI_INT, i, 3, MPI_COMM_WORLD);
      MPI_Bsend(polynom2.data(), min(right, lower_bound), MPI_INT, i, 4, MPI_COMM_WORLD);
  }
    cout << "Work sent \n";
}

void multiplication(int left, int right, vector <int> &polynom1, vector <int> &polynom2, vector <int> &result) {
  for(int i = left; i < right; ++ i) {
    int upper_bound = min(int(polynom1.size()) - 1, i);
    for(int x = 0; x <= upper_bound; ++ x) {
      int y = i - x;
      if(y >= polynom2.size()) {
        continue;
      }
      result[i - left] += polynom1[x] * polynom2[y];
    }
  }
}

void master_receive(int processNumber, vector <int> &result) {
  cout << "> master collect\n";
  int l = result.size();
  for(int i = 1; i < processNumber; ++ i) {
    MPI_Status status;
    int st = i * l / processNumber;
    int dr = min(l, (i + 1) * l / processNumber);
    MPI_Recv(result.data() + st, dr - st, MPI_INT, i, 5, MPI_COMM_WORLD, &status);
  }
  cout << "> master collected\n";
}

void master_check(vector <int> &polynom1, vector <int> &polynom2, vector <int> &result) {
  cout << "> master check\n";
  vector <int> check(polynom1.size() + polynom2.size() - 1, 0);
  for(int i = 0; i < polynom1.size(); ++ i) {
    for(int j = 0; j < polynom2.size(); ++ j) {
      check[i + j] += polynom1[i] * polynom2[j];
    }
  }

  cout << "\nResult polynom: ";
  for (int i = 0; i < result.size() - 1; i++) {
      cout << result[i] << "X^" << i << " + ";
  }
  cout << result[result.size() - 1] << "X^" << result.size() - 1 << "\n";


  assert(check.size() == result.size());
  for(int i = 0; i < check.size(); ++ i) {
    assert(check[i] == result[i]);
  }
  cout << "> master checked\n";
}

void slave_exec(int myId) {
  cout << "> slave("  << myId << ") started\n";
  int n;
  int st;
  int dr;
  MPI_Status _;
  MPI_Recv(&n, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &_);
  MPI_Recv(&st, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &_);
  MPI_Recv(&dr, 1, MPI_INT, 0, 2, MPI_COMM_WORLD, &_);
  vector <int> a(dr);
  vector <int> b(dr);
  MPI_Recv(a.data(), min(dr, n), MPI_INT, 0, 3, MPI_COMM_WORLD, &_);
  MPI_Recv(b.data(), min(dr, n), MPI_INT, 0, 4, MPI_COMM_WORLD, &_);
  vector <int> res(dr - st, 0);
  multiplication(st, dr, a, b, res);
  MPI_Bsend(res.data(), dr - st, MPI_INT, 0, 5, MPI_COMM_WORLD);
  cout << "> slave("  << myId << ") finished\n";
}


int main(int argc, char** argv) {
    MPI_Init(0, 0);
    int myId;
    int processNumber;
    MPI_Comm_size(MPI_COMM_WORLD, &processNumber);
    MPI_Comm_rank(MPI_COMM_WORLD, &myId);
    
    int n;
    cin >> n;

    if (myId == 0) {
        pair<vector<int>, vector<int> > polynoms = generatePolynoms(n);
        vector<int> polynom1 = polynoms.first;
        vector<int> polynom2 = polynoms.second;

        cout << "First polynom: ";
        for (int i = 0; i < n - 1; i++) {
            cout << polynom1[i] << "X^" << i << " + ";
        }
        cout << polynom1[n - 1] << "X^" << n - 1 << "\n";

        cout << "\nSecond polynom: ";
        for (int i = 0; i < n - 1; i++) {
            cout << polynom2[i] << "X^" << i << " + ";
        }
        cout << polynom2[n - 1] <<"X^" << n - 1 << "\n";


        time_point <high_resolution_clock> start = high_resolution_clock::now();
        split_work(polynom1, polynom2, processNumber);
        int left = 0;
        int right = (2 * n - 1) / processNumber;
        vector <int> result(2 * n - 1);
        multiplication(left, right, polynom1, polynom2, result);
        master_receive(processNumber, result);
        time_point <high_resolution_clock> stop = high_resolution_clock::now();
        master_check(polynom1, polynom2, result);
        cout << duration_cast<milliseconds>(stop - start).count() << "ms\n";
    } else {
      slave_exec(myId);
    }

    MPI_Finalize();
}
