package SQ;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AlgoSQ {
    // ********** 程序参数 **********//
    // 输入文件
    String input;

    // 输出文件
    String output;

    // 支持度阈值
    double minsup = 0;

    double percent = 0;

    String split; // 数据库分隔符

    // ********** 统计信息 **********//
    // record for calculate program run time
    double startTimestamp = 0;
    double endTimestamp = 0;
    double maxMemoryUsage;
    long totalCount = 0; // 记录一共多少频繁序列
    long totalSeq = 0; // 记录一共多少序列
    long maxSeqLen = 0; // 记录最长的频繁模式的长度
    long SeqLen1Count = 0; // 记录长度为1的频繁序列的大小

    // ********** 读取的数据结构 **********//
    // label -> support: 记录label的支持度，临时结构
    Map<Integer, Integer> label2sup = new HashMap<>();

    // 数据库
    Map<Integer, ArrayList<Event>> database = new HashMap<>();

    // label -> ITable: 记录label所在的行号
    Map<Integer, ITable> label2seq = new HashMap<>();

    // 所有长度为1的频繁的label
    ArrayList<Integer> one = new ArrayList<>();

    // 设置程序参数
    public void setParameters(String input, String output, double percent, String split) {
        this.input = input;
        this.output = output;
        this.percent = percent;
        this.split = split;
        this.totalCount = 0;
    }

    // 主程序
    public void runAlgoSQ() throws IOException {
        MemoryUsageMonitor monitor = new MemoryUsageMonitor();
        monitor.start();

        startTimestamp = System.currentTimeMillis();

        // 读数据库，填充database, label2Line, label2sup
        readDatabase();
        minsup = totalSeq * percent;
        System.out.println("Minsup: " + minsup);

        // 获得所有长度为1的频繁event label, 放入one中
        calculateAllL1Seq();
        SeqLen1Count = label2seq.size();

        // 扩展序列 1 -> n
        for (Integer label1 : one) {
            for (Integer label2 : one) {
                findPattern(label2seq.get(label1), label2, 1);
            }
        }

        endTimestamp = System.currentTimeMillis();

        maxMemoryUsage = monitor.getMaxMemoryUsage();
        monitor.stopMonitoring();
        printStatistics();
    }

    // len表示seq的长度，seq表示当前序列， label表示需要扩展的event label
    private void findPattern(Arrangement seq1, int label, int len) {
        ITable seq2 = label2seq.get(label);
        BitSet lines = retainLine(seq1, seq2); // 计算seq和label共同存在的行

        // 如何交集的行数小于minsup，直接return
        int totalSidNumber = lines.cardinality();
        if (minsup > totalSidNumber) {
            return;
        }

        Map<String, Map<Integer, PList>> rela2position = new HashMap<>(); // relation -> line -> position
        Map<String, BitSet> rela2sids = new HashMap<>(); // relations -> sid
        Map<String, Integer> rela2sup = new HashMap<>(); // relation -> support
        Map<String, Integer> rela2before = new HashMap<>(); // relation -> before position
        int maxSup = 0; //记录当前最大的支持度
        int count = 0; // 记录当前已经计算过多少的序列
        // 构建长度为len的sequence
        int sid = lines.nextSetBit(0);
        while (sid != -1) {
            count += 1;
            ArrayList<Event> line = database.get(sid);
            PList pos1 = seq1.getPLists(sid);
            ArrayList<Integer> pos2 = seq2.positions.get(sid); // label在line中所有的位置

            // 找到大于等于min_position的所有位置
            int min_position = seq1.getMinPosition(sid);
            int index = Collections.binarySearch(pos2, min_position);
            int startPos = index >= 0 ? index + 1 : -(index + 1); 
            if (startPos >= pos2.size()) {
                sid = lines.nextSetBit(sid + 1);
                continue;
            }
            List<Integer> position2 = pos2.subList(startPos, pos2.size());

            ArrayList<Long> position_indexes = pos1.position_indexes;
            for(int i = 0; i < position_indexes.size(); i++) {
                Long position_index = position_indexes.get(i);
                for (int j = 0; j < position2.size(); j++) {
                    // p1和p2是event在line中的位置
                    Integer p1 = pos1.positions.get(len-1).get((int)(position_index % pos1.positions.get(len-1).size()));
                    Integer p2 = position2.get(j);
                    if (p1 >= p2) { // p1在p2的后面
                        continue;
                    }

                    // 计算关系
                    AtomicInteger max_timestamp = new AtomicInteger(-1);
                    AtomicInteger before_position = new AtomicInteger(-1);
                    String rela;
                    if(!seq1.isSequence()) {
                        rela = calculateRelation1(line, p1, p2, max_timestamp, before_position);
                    } else {
                        rela = calculateRelation2(seq1.getRelations(), len, pos1, i, position_index,  line, p1, p2, max_timestamp, before_position, seq1.getBeforePosition());
                    }

                    // 添加rela的待选序列中
                    if (!rela2position.containsKey(rela)) {
                        rela2position.put(rela, new HashMap<>());
                        rela2sids.put(rela, new BitSet());
                        rela2sup.put(rela, 1);
                        rela2before.put(rela, before_position.get());
                    }
                    if (!rela2position.get(rela).containsKey(sid)) {
                        @SuppressWarnings("unchecked")
                        ArrayList<ArrayList<Integer>> positions = (ArrayList<ArrayList<Integer>>) pos1.positions.clone(); 
                        positions.add(new ArrayList<>(position2));

                        rela2position.get(rela).put(sid, new PList(new ArrayList<>(positions)));
                        rela2sids.get(rela).set(sid);
                        
                        // 计算支持度，用于快速剪枝
                        rela2sup.compute(rela, (k, v) -> v + 1);
                        if(rela2sup.get(rela) > maxSup){
                            maxSup = rela2sup.get(rela);
                        }
                    }

                    long next_index = position_index * position2.size() + j;
                    rela2position.get(rela).get(sid).addPosition(next_index, max_timestamp.get());
                }
                if (maxSup < minsup && maxSup + totalSidNumber - count < minsup) {
                    return;
                }
            }
            sid = lines.nextSetBit(sid + 1);
        }
        // 进行剪枝, 删除支持度小于minsup的那些关系
        rela2position.entrySet().removeIf(entry -> entry.getValue().size() < minsup);

        for (String rela : rela2position.keySet()) {
            // 合并label
            @SuppressWarnings("unchecked")
            ArrayList<Integer> labels = (ArrayList<Integer>) seq1.getLabels().clone();
            labels.add(label);

            // 输出序列
            // System.out.println("pattern: " + labels + ", relations: " + rela + ", support: " + rela2position.get(rela).size());
            maxSeqLen = Math.max(maxSeqLen, labels.size());
            totalCount++;

            // 扩展序列
            Sequence nextseq = new Sequence(labels, seq1.getRelations() + rela, rela2position.get(rela), rela2sids.get(rela), rela2before.get(rela));
            for (Integer nextLabel : one) {
                findPattern(nextseq, nextLabel, len + 1);
            }
        }
    }

    // 1 + 1 -> 2
    public String calculateRelation1(ArrayList<Event> line, int p1, int p2, AtomicInteger max_timestamps, AtomicInteger before_positions) {
        Event e1 = line.get(p1);
        Event e2 = line.get(p2);
        char relationP1P2 = relation(e1, e2);
        max_timestamps.set(Math.max(e1.end, e2.end));
        if(relationP1P2 == '1') {
            before_positions.set(0);
        }
        return String.valueOf(relationP1P2);
    }

    // n + 1 -> n+1
    public String calculateRelation2(String relations, int len, PList plist, int p1_index, Long index, ArrayList<Event> line, int p1, int p2, AtomicInteger max_timestamps, AtomicInteger before_positions, int prev_before_position) {
        int rlen = relations.length(); // 记录关系的长度
        char[] relaNew = new char[len];

        Event e1 = line.get(p1);
        Event e2 = line.get(p2);
        char relationP1P2 = relation(e1, e2);

        max_timestamps.set(Math.max(plist.max_timestamps.get(p1_index), e2.end));

        if (relationP1P2 == '6') {
            for(int i = 0; i < len-1; i++) {
                relaNew[i] = relations.charAt(rlen - len + 1 + i);
            }
            relaNew[len-1] = '6';
            before_positions.set(prev_before_position);
        } else if (relationP1P2 == '1' && e1.end >= plist.max_timestamps.get(p1_index)) { 
            Arrays.fill(relaNew, '1');
            before_positions.set(len-1);
        } else {
            ArrayList<Integer> l1 = new ArrayList<>();
            long prev_len = 1;
            for (int i = plist.positions.size()-1; i >= 0; i--) {
                int cur_len = plist.positions.get(i).size();
                int p =(int) ((index / prev_len) % cur_len);
                l1.add(plist.positions.get(i).get(p));
                prev_len *= cur_len;
            }
            Collections.reverse(l1);
            Arrays.fill(relaNew, '1');
            int before_position = prev_before_position;
            int max = before_position;
            for (int i = before_position+1; i < len-1; i++) {
                relaNew[i] = relation(line.get(l1.get(i)), e2);
                if (relaNew[i] == '1' && (max == i-1 || (max == before_position && i == before_position+1))) {
                    max = i;
                }
            }
            before_positions.set(max);
            relaNew[len-1] = relationP1P2;
        }
        return String.valueOf(relaNew);
    }

    // 读取一行数据并进行处理
    public BitSet retainLine(Arrangement q1, ITable q2) {
        BitSet sids = (BitSet) q1.getBitSet().clone();
        sids.and(q2.sids);
        return sids;
    }

    // 获得所有长度为1的频繁模式，放在one里面
    private void calculateAllL1Seq() {
        for (Map.Entry<Integer, Integer> e : label2sup.entrySet()) {
            if (e.getValue() >= minsup) {
                one.add(e.getKey());
            } else {
                // 删除所有不频繁的label
                label2seq.remove(e.getKey());
            }
        }
        label2sup.clear();
    }

    // 读取整个数据库
    public void readDatabase() throws IOException {
        String thisLine;
        int currentSeqId = -1; // 记录当前行的行ID
        ArrayList<Event> currentSeq = new ArrayList<>(); // 记录当前的序
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.input)))) {
            while ((thisLine = reader.readLine()) != null) {
                String[] tokens = thisLine.split(split);
                int sid = Integer.parseInt(tokens[0]); // 记录序列id
                int label = Integer.parseInt(tokens[1]); // 记录label
                int start = Integer.parseInt(tokens[2]); // 记录开始时间
                int end = Integer.parseInt(tokens[3]); // 记录结束时间

                // 最开始的序列id
                if (currentSeqId == -1) {
                    currentSeqId = sid;
                }

                // 如果sid不等于currentSeqId，说明当前序列已经全部读入进来了，进行解析工作
                if (sid != currentSeqId) {
                    parseSequence(currentSeqId, currentSeq);
                    currentSeqId = sid;
                    currentSeq.clear();
                }

                // 添加到当前序列中
                currentSeq.add(new Event(label, start, end));
            }
            // 解析最后一个sequence
            parseSequence(currentSeqId, currentSeq);
            currentSeq.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 解析一行数据
    public void parseSequence(int sid, ArrayList<Event> sequence) {
        totalSeq++;
        Collections.sort(sequence);
        // 记录每一行出现的label的种类，用于计算支持度
        Set<Integer> noDuplicate = new HashSet<>();
        for (int i = 0; i < sequence.size(); i++) {
            int label = sequence.get(i).label;

            // 收集每个label所在的行
            if (!label2seq.containsKey(label)) {
                label2seq.put(label, new ITable(label, new HashMap<>(), new BitSet()));
            }
            ITable list = label2seq.get(label);
            if (!list.positions.containsKey(sid)) {
                list.positions.put(sid, new ArrayList<>());
                list.sids.set(sid);
            }
            list.positions.get(sid).add(i);

            // 计算所有label的支持度
            if (!noDuplicate.contains(label)) {
                noDuplicate.add(label);
                label2sup.merge(label, 1, Integer::sum);
            }
        }
        // System.out.println("sid: " + sid + sequence);
        database.put(sid, new ArrayList<>(sequence));
    }

    // 定义各种关系
    // 1: before, 2: meets, 3: overlaps, 4: contains, 5: finish-by, 6: equal, 7:
    // stars
    public char relation(Event a, Event b) {
        if (a.start == b.start && a.end == b.end) {
            return '6';
        } else if (a.start == b.start && b.end > a.end) {
            return '7';
        } else if (a.end == b.end && b.start > a.start) {
            return '5';
        } else if (b.start > a.start && a.end > b.end) {
            return '4';
        } else if (a.end > b.start && b.start > a.start) {
            return '3';
        } else if (b.start == a.end) {
            return '2';
        } else if (b.start > a.end) {
            return '1';
        } else {
            System.out.println("Error: relation");
            return '0';
        }
    }

    // 打印挖掘信息
    public void printStatistics() {
        System.out.println("================ AgoSQ - STATS =======================");
        System.out.println(" Filename: " + input);
        System.out.println(" Total Sequence: " + totalSeq);
        System.out.println(" Percent: " + this.percent);
        System.out.println(" Total Time ~ " + (endTimestamp - startTimestamp) / 1000 + " s");
        System.out.println(" Max Memory ~ " + maxMemoryUsage + " MB");
        System.out.println(" Total Freq Sequence: " + totalCount);
        System.out.println(" Max Sequence Len: " + maxSeqLen);
        System.out.println(" 1-Sequence num: " + SeqLen1Count);
        System.out.println("========================================================" + " \n");
    }
}


// // n + 0 -> n+1
// public String calculateRelation1(String relations, PList plist, int p1_index, Long index, ArrayList<Event> line, int p1, int p2, AtomicInteger max_timestamps, AtomicInteger before_positions) {
//     ArrayList<Integer> l0 = new ArrayList<>();
//     int prev_len = 0;
//     for (int i = plist.positions.size()-2; i >= 0; i--) {
//         int cur_len = plist.positions.get(i).size();
//         l0.add((int)((index / prev_len) % cur_len));
//         prev_len *= cur_len;
//     }
//     Collections.reverse(l0);
//     int len = l0.size(); // 带扩展序列的长度
//     char[] relaNew = new char[len];
//     Event e1 = line.get(p2);
//     Arrays.fill(relaNew, '0');
//     for (int i = -1; i < len; i++) {
//         relaNew[i] = relation(line.get(plist.positions.get(i).get(l0.get(i))), e2);
//     }
//     return String.valueOf(relaNew);
// }