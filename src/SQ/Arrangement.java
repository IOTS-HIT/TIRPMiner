package SQ;

import java.util.BitSet;
import java.util.ArrayList;

public interface Arrangement {
    BitSet getBitSet();
    PList getPLists(int sid);
    int getMinPosition(int sid); 
    ArrayList<Integer> getLabels();
    String getRelations();
    boolean isSequence();
    int getBeforePosition();
}
