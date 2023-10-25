import java.util.HashMap;

public class Rotator {
    private int elevatorNum;
    private int packSize;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private int index;
    private int counter;

    public Rotator(int elevatorNum, int packSize,
                   HashMap<Integer, Boolean> elevatorMaintained) {
        this.elevatorNum = elevatorNum;
        this.packSize = packSize;
        this.elevatorMaintained = elevatorMaintained;
        this.index = 0;
        this.counter = 0;
    }

    public void elevatorNumIncrement() {
        this.elevatorNum++;
    }

    public int next() {
        counter++;
        // 达到分配包大小后, 向下一个电梯分配
        if (counter > packSize) {
            index += 1;
            counter = 1;
        }
        // 每轮分配结束后, 重新从第一个电梯开始分配
        if (index >= elevatorNum) {
            index = 0;
        }
        // 跳过维护的电梯
        while (elevatorMaintained.get(index) != null) {
            index++;
            counter = 1;
            if (index >= elevatorNum) {
                index = 0;
            }
        }
        return index;
    }
}