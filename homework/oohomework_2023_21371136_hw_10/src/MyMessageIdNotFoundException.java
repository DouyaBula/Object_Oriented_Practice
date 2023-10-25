import com.oocourse.spec2.exceptions.MessageIdNotFoundException;

public class MyMessageIdNotFoundException extends MessageIdNotFoundException {
    private int id;
    private int minfCnt;
    private int minfCntId;

    public MyMessageIdNotFoundException(int id) {
        Counter.COUNTER.triggerMinf(id);
        this.id = id;
        this.minfCnt = Counter.COUNTER.getMinf();
        this.minfCntId = Counter.COUNTER.getMinfId(id);
    }

    @Override
    public void print() {
        System.out.printf("minf-%d, %d-%d\n", minfCnt, id, minfCntId);
    }
}
