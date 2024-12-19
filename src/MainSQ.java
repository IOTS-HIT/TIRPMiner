import SQ.AlgoSQ;

import java.io.IOException;

public class MainSQ {
    public static void main(String[] args) throws IOException {
        String split = " |,";
        String[] filenames = {
                "../input/asl.txt",      // 0 
                "../input/aslbu.txt",    // 1 
                "../input/blocks.txt",   // 2 
                "../input/auslan2.txt",  // 3 
                "../input/skating.txt",  // 4 
                "../input/pioneer.txt",  // 5 
                "../input/hepatitis.txt",// 6 
                "../input/context.txt",  // 7 
                "../input/SYN_5000.txt", // 8
                "../input/SYN_10000.txt",// 9
            }; 
        //                    0    1    2      3    4    5     6     7    8    9   
        double[] percents = {0.1, 0.2, 0.035, 0.01, 0.4, 0.1, 0.40, 0.45, 0.1, 0.1};
        int index = 1;
        AlgoSQ algo = new AlgoSQ();
        algo.setParameters(filenames[index], "test", percents[index], split);
        algo.runAlgoSQ();
    }
}
