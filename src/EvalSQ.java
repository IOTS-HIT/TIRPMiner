import SQ.AlgoSQ;
import java.io.IOException;

public class EvalSQ {
    public static void main(String[] args) throws IOException {
        String split = " |,";
        String[] filenames = {
                "../input/ASL.txt", // 0 
                "../input/ASL_BU.txt", // 1 
                "../input/BLOCKS.txt",   // 2 
                "../input/AUSLAN2.txt",  // 3 
                "../input/SKATING.txt",  // 4 
                "../input/PIONEER.txt",  // 5 
                "../input/HEPATITIS.txt",// 6 
                "../input/CONTEXT.txt",  // 7 
                "../input/SYN_5000.txt",     // 8
                "../input/SYN_10000.txt",      // 9
            }; 
        // double[] percents = {0.005, 0.005, 0.005, 0.05, 0.3, 0.3, 0.50, 0.90, 0.5};
        double[][] percents = {
            {0.1,   0.2,   0.3,   0.4,   0.5,   0.6,   0.7},
            {0.2,   0.3,   0.4,   0.5,   0.6,   0.7,   0.8},
            {0.005, 0.010, 0.015, 0.020, 0.025, 0.030, 0.035},
            {0.05,  0.10,  0.15,  0.20,  0.25,  0.30,  0.35},
            {0.4,   0.45,  0.50,  0.55,  0.60,  0.65,  0.70},
            {0.1,   0.15,  0.20,  0.25,  0.30,  0.35,  0.40},
            {0.30,  0.35,  0.40,  0.45,  0.50,  0.55,  0.60},
            {0.45,  0.50,  0.55,  0.60,  0.65,  0.70,  0.75},
            {0.10,  0.15,  0.20,  0.25,  0.30,  0.35,  0.40},
            {0.10,  0.15,  0.20,  0.25,  0.30,  0.35,  0.40},
        };
        
        // choose the dataset
        int index = Integer.parseInt(args[0]);

        // chooes the percents index
        int percent = Integer.parseInt(args[1]);
        
        AlgoSQ algo = new AlgoSQ();
        algo.setParameters(filenames[index], "test", percents[index][percent], split);
        algo.runAlgoSQ();
    }
}