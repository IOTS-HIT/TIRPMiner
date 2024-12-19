package SQ;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.Collections;

public class ITable implements Arrangement {
    ArrayList<Integer> label;    // 记录sequence的所有label

    Map<Integer, ArrayList<Integer>> positions; // sid -> position 记录记录每一个label出现的位置

    BitSet sids;

    public ITable(int label, Map<Integer, ArrayList<Integer>> positions, BitSet sids) {
        this.label = new ArrayList<>(Collections.singletonList(label));
        this.positions = positions;
        this.sids = sids;
    }  

    @Override
    public BitSet getBitSet() {
        return sids;
    }

    @Override
    public int getMinPosition(int sid) {
        return positions.get(sid).get(0);
    }

    @Override
    public ArrayList<Integer> getLabels() {
        return label;
    }

    @Override
    public String getRelations() {
        return "";
    }

    @Override
    public PList getPLists(int sid) {
        PList result = new PList(new ArrayList<>(Collections.singletonList(positions.get(sid))));
        ArrayList<Integer> labels_position = new ArrayList<>(positions.get(sid));
        for (int i = 0; i < labels_position.size(); i++) {
            result.addPosition(i, -1);
        }
        return result;
    }

    @Override
    public boolean isSequence() {
        return false;
    }

    @Override
    public int getBeforePosition() {
        return -1;
    }
}
