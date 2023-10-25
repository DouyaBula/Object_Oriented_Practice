public class Rotator {
    private int elevatorNum;
    private int packSize;
    private int index;
    private int counter;

    public Rotator(int elevatorNum, int packSize) {
        this.elevatorNum = elevatorNum;
        this.packSize = packSize;
        this.index = 0;
        this.counter = 0;
    }

    public int next() {
        counter++;
        if (counter > packSize) {
            counter = 1;
            index += 2;
        }
        if (index >= elevatorNum) {
            index = 0;
        }
        if (counter <= packSize / 2) {
            return index;
        } else {
            return index + 1;
        }
    }
}
