import java.util.ArrayList;
import java.util.HashMap;

public class Rotator {
    private int elevatorNum;
    private int packSize;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private ArrayList<ArrayList<Boolean>> accessibility;
    private int index;

    public Rotator(int elevatorNum, int packSize,
                   HashMap<Integer, Boolean> elevatorMaintained,
                   ArrayList<ArrayList<Boolean>> accessibility) {
        this.elevatorNum = elevatorNum;
        this.packSize = packSize;
        this.elevatorMaintained = elevatorMaintained;
        this.accessibility = accessibility;
        this.index = 0;
    }

    public void elevatorNumIncrement() {
        this.elevatorNum++;
    }

    // 轮转到下一个可在to停靠的电梯
    public synchronized int next(int to) {
        index = (index + 1) % elevatorNum;
        while (elevatorMaintained.get(index) != null
                || !accessibility.get(index).get(to)) {
            index = (index + 1) % elevatorNum;
        }
        return index;
    }
}