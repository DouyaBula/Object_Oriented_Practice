import com.oocourse.spec3.main.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

public class Block {
    private int blockNum;
    private int blockIndex;
    private HashMap<Person, Integer> person2Block;
    private HashMap<Integer, HashSet<Person>> block2Persons;

    public Block() {
        this.blockNum = 0;
        this.blockIndex = 0;
        this.person2Block = new HashMap<>();
        this.block2Persons = new HashMap<>();
    }

    public HashSet<Person> getBlock(Person dude) {
        if (!person2Block.containsKey(dude)) {
            HashSet<Person> block = new HashSet<>();
            block.add(dude);
            return block;
        }
        int block = person2Block.get(dude);
        return block2Persons.get(block);
    }

    private void addPairSub(Person dude1, Person dude2, int block) {
        person2Block.put(dude1, block);
        person2Block.put(dude2, block);
        block2Persons.put(block, new HashSet<>());
        block2Persons.get(block).add(dude1);
        block2Persons.get(block).add(dude2);
    }

    private void addPairSub(Person dude, int block) {
        person2Block.put(dude, block);
        block2Persons.get(block).add(dude);
    }

    public void addPair(Person dude1, Person dude2) {
        int dude1Block = person2Block.getOrDefault(dude1, -1);
        int dude2Block = person2Block.getOrDefault(dude2, -1);
        blockNum--;
        // 两者均未入群: 新建.
        if (dude1Block == -1 && dude2Block == -1) {
            blockIndex++;
            addPairSub(dude1, dude2, blockIndex);
            return;
        }
        // 两者其一未入群: 加入.
        if (dude1Block == -1) {
            addPairSub(dude1, dude2Block);
            return;
        }
        if (dude2Block == -1) {
            addPairSub(dude2, dude1Block);
            return;
        }
        // 两者皆已入群, 但是群友: 跳过.
        if (dude1Block == dude2Block) {
            blockNum++;
            return;
        }
        // 两者皆已入群, 但不是群友: 合并.
        HashSet<Person> persons1 = block2Persons.get(dude1Block);
        HashSet<Person> persons2 = block2Persons.get(dude2Block);
        if (persons1.size() <= persons2.size()) {
            for (Person dude : persons1) {
                person2Block.replace(dude, dude2Block);
            }
            persons2.addAll(persons1);
            block2Persons.remove(dude1Block);
        } else {
            for (Person dude : persons2) {
                person2Block.replace(dude, dude1Block);
            }
            persons1.addAll(persons2);
            block2Persons.remove(dude2Block);
        }
    }

    public void delPair(Person dude1, Person dude2) {
        // from: dude1, to: dude2
        // dismiss dude1 - dude2
        int originalBlock = person2Block.get(dude1);
        MyPerson from = (MyPerson) dude1;
        MyPerson to = (MyPerson) dude2;

        for (Person dude : block2Persons.get(originalBlock)) {
            ((MyPerson) dude).setFlag(false);
        }
        // 若无新的可达路径, 则分裂为新的一组。
        ArrayList<Person> bfsRecord = new ArrayList<>();
        if (!bfs(from, to, bfsRecord)) {
            blockNum++;
            blockIndex++;
            block2Persons.put(blockIndex, new HashSet<>());
            for (Person dude : bfsRecord) {
                block2Persons.get(originalBlock).remove(dude);
                block2Persons.get(blockIndex).add(dude);
                person2Block.replace(dude, blockIndex);
            }
        }
    }

    private boolean bfs(Person from, Person to, ArrayList<Person> bfsRecord) {
        ArrayList<Person> bfsQueue = new ArrayList<>();
        ((MyPerson) from).setFlag(true);
        bfsQueue.add(from);
        while (!bfsQueue.isEmpty()) {
            MyPerson subFrom = (MyPerson) bfsQueue.get(0);
            for (Person dude : subFrom.getAc()) {
                if (dude.equals(to)) {
                    return true;
                }
                MyPerson myDude = (MyPerson) dude;
                if (!myDude.getFlag()) {
                    myDude.setFlag(true);
                    bfsQueue.add(dude);
                }
            }
            bfsRecord.add(bfsQueue.get(0));
            bfsQueue.remove(0);
        }
        return false;
    }

    public boolean sameBlock(Person dude1, Person dude2) {
        return dude1.equals(dude2) ||
                person2Block.getOrDefault(dude1, -1)
                        .equals(person2Block.getOrDefault(dude2, -2));
    }

    public void blockNumUp() {
        blockNum++;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public void print() {
        for (Map.Entry<Integer, HashSet<Person>> entry :
                block2Persons.entrySet()) {
            System.out.print("Block" + entry.getKey() + ":");
            for (Person dude :
                    entry.getValue()) {
                System.out.print(" " + dude.getId());
            }
            System.out.println("");
        }
        System.out.println("and " +
                (blockNum - block2Persons.keySet().size())
                + " individual block.");
    }

    HashMap<Person, Integer> dijkstra(Person root) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        HashSet<Person> unsolved = new HashSet<>();
        HashMap<Person, Integer> solved = new HashMap<>();
        HashSet<Person> people = getBlock(root);
        for (Person dude : people) {
            unsolved.add(dude);
            solved.put(dude, Integer.MAX_VALUE);
        }
        solved.replace(root, 0);
        queue.add(new Node(root, 0));
        ((MyPerson) root).setPrev(root);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            Person slt = node.getPerson();
            if (!unsolved.contains(slt)) {
                continue;
            }
            int val = node.getDis();
            unsolved.remove(slt);
            solved.replace(slt, val);
            for (Person dude : ((MyPerson) slt).getAc()) {
                if (unsolved.contains(dude)) {
                    int newVal = solved.get(slt) + slt.queryValue(dude);
                    if (newVal < solved.get(dude)) {
                        ((MyPerson) dude).setPrev(
                                slt.equals(root) ? dude : slt);
                        solved.replace(dude, newVal);
                        queue.add(new Node(dude, newVal));
                    }
                }
            }
        }
        return solved;
    }
}
