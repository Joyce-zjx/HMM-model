// Forward Algorithm
public class Forward{
    int[] O; 
    double[][] a;
    double[][] b; // b[i][O[i]]
    double[] c;
    double[] pi;
    int N;
    int T;
    double[][] alpha; 

    Forward(double[][] a, double[][] b, double[] c, double[] pi, int[] O, int N, int T) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.pi = pi;
        this.O = O;
        this.N = N;
        this.T = T;
        alpha = new double[T][N];
    }
    public void forAgr() {
        //compute alpha[0][i]
        c[0] = 0;
        for(int i = 0; i <= N - 1; i++) {
            alpha[0][i] = pi[i] * b[i][O[0]];
            c[0] += alpha[0][i];
        }
        // scale alpha[0][i]
        c[0] = 1 / c[0]; 
        for(int i = 0; i <= N - 1; i++) {
            alpha[0][i] *= c[0];
        }
        // compute alpha[t][i]
        for(int t = 1; t <= T - 1; t++) {
            c[t] = 0;
            for(int i = 0; i <= N - 1; i++) {
                alpha[t][i] = 0;
                for(int j = 0; j <= N - 1; j++) {
                    alpha[t][i] += alpha[t-1][j] * a[j][i];
                }             
                alpha[t][i] *= b[i][O[t]];
                c[t] += alpha[t][i];
            }
            // scale alpha[t][i]
            if(c[t] == 0) {
                c[t] = 0.0000000001;
            }
            c[t] = 1 / c[t];
            for(int i = 0; i <= N - 1; i++) {
                alpha[t][i] *= c[t];
            }
        }        
    }
    public double[][] getAlpha() {
        return alpha;
    }
    public double[] getC() {
        return this.c;
    }
}
