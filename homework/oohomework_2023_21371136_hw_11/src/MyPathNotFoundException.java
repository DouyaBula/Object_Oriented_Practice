import com.oocourse.spec3.exceptions.PathNotFoundException;

public class MyPathNotFoundException extends PathNotFoundException {
    private int id;
    private int pnfCnt;
    private int pnfCntId;

    public MyPathNotFoundException(int id) {
        Counter.COUNTER.triggerPnf(id);
        this.id = id;
        this.pnfCnt = Counter.COUNTER.getPnf();
        this.pnfCntId = Counter.COUNTER.getPnfId(id);
    }

    @Override
    public void print() {
        System.out.printf("pnf-%d, %d-%d\n", pnfCnt, id, pnfCntId);
    }
}
