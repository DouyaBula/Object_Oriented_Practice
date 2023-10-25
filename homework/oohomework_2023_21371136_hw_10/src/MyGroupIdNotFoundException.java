import com.oocourse.spec2.exceptions.GroupIdNotFoundException;

public class MyGroupIdNotFoundException extends GroupIdNotFoundException {
    private int id;
    private int ginfCnt;
    private int ginfCntId;

    public MyGroupIdNotFoundException(int id) {
        Counter.COUNTER.triggerGinf(id);
        this.id = id;
        this.ginfCnt = Counter.COUNTER.getGinf();
        this.ginfCntId = Counter.COUNTER.getGinfId(id);
    }

    @Override
    public void print() {
        System.out.printf("ginf-%d, %d-%d\n", ginfCnt, id, ginfCntId);
    }
}