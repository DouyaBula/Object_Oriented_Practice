import com.oocourse.spec1.exceptions.PersonIdNotFoundException;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private int id;
    private int pinfCnt;
    private int pinfIdCnt;

    public MyPersonIdNotFoundException(int id) {
        Counter.COUNTER.triggerPinf(id);
        this.id = id;
        this.pinfCnt = Counter.COUNTER.getPinf();
        this.pinfIdCnt = Counter.COUNTER.getPinfId(id);
    }

    @Override
    public void print() {
        System.out.printf("pinf-%d, %d-%d\n", pinfCnt, id, pinfIdCnt);
    }
}
