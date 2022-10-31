// Lab8-1 main.c
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdint.h>

uint8_t **init_matrix(int dimX, int dimY, char *name){
  uint8_t **mat= malloc(sizeof(uint8_t *)*dimX);
  if (name)
     printf("%s = [\n",name);
  for (int i=0; i <dimX; i++){
     mat[i] = malloc(sizeof(uint8_t)*dimY);
     for (int j=0; j <dimY; j++){
        if (name) {
         mat[i][j] = (uint8_t)(rand() & 0xFF);
         printf("%d ",mat[i][j]);
        }
        else
         mat[i][j] = (uint8_t)(0 & 0xFF);
     }
     if (name)
        printf("\n");
  }
  if (name)
     printf("];\n");
  return mat;
}

void mat_mul(uint8_t **A,
            uint8_t **B,
            uint8_t **C,
            int dimM,
            int dimK,
            int dimN
            ){
    for (int i=0; i< dimM; i++) {
        for (int j=0; j< dimN; j++) {
            for (int k=0; k< dimK; k++){
                C[i][j] += A[i][k]*B[k][j];
            }
        }
    }
}


int main(int argc, char *argv[]){

int M = 3;
int K = 2;
int N = 3;
uint8_t **A;
uint8_t **B;
uint8_t **C;

srand(time(NULL));   // Initialization, should only be called once.

A = init_matrix(M, K, "A");
B = init_matrix(K, N, "B");
C = init_matrix(M, N, NULL);

mat_mul(A,B,C, M, K, N);

printf("C = [\n");
for (int i=0; i <M; i++){
   for (int j=0; j <N; j++){
        printf("%d ",C[i][j]);
   }
   printf("\n");
}
printf("];\n");
}