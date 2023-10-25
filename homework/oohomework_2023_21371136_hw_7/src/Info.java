public class Info {
    private int cnt;
    private int dest;

    public Info(int cnt, int dest) {
        this.cnt = cnt;
        this.dest = dest;
    }

    public int getCnt() {
        return cnt;
    }

    public int getDest() {
        return dest;
    }

    public void reduceCnt() {
        this.cnt--;
    }

    public void clear() {
        this.cnt = 0;
    }
}
