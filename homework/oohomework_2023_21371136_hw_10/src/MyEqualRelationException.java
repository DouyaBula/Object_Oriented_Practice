import com.oocourse.spec2.exceptions.EqualRelationException;

public class MyEqualRelationException extends EqualRelationException {
    private int id1;
    private int id2;
    private int erCnt;
    private int erCntId1;
    private int erCntId2;

    public MyEqualRelationException(int id1, int id2) {
        Counter.COUNTER.triggerEr(id1, id2);
        this.id1 = Math.min(id1, id2);
        this.id2 = Math.max(id1, id2);
        this.erCnt = Counter.COUNTER.getEr();
        this.erCntId1 = Counter.COUNTER.gerErId(this.id1);
        this.erCntId2 = Counter.COUNTER.gerErId(this.id2);
    }

    @Override
    public void print() {
        System.out.printf("er-%d, %d-%d, %d-%d\n",
                erCnt, id1, erCntId1, id2, erCntId2);
    }
}
