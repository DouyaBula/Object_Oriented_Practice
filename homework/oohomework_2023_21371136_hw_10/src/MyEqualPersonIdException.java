import com.oocourse.spec2.exceptions.EqualPersonIdException;

public class MyEqualPersonIdException extends EqualPersonIdException {
    private int id;
    private int epiCnt;
    private int epiCntId;

    public MyEqualPersonIdException(int id) {
        Counter.COUNTER.triggerEpi(id);
        this.id = id;
        this.epiCnt = Counter.COUNTER.getEpi();
        this.epiCntId = Counter.COUNTER.getEpiId(id);
    }

    @Override
    public void print() {
        System.out.printf("epi-%d, %d-%d\n", epiCnt, id, epiCntId);
    }
}
