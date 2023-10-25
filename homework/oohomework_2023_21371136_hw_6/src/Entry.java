import java.util.ArrayList;

public class Entry {
    private int num = 0;
    private ArrayList<
            ArrayList<Integer>
            > entry = new ArrayList<>();

    public Entry(int floorNum) {
        for (int i = 0; i < floorNum; i++) {
            entry.add(i, new ArrayList<>());
        }
    }

    public ArrayList<ArrayList<Integer>> getRequests() {
        return entry;
    }

    public int getNum() {
        return num;
    }

    public void addPassenger(Integer to, Integer id) {
        entry.get(to).add(id);
        num++;
    }

    public void removePassenger(Integer to, Integer id) {
        entry.get(to).remove(id);
        num--;
    }
}
