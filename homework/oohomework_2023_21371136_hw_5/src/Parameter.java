public class Parameter {
    private final int initialPos;
    private final long moveTime;
    private final long switchTime;
    private final int capacity;

    public Parameter(int initialPos, long moveTime, long switchTime, int capacity) {
        this.initialPos = initialPos;
        this.moveTime = moveTime;
        this.switchTime = switchTime;
        this.capacity = capacity;
    }

    public int getInitialPos() {
        return initialPos;
    }

    public long getMoveTime() {
        return moveTime;
    }

    public long getSwitchTime() {
        return switchTime;
    }

    public int getCapacity() {
        return capacity;
    }
}
