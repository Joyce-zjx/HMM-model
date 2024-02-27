// Backward Algorithm
public class Backward {
    int[] O; 
    double[][] a;
    double[][] b; // b[i][O[i]]
    double[] c;
    int N;
    int T;
    double[][] beta;

    Backward(double[][] a, double[][] b, double[] c, int[] O, int N, int T) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.O = O;
        this.N = N;
        this.T = T;
        beta = new double[T][N];
    }
    public double[][] backAgr() {
        for(int i = 0; i <= N - 1; i++) {
            beta[T-1][i] = 1;
        }       
        for(int i = 0; i <= N - 1; i++) {
            // beta[T-1][i] = 1;
            beta[T-1][i] = c[T-1];
        }
        // beta pass
        for(int t = T-2; t >= 0; t--) {
            for(int i = 0; i <= N - 1; i++) {
                beta[t][i] = 0;
                for(int j = 0; j <= N - 1; j++) {
                    beta[t][i] += a[i][j] * b[j][O[t+1]] * beta[t+1][j];
                }
                // scale beta[t][i] with same scale factor as alpha[t][i]
                beta[t][i] *= c[t];
            }
        }
        return beta;
    }
}
