import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;

public class MyEmojiIdNotFoundException extends EmojiIdNotFoundException {
    private int id;
    private int einfCnt;
    private int einfCntId;

    public MyEmojiIdNotFoundException(int id) {
        Counter.COUNTER.triggerEinf(id);
        this.id = id;
        this.einfCnt = Counter.COUNTER.getEinf();
        this.einfCntId = Counter.COUNTER.getEinfId(id);
    }

    @Override
    public void print() {
        System.out.printf("einf-%d, %d-%d\n", einfCnt, id, einfCntId);
    }
}
