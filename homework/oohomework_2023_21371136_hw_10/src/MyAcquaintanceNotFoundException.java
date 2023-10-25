import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private int id;
    private int anfCnt;
    private int anfCntId;

    public MyAcquaintanceNotFoundException(int id) {
        Counter.COUNTER.triggerAnf(id);
        this.id = id;
        this.anfCnt = Counter.COUNTER.getAnf();
        this.anfCntId = Counter.COUNTER.getAnfId(id);
    }

    @Override
    public void print() {
        System.out.printf("anf-%d, %d-%d\n", anfCnt, id, anfCntId);
    }
}
