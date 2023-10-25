import com.oocourse.spec2.exceptions.RelationNotFoundException;

public class MyRelationNotFoundException extends RelationNotFoundException {

    private int id1;
    private int id2;
    private int rnfCnt;
    private int rnfCntId1;
    private int rnfCntId2;

    public MyRelationNotFoundException(int id1, int id2) {
        Counter.COUNTER.triggerRnf(id1, id2);
        this.id1 = Math.min(id1, id2);
        this.id2 = Math.max(id1, id2);
        this.rnfCnt = Counter.COUNTER.getRnf();
        this.rnfCntId1 = Counter.COUNTER.getRnfId(this.id1);
        this.rnfCntId2 = Counter.COUNTER.getRnfId(this.id2);
    }

    @Override
    public void print() {
        System.out.printf("rnf-%d, %d-%d, %d-%d\n",
                rnfCnt, id1, rnfCntId1, id2, rnfCntId2);
    }
}
