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

void karatsuba(int *a, int *b, int *ret, int n) {
  if (n <= 4) {
    for(int i = 0; i < 2 * n; ++ i) {
      ret[i] = 0;
    }
    for(int i = 0; i < n; ++ i) {
      for(int j = 0; j < n ;++ j) {
        ret[i + j] += a[i] * b[j];
      }
    }
    return;
  }
  int i;
  int *ar = &a[0];                 // low-order half of a
  int *al = &a[n / 2];             // high-order half of a
  int *br = &b[0];                 // low-order half of b
  int *bl = &b[n / 2];             // high-order half of b
  int *asum = &ret[n * 5];         // sum of a's halves
  int *bsum = &ret[n * 5 + n / 2]; // sum of b's halves
  int *x1 = &ret[n * 0];           // ar*br's location
  int *x2 = &ret[n * 1];           // al*bl's location
  int *x3 = &ret[n * 2];           // asum*bsum's location

  for (i = 0; i < n / 2; i++)
  {
    asum[i] = al[i] + ar[i];
    bsum[i] = bl[i] + br[i];
  }

  karatsuba(ar, br, x1, n / 2);
  karatsuba(al, bl, x2, n / 2);
  karatsuba(asum, bsum, x3, n / 2);

  for (i = 0; i < n; i++)
    x3[i] = x3[i] - x1[i] - x2[i];
  for (i = 0; i < n; i++)
    ret[i + n / 2] += x3[i];
}

void multiplication(int st, int dr, vector <int> &a, vector <int> &b, vector <int> &res) {
  karatsuba(a.data(), b.data(), res.data(), a.size());
}

inline void send_work(vector <int> &a, vector <int> &b, int processNumber) {
  cout << "> master sends work\n";
  int n = a.size();
  for(int i = 1; i < processNumber; ++ i) {
    int left = i * n / processNumber;
    int right = min(n, (i + 1) * n / processNumber);
    MPI_Ssend(&n, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
    MPI_Ssend(&left, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
    MPI_Ssend(&right, 1, MPI_INT, i, 2, MPI_COMM_WORLD);
    MPI_Ssend(a.data() + left, right - left, MPI_INT, i, 3, MPI_COMM_WORLD);
    MPI_Ssend(b.data(), n, MPI_INT, i, 4, MPI_COMM_WORLD);
  }
  cout << "> master sent work\n";
}

inline void karatsuba_wrapper(int left, int right, vector <int> &a, vector <int> &b, vector <int> &result) {
  karatsuba(a.data(), b.data(), result.data(), a.size());
}

inline void master_receive(int n, int processNumber, vector <int> &result) {
  cout << "> master collect\n";
  vector <int> aux(2 * n - 1);
  for(int i = 1; i < processNumber; ++ i) {
    MPI_Status _;
    int left = i * n / processNumber;
    int right = min(n, (i + 1) * n / processNumber);
    MPI_Recv(aux.data(), 2 * n - 1, MPI_INT, i, 5, MPI_COMM_WORLD, &_);
    for (int i = 0; i < 2 * n - 1; ++ i) {
      result[i] += aux[i];
    }
  }
  cout << "> master collected\n";
}

inline void master_check(vector <int> &a, vector <int> &b, vector <int> &result) {
  cout << "> master check\n";
  vector <int> check(a.size() + b.size() - 1, 0);
  for(int i = 0; i < a.size(); ++ i) {
    for(int j = 0; j < b.size(); ++ j) {
      check[i + j] += a[i] * b[j];
    }
  }
  assert(check.size() == result.size());
  for(int i = 0; i < check.size(); ++ i) {
    assert(check[i] == result[i]);
  }

  cout << "\nResult polynom: ";
  for (int i = 0; i < result.size() - 1; i++) {
      cout << result[i] << "X^" << i << " + ";
  }
  cout << result[result.size() - 1] << "X^" << result.size() - 1 << "\n";

  cout << "> master checked\n";
}

inline void worker_exec(int myId) {
  cout << "> slave("  << myId << ") started\n";
  int n;
  int left;
  int right;
  MPI_Status _;
  MPI_Recv(&n, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, &_);
  MPI_Recv(&left, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &_);
  MPI_Recv(&right, 1, MPI_INT, 0, 2, MPI_COMM_WORLD, &_);
  vector <int> a(n, 0);
  vector <int> b(n, 0);
  MPI_Recv(a.data() + left, right - left, MPI_INT, 0, 3, MPI_COMM_WORLD, &_);
  MPI_Recv(b.data(), n, MPI_INT, 0, 4, MPI_COMM_WORLD, &_);
  vector <int> result(6 * n, 0);
  multiplication(left, right, a, b, result);
  MPI_Ssend(result.data(), 2 * n - 1, MPI_INT, 0, 5, MPI_COMM_WORLD);
  cout << "> slave("  << myId << ") finished\n";
}

int main(int argc, char* argv[]) {
  MPI_Init(0, 0);

  int myId;
  int processNumber;
  MPI_Comm_size(MPI_COMM_WORLD, &processNumber);
  MPI_Comm_rank(MPI_COMM_WORLD, &myId);

  unsigned int n;
  if (argc != 2 || 1 != sscanf(argv[1], "%u", &n)) {
    fprintf(stderr, "usage: mpi_kar <n>\n");
    return 1;
  }

  if (myId == 0) {
    pair<vector<int>, vector<int> > polynoms = generatePolynoms(n);
    vector<int> polynom1 = polynoms.first;
    vector<int> polynom2 = polynoms.second;

    while(n & (n - 1)) {
      ++ n;
      polynom1.push_back(0);
      polynom2.push_back(0);
    }

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
    send_work(polynom1, polynom2, processNumber);
    int left = 0;
    int right = n / processNumber;
    vector <int> aux(polynom1);
    for(int i = right; i < aux.size(); ++ i) {
      aux[i] = 0;
    }
    vector <int> result(6 * n);
    multiplication(left, right, aux, polynom2, result);
    master_receive(n, processNumber, result);
    result.resize(2 * n - 1);
    time_point <high_resolution_clock> stop = high_resolution_clock::now();
    master_check(polynom1, polynom2, result);
    cout << duration_cast<milliseconds>(stop - start).count() << "ms\n";
  } else {
    worker_exec(myId);
  }

  MPI_Finalize();
}
