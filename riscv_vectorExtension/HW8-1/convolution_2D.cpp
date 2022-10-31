void convolution_2D(int N[][INPUT_SIZE], int M[][KERNEL_SIZE], int P[][INTPUT_SIZE]) {

// find center position of kernel (half of kernel size)
int kernel_center_X = KERNEL_SIZE / 2;
int kernel_center_Y = KERNEL_SIZE / 2;

for (int i = 0; i < INPUT_SIZE; ++i)              // rows
{
    for (int j = 0; j < INPUT_SIZE; ++j)          // columns
    {
        for (int m = 0; m < KERNEL_SIZE; ++m)     // kernel rows
        {
            for (int n = 0; n < KERNEL_SIZE; ++n) // kernel columns
            {
                int ii = i + (m - kernel_center_X);
                int jj = j + (n - kernel_center_Y);

                // ignore input samples which are out of bound
                if (ii >= 0 && ii < INPUT_SIZE && jj >= 0 && jj < INPUT_SIZE)
                    P[i][j] += N[ii][jj] * M[mm][nn];
            }
        }
    }
}