import com.oocourse.spec2.exceptions.EqualMessageIdException;

public class MyEqualMessageIdException extends EqualMessageIdException {
    private int id;
    private int emiCnt;
    private int emiCntId;

    public MyEqualMessageIdException(int id) {
        Counter.COUNTER.triggerEmi(id);
        this.id = id;
        this.emiCnt = Counter.COUNTER.getEmi();
        this.emiCntId = Counter.COUNTER.getEmiId(id);
    }

    @Override
    public void print() {
        System.out.printf("emi-%d, %d-%d\n", emiCnt, id, emiCntId);
    }
}
