import com.oocourse.spec2.exceptions.EqualGroupIdException;

public class MyEqualGroupIdException extends EqualGroupIdException {
    private int id;
    private int egiCnt;
    private int egiCntId;

    public MyEqualGroupIdException(int id) {
        Counter.COUNTER.triggerEgi(id);
        this.id = id;
        this.egiCnt = Counter.COUNTER.getEgi();
        this.egiCntId = Counter.COUNTER.getEgiId(id);
    }

    @Override
    public void print() {
        System.out.printf("egi-%d, %d-%d\n", egiCnt, id, egiCntId);
    }
}
