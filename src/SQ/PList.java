package SQ;

import java.util.ArrayList;

public class PList {
    ArrayList<ArrayList<Integer>> positions; // position 记录每一个label出现的所有位置

    ArrayList<Long> position_indexes;

    ArrayList<Integer> max_timestamps;  // max timestamp 记录模式最大的时间戳

    public PList(ArrayList<ArrayList<Integer>> positions) {
        this.positions = positions;
        this.max_timestamps = new ArrayList<>();
        this.position_indexes = new ArrayList<>();
    }

    public ArrayList<ArrayList<Integer>> GetAllPositions() {
        return positions;
    }

    public void addPosition(long index, int max_timestamps) {
        this.position_indexes.add(index);
        this.max_timestamps.add(max_timestamps);
    }

    public int getMinPosition() {
        int len = positions.size();
        return positions.get(len - 1).get(0);
    }
}
