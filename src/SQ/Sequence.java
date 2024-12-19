package SQ;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

// 记录一个序列 <label set, relation set>
public class Sequence implements Arrangement{
    ArrayList<Integer> labels;    // 记录sequence的所有label
    String relations; // 记录sequence的relations

    Map<Integer, PList> positions; // sid -> position 记录记录每一个label出现的位置

    BitSet sids;

    int before_position;

    public Sequence(ArrayList<Integer> labels, String relations, Map<Integer, PList> positions, BitSet sids, int before_position) {
        this.labels = labels;
        this.relations = relations;
        this.positions = positions;
        this.sids = sids;
        this.before_position = before_position;
    }

    @Override
    public BitSet getBitSet() {
        return sids;
    }

    @Override
    public int getMinPosition(int sid) {
        return positions.get(sid).getMinPosition();
    }

    @Override
    public ArrayList<Integer> getLabels() {
        return labels;
    }

    @Override
    public String getRelations() {
        return relations;
    }

    @Override
    public PList getPLists(int sid) {
        return positions.get(sid);
    }

    @Override
    public boolean isSequence() {
        return true;
    }

    @Override
    public int getBeforePosition() {
        return before_position;
    }
}