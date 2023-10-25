import com.oocourse.spec3.exceptions.EqualEmojiIdException;

public class MyEqualEmojiIdException extends EqualEmojiIdException {
    private int id;
    private int eeiCnt;
    private int eeiCntId;

    public MyEqualEmojiIdException(int id) {
        Counter.COUNTER.triggerEei(id);
        this.id = id;
        this.eeiCnt = Counter.COUNTER.getEei();
        this.eeiCntId = Counter.COUNTER.getEeiId(id);
    }

    @Override
    public void print() {
        System.out.printf("eei-%d, %d-%d\n", eeiCnt, id, eeiCntId);
    }
}
