public class reEstimation {
    int[] O; 
    double[][] a;
    double[][] b; // b[i][O[i]]
    double[] pi;
    int T;
    int N;
    int M;
    double denom = 0;
    double numer = 0;

    reEstimation(double[][] a, double[][] b, double[] pi, int[] O, int N, int M, int T) {
        this.a = a;
        this.b = b;
        this.pi = pi;
        this.O = O;
        this.T = T;
        this.N = N;
        this.M = M;
    }
    public void reEstm(double[][] gamma, double[][][] deGamma) {
        // re-estimate pi
        for(int i = 0; i <= N - 1; i++) {
            pi[i] = gamma[0][i];
        }
        // re-estimate a
        for(int i = 0; i <= N - 1; i++) {
            denom = 0;
            for(int t = 0; t <= T - 2; t++) {
                denom += gamma[t][i];
            }
            for(int j = 0; j <= N - 1; j++) {
                numer = 0;
                for(int t = 0; t <= T - 2; t++) {
                    numer += deGamma[t][i][j];
                }
                a[i][j] = numer / denom;
            }
        }
        // re-estimate b
        for(int i = 0; i <= N - 1; i++) {
            denom = 0;
            for(int t = 0; t <= T - 1; t++) {
                denom += gamma[t][i];
            }
            for(int j = 0; j <= M - 1; j++) {
                numer = 0;
                for(int t = 0; t <= T - 1; t++) {
                    if(O[t] == j) {
                        numer += gamma[t][i];
                    }
                }
                b[i][j] = numer / denom;
            }
        }
    }
    public double[] getPi() {
        return this.pi;
    }
    public double[][] getA() {
        return this.a;
    }
    public double[][] getB() {
        return this.b;
    }
}