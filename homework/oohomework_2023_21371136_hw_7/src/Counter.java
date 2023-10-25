public class Counter {
    private int cnt = 0;

    public synchronized void increase() {
        cnt++;
    }

    public synchronized void reduce() {
        cnt--;
        this.notifyAll();
    }

    public boolean isZero() {
        return cnt == 0;
    }

    @Override
    public String toString() {
        return "" + cnt;
    }
}
