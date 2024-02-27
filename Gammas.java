public class Gammas {
    int[] O; 
    double[][] a;
    double[][] b; // b[i][O[i]]
    int T;
    int N;
    double[][] gamma;
    double[][][] deGamma;
    double denom = 0;

    Gammas(double[][] a, double[][] b, int[] O, int N, int T) {
        this.a = a;
        this.b = b;
        this.O = O;
        this.T = T;
        this.N = N;
        gamma = new double[T][N];
        deGamma = new double[T][N][N];
    }
    public void computeGamma(double[][] alpha, double[][] beta) {
        for(int t = 0; t <= T-2; t++) {
            denom = 0;
            for(int i = 0; i <= N-1; i++) {
                for(int j = 0; j <= N-1; j++) {
                    denom += alpha[t][i] * a[i][j] * b[j][O[t+1]] * beta[t+1][j];
                }
            }
            for(int i = 0; i <= N-1; i++) {
                gamma[t][i] = 0;
                for(int j = 0; j <= N-1; j++) {
                    deGamma[t][i][j] = (alpha[t][i] * a[i][j] * b[j][O[t+1]] * beta[t+1][j]) / denom;
                    gamma[t][i] += deGamma[t][i][j];
                }
            }
        }
        // Special case for gamma[T-1][i]
        denom = 0;
        for(int i = 0; i <= N-1; i++) {
            denom += alpha[T-1][i];
        }
        for(int i = 0; i <= N-1; i++) {
            gamma[T-1][i] = alpha[T-1][i] / denom;
        }
    }  
    public double[][] getGamma() {
        return this.gamma;
    }
    public double[][][] getDeGamma() {
        return this.deGamma;
    }
}
