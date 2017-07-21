#include <iostream>
#include <math.h>
#include <stdio.h>

// Kernel function to add the elements of two arrays
__global__
void add(int n, float *x, float *y)
{
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < 30; j++) {
      y[i] = x[i] + y[i];
      y[i] = x[i] - y[i];
    }
    if ( threadIdx.x % 2 == 0 ) {
      y[i] = x[i] + y[i];
    } else {
      y[i] = x[i] - y[i];
    }
  }
}


int main(int argc, char const *argv[])
{
  int N = 1<<20;
  float *x, *y;

  // Allocate Unified Memory – accessible from CPU or GPU
  cudaMallocManaged(&x, N*sizeof(float));
  cudaMallocManaged(&y, N*sizeof(float));

  // initialize x and y arrays on the host
  for (int i = 0; i < N; i++) {
    x[i] = 1.0f;
    y[i] = 2.0f;
  }

  // Run kernel on 1M elements on the GPU
  int blockSize = atoi(argv[2]);
  int numBlocks = atoi(argv[1]);
  printf("The block size is: %d and the number of blocks is: %d and the amount of work is: %d\n\n",blockSize ,numBlocks , N);
  add<<<numBlocks, blockSize>>>(N, x, y);

  // Wait for GPU to finish before accessing on host
  cudaDeviceSynchronize();

  // Check for errors (all values should be 3.0f)
  //float maxError = 0.0f;
  //for (int i = 0; i < N; i++)
  //  maxError = fmax(maxError, fabs(y[i]-3.0f));
  //std::cout << "Max error: " << maxError << std::endl;

  // Free memory
  cudaFree(x);
  cudaFree(y);
  
  return 0;
}