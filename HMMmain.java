/**
 * CS 131 Midterm
 * Aurthor: Jiaxin Zhao
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.nio.file.Path;
// For the noise reduction, I modify M value manually (didn’t create any loop for it)
// The final score txt files will be generated after running the code, need to delete the files before next run, otherwise new data would append directly on the previous files (not replace)

public class HMMmain {
    static int T = 0;
    static int N = 0;
    static int M = 0;
    static double[][] a;
    static double[][] b; // b[i][O[i]]
    static double[] pi;
    static double[] c;
    static int[] O; // observation sequence
    static double oldLogProb;
    static double logProb;
    static int iters = 0;  
    static int maxIters = 100; 

    static Path fileName;
    static boolean ifTest = false; 
    static String obsString = "";       
    static ArrayList<File> opcodeDataTrain_byFiles = new ArrayList<File>(); // list of all data from the training folder
    static ArrayList<String> opcodeDataTrain_bySymbol = new ArrayList<String>(); // list of all data from the training folder
    static int[] numOpcodeTrain; // occurence of each unique opcode in training set
    static ArrayList<File> opcodeDataSec = new ArrayList<File>(); // all testing files from the non training folder
    static ArrayList<File> opcodeDataTest1 = new ArrayList<File>(); // partial testing files from the training folder
    static ArrayList<File> opcodeDataTest2 = new ArrayList<File>(); // partial testing files from the non training folder
    static int[] numOpcodeTest; // occurence of each unique opcode in testing set

    static HashMap<String, Integer> opcodeType = new HashMap<String, Integer>();
    static HashMap<Integer, Integer> newOpcodeType = new HashMap<Integer, Integer>(); // for noise reduction
    static boolean isNR = false; // if noise reduction flag
    static boolean isNewObs = true;
    static File Folder;
    static String trainFolder = "/Users/rickylam/Desktop/zbot";
    static String NonTrainFolder = "/Users/rickylam/Desktop/zeroaccess";

    static String scoreFile1 = "score1.txt"; // no NR
    static String scoreFile2 = "score2.txt"; // no NR
    static String scoreFile3 = "score1NR.txt"; // with NR
    static String scoreFile4 = "score2NR.txt"; // with NR
    static ArrayList<Double> testScore1 = new ArrayList<Double>(); // no NR
    static ArrayList<Double> testScore2 = new ArrayList<Double>(); // no NR
    static ArrayList<Double> testScore3 = new ArrayList<Double>(); // with NR
    static ArrayList<Double> testScore4 = new ArrayList<Double>(); // with NR

    public static void readFolder(String folderPath) throws FileNotFoundException {
        File folder = new File(folderPath);
        // read the file data from folders 
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files != null) {
            for (File file : files) {
                try {                    
                    if(folderPath.compareTo(trainFolder) == 0) {
                        Scanner sc = new Scanner(file);
                        while (sc.hasNextLine()) {
                            opcodeDataTrain_bySymbol.add(sc.nextLine());
                        }
                        opcodeDataTrain_byFiles.add(file);
                    } else if(folderPath.compareTo(NonTrainFolder) == 0) {
                        opcodeDataSec.add(file); // store all files from non train folder
                    }  
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }      
    }

    public static void classifyOpcode(ArrayList<String> opcodeData_bySymbol) {
        // store all the unique observations in the whole dataset (consistent)
        int mapInt;
        if(opcodeType.size() == 0) {
            mapInt = -1;
        } else {
            mapInt = opcodeType.size() - 1;
        }
        for(int i = 0; i < opcodeData_bySymbol.size(); i++) {
            obsString = opcodeData_bySymbol.get(i);
            isNewObs = true;
            if(opcodeType.size() == 0) {
                mapInt++;
                opcodeType.put(obsString, mapInt);
            } else {
                for(int j = 0; j < opcodeType.size(); j++) {
                    if(opcodeType.containsKey(obsString)) {
                        isNewObs = false;
                    }
                }
                if(isNewObs) {
                    opcodeType.put(obsString, mapInt++);
                }
            }
        }
    }

    public static void dataPreProcess(String purpose, boolean isNR) {       
        // training
        if(purpose.compareTo("training") == 0) {
            // store 80% of the dataset from training folder for training
            int trainSize = (int)(opcodeDataTrain_bySymbol.size() * 0.8);
            numOpcodeTrain = new int[opcodeType.size()];
            for(int i = 0; i < opcodeType.size(); i++) {
                numOpcodeTrain[i] = 0;
            }
            for(int i = 0; i < trainSize; i++) {
                obsString = opcodeDataTrain_bySymbol.get(i);
                // make a long obs sequence for all training data
                if(!isNR) {
                    O[i] = opcodeType.get(obsString);
                    // count each symbol's occurrence in train set
                    numOpcodeTrain[O[i]] += 1;
                } else {
                    O[i] = newOpcodeType.get(opcodeType.get(obsString));
                }           
            }
        } 
        // For testing
        else if(purpose.compareTo("testing") == 0){
            // number of testing files in each folder
            int numFilesTest = (int)(opcodeDataTrain_byFiles.size() * 0.2);            
            // store files from the training folder (remaining 20%) for testing
            int startFrom = (int)(opcodeDataTrain_byFiles.size() * 0.8);
            for(int i = startFrom; i < opcodeDataTrain_byFiles.size(); i++) {
                opcodeDataTest1.add(opcodeDataTrain_byFiles.get(i));
            }
            // store the same amount of files from the non training folder for testing
            int fold2TestSize = numFilesTest;
            for(int i = 0; i < fold2TestSize; i++) {
                opcodeDataTest2.add(opcodeDataSec.get(i));
            }          
        }
    }

    public static int[] testObserSequence(File testingFile, boolean isNR) throws FileNotFoundException { // every file in opcodeDataTest (testing dataset)
        ArrayList<Integer> Obs = new ArrayList<Integer>(); // new obs sequence
        Scanner sc = new Scanner(testingFile);           
        while (sc.hasNextLine()) {
            obsString = sc.nextLine();
            // make an obs sequence for current testing file
            if(!isNR) {
                Obs.add(opcodeType.get(obsString));
                numOpcodeTest = new int[opcodeType.size()];
                for(int i = 0; i < opcodeType.size(); i++) {
                    numOpcodeTest[i] = 0;
                }
                // count each symbol's occurrence in test set
                numOpcodeTest[opcodeType.get(obsString)] += 1;  
            } else {
                Obs.add(newOpcodeType.get(opcodeType.get(obsString)));
            } 
        } 
        int[] O = new int[Obs.size()];
        for(int i = 0; i < Obs.size(); i++) {
            O[i] = Obs.get(i);
        }
        return O;
    }

    public static void initialization(int N, int M) {
        // initialize λ=(A,B,pi)
        a = new double[N][N];
        b = new double[N][M];
        pi = new double[N];
       
        double x;
        double max = 1;
        double min = 0;
        double sum;
        // initialize pi
        sum = 0;
        for(int i = 0; i < N; i++) {
            x = Math.random() * (max - min) + min; // random value between 0 to 1
            pi[i] = x;
            sum += x;
        }
        for(int i = 0; i < N; i++) {
            pi[i] /= sum;
        }
        // initialize a        
        for(int i = 0; i < N; i++) {
            sum = 0;
            for(int j = 0; j < N; j++) {
                x = Math.random() * (max - min) + min; // random value between 0 to 1
                a[i][j] = x;
                sum += x;
            }
            for(int j = 0; j < N; j++) {
                a[i][j] /= sum;
            }            
        }
        // initialize b
        for(int i = 0; i < N; i++) {
            sum = 0;
            for(int j = 0; j < M; j++) {
                x = Math.random() * (max - min) + min; // random value between 0 to 1
                b[i][j] = x;
                sum += x;
            }
            for(int j = 0; j < M; j++) {
                b[i][j] /= sum;
            }            
        }
    }

    public static void printLambda(double[][] A, double[][] B, double[] PI) {
        System.out.println("\nPi: ");
        for (int i = 0; i < PI.length; i++) {
            if(i == 0) {
                System.out.print("[");
            }
            System.out.print(PI[i]);
            if(i == PI.length - 1) {
                System.out.print("]");
            } else {
                System.out.print(", ");
            }
        }
        System.out.println("\n\nA: ");
        for (int i = 0; i < A.length; i++) { 
            for (int j = 0; j < A[0].length; j++) {
                if(j == 0) {
                    System.out.print("[");
                }
                System.out.print(A[i][j]);
                if(j == A[0].length - 1) {
                    System.out.print("]");
                } else {
                    System.out.print(", ");
                }
            }
            System.out.print("\n");
        }
        System.out.println("\nB: ");
        for (int i = 0; i < B.length; i++) { 
            for (int j = 0; j < B[0].length; j++) {
                if(j == 0) {
                    System.out.print("[");
                }
                System.out.print(B[i][j]);
                if(j == B[0].length - 1) {
                    System.out.print("]");
                } else {
                    System.out.print(", ");
                }
            }
            System.out.print("\n");
        }  
    }

    public static void stopCriteria(double[][] A, double[][] B, double[] PI, int T, String testSet, int N) throws IOException {
        iters = 0;
        boolean getScore = false;
        while(!getScore) {
            // Compute log[P(O|λ)]
            logProb = 0;
            for(int i = 0; i <= T - 1; i++) {
                logProb += Math.log(c[i]);
            }
            logProb = (-1) * logProb;
            // store the score
            if(ifTest) {
                if(testSet.compareTo("test1") == 0) {
                    testScore1.add(logProb);
                }
                if(testSet.compareTo("test2") == 0) {
                    testScore2.add(logProb);
                }
                if(testSet.compareTo("test3") == 0) {
                    testScore3.add(logProb);
                }
                if(testSet.compareTo("test4") == 0) {
                    testScore4.add(logProb);
                }
                break;
            } 
            // To iterate or not
            iters += 1;
            if(iters < maxIters && logProb > oldLogProb) {
                oldLogProb = logProb;
                Forward newAlpha = new Forward(A, B, c, PI, O, N, T);
                newAlpha.forAgr(); // in order to updata c[]
            } else {
                getScore = true;
                // output λ = (pi, A, B)
                printLambda(A,B,PI);
                System.out.println("\nTraining score at N = " + N + ": " + logProb);
            }
        }
    } 

    public static void scoreRecord(String scoreFile, ArrayList<Double> scoreList, int N) throws IOException {
        PrintWriter print = new PrintWriter(new FileWriter(scoreFile, true));
        print.println("\n\nTesting Score at N = " + N + "\n----------------------------------------------------------------------------\n\n");
        for(int i = 0; i < scoreList.size(); i++) {
            print.println(scoreList.get(i));          
        }
        print.close();
    }

    public static HashMap<Integer, Integer> noiseReduction(int custom_M, int[] opCountList) {
        int m = custom_M - 1;
        int[] forSorting = new int[opcodeType.size()];
        // copy the corresponding opcode occurrence list
        forSorting = opCountList;
        // sort the list
        for (int i = 0; i < forSorting.length - 1; i++) {
            int min_idx = i;
            for (int j = i+1; j < forSorting.length; j++) {
                if (forSorting[j] < forSorting[min_idx]) {
                    min_idx = j;
                }
            } 
            int temp = forSorting[min_idx];
            forSorting[min_idx] = forSorting[i];
            forSorting[i] = temp;   
        }
        // reduce the tpye of opcode: decrease M depending on the rank of the opcode occurrence
        // the list of opcodes need to change (store the opcode mapping number)
        int[] opNoChange = new int[m];    
        int k = 0;
        for(int j = 0; j < opCountList.length; j++) {
            if(opCountList[j] == forSorting[k] && k < m) {
                opNoChange[k] = j;
                k++;
            }
        }
        
        HashMap<Integer, Integer> newOpcodeType = new HashMap<Integer, Integer>();
        for(int i = 0; i < opcodeType.size(); i++) {
            newOpcodeType.put(i, m);
        }
        for(int i = 0; i < opNoChange.length; i++) {
            newOpcodeType.put(opNoChange[i], opNoChange[i]);  
        }
        return newOpcodeType;
    }

    public static void main(String[] args) throws IOException { 
        // read training folder
        readFolder(trainFolder);
        classifyOpcode(opcodeDataTrain_bySymbol); // make unique symbol list of training folder              
        // read non training folder 
        readFolder(NonTrainFolder);
        // read opcode in non training folder
        ArrayList<String> opcodeDataSec_bySymbol = new ArrayList<String>();
        for(int i = 0; i < opcodeDataSec.size(); i++) {
            Scanner sc = new Scanner(opcodeDataSec.get(i));           
            while (sc.hasNextLine()) {
                obsString = sc.nextLine();
                opcodeDataSec_bySymbol.add(obsString);     
            } 
        }
        classifyOpcode(opcodeDataSec_bySymbol); // update unique symbol list by reading non training folder
        
        int count = 0;
        while(count <= 1) {
        if(!isNR) {
            System.out.println("\n\nHMM Model Without Noise Reduction: ");
            // set M (without noise reduction)
            M = opcodeType.size();
        } else {
            System.out.println("\n\nHMM Model With Noise Reduction: ");
            // set M (with noise reduction)
            M = 30; // custom
            newOpcodeType = noiseReduction(M, numOpcodeTrain);
        }
        System.out.println("M = " + M);
        // For training
        T = (int)(opcodeDataTrain_bySymbol.size() * 0.8);
        O = new int[T];
        dataPreProcess("training", isNR);
        c = new double[T];
        System.out.println("\nNow is for training: ");
        oldLogProb = Double.NEGATIVE_INFINITY;
        // try different N, start at N = 1
        for(N = 1; N <= 2; N++) {
            initialization(N, M);
            System.out.println("\nInitial Model at N = " + N + ": ");
            printLambda(a,b,pi);
            System.out.println("\n\nTraining Model at N = " + N + ": ");
            // Forward Algr
            Forward Alpha = new Forward(a, b, c, pi, O, N, T);
            Alpha.forAgr();
            double[][] alpha = Alpha.getAlpha();
            c = Alpha.getC(); // c only gets modified in Forward Algr           
            // Backward Algr
            Backward Beta = new Backward(a, b, c, O, N, T);
            double[][] beta = Beta.backAgr();
            // Gammas
            Gammas G = new Gammas(a, b, O, N, T);
            G.computeGamma(alpha, beta);
            double[][] gamma = G.getGamma();
            double[][][] deGamma = G.getDeGamma();
            // Re-Estimation
            reEstimation R = new reEstimation(a, b, pi, O, N, M, T);
            R.reEstm(gamma, deGamma);
            a = R.getA();
            b = R.getB();
            pi = R.getPi();            
            // Stopping Criteria
            stopCriteria(a, b, pi, T, "notTesting", N);
        } 

        // For testing
        if(isNR) {
            newOpcodeType = noiseReduction(M, numOpcodeTest);
        }
        ifTest = true; 
        System.out.println("\n\n\nNow is for testing: ");
        if(!isNR) {
            dataPreProcess("testing", isNR); 
        }      
        // test set 1 (from training folder)    
        System.out.println("testing set 1: ");
        for(N = 1; N <= 2; N++) {
            if(!isNR) {
                testScore1 = new ArrayList<Double>();
            } else {
                testScore3 = new ArrayList<Double>();
            }  
            for(int i = 0; i < opcodeDataTest1.size(); i++) {
                File currFile = opcodeDataTest1.get(i);
                O = testObserSequence(currFile, isNR); // update new obs sequence of current file
                if(O.length > 500) {
                    T = 500;
                }
                T = O.length;
                c = new double[T]; // make new c[] for current testing file data
                // Forward Algr
                Forward Alpha = new Forward(a, b, c, pi, O, N, T);
                Alpha.forAgr();
                c = Alpha.getC(); // c only gets modified in Forward Algr
                // Stopping Criteria
                if(!isNR) {
                    stopCriteria(a, b, pi, T, "test1", N);  
                } else {
                    stopCriteria(a, b, pi, T, "test3", N);  
                }
            }
            if(!isNR) {
                scoreRecord(scoreFile1, testScore1, N); 
            } else {
                scoreRecord(scoreFile3, testScore3, N); 
            }
        }
        
        // test set 2 (from non training folder)
        System.out.println("testing set 2: ");
        for(N = 1; N <= 2; N++) {
            if(!isNR) {
                testScore2 = new ArrayList<Double>();
            } else {
                testScore4 = new ArrayList<Double>();
            }  
            for(int i = 0; i < opcodeDataTest2.size(); i++) {
                File currFile = opcodeDataTest2.get(i);
                O = testObserSequence(currFile, isNR); // update new obs sequence of current file
                if(O.length > 500) {
                    T = 500;
                }
                T = O.length;                          
                c = new double[T]; // make new c[] for current testing file data  
                // Forward Algr
                Forward Alpha = new Forward(a, b, c, pi, O, N, T);
                Alpha.forAgr();
                c = Alpha.getC(); // c only gets modified in Forward Algr
                // Stopping Criteria
                if(!isNR) {
                    stopCriteria(a, b, pi, T, "test2", N); 
                } else {
                    stopCriteria(a, b, pi, T, "test4", N);  
                }
            }
            if(!isNR) {
                scoreRecord(scoreFile2, testScore2, N);
            } else {
                scoreRecord(scoreFile4, testScore4, N); 
            }   
        }  
        isNR = true;
        ifTest = false;
        count++;
        System.out.println("Score save!");
    }
    }   
}



