import java.util.HashMap;

public class Counter {
    public static final Counter COUNTER = new Counter();
    private int pinf = 0;
    private HashMap<Integer, Integer> pinfId;
    private int epi = 0;
    private HashMap<Integer, Integer> epiId;
    private int rnf = 0;
    private HashMap<Integer, Integer> rnfId;
    private int er = 0;
    private HashMap<Integer, Integer> erId;

    private Counter() {
        this.pinfId = new HashMap<>();
        this.epiId = new HashMap<>();
        this.rnfId = new HashMap<>();
        this.erId = new HashMap<>();
    }

    public int getPinf() {
        return pinf;
    }

    public int getPinfId(int id) {
        return pinfId.get(id);
    }

    public int getEpi() {
        return epi;
    }

    public int getEpiId(int id) {
        return epiId.get(id);
    }

    public int getRnf() {
        return rnf;
    }

    public int getRnfId(int id) {
        return rnfId.get(id);
    }

    public int getEr() {
        return er;
    }

    public int gerErId(int id) {
        return erId.get(id);
    }

    public void triggerPinf(int id) {
        pinf++;
        if (pinfId.get(id) == null) {
            pinfId.put(id, 1);
        } else {
            pinfId.replace(id,
                    pinfId.get(id) + 1);
        }
    }

    public void triggerEpi(int id) {
        epi++;
        if (epiId.get(id) == null) {
            epiId.put(id, 1);
        } else {
            epiId.replace(id,
                    epiId.get(id) + 1);
        }
    }

    public void triggerRnf(int id1, int id2) {
        rnf++;
        if (rnfId.get(id1) == null) {
            rnfId.put(id1, 1);
        } else {
            rnfId.replace(id1,
                    rnfId.get(id1) + 1);
        }
        if (rnfId.get(id2) == null) {
            rnfId.put(id2, 1);
        } else {
            rnfId.replace(id2,
                    rnfId.get(id2) + 1);
        }
    }

    public void triggerEr(int id1, int id2) {
        er++;
        if (erId.get(id1) == null) {
            erId.put(id1, 1);
        } else {
            erId.replace(id1,
                    erId.get(id1) + 1);
        }
        if (id2 != id1) {
            if (erId.get(id2) == null) {
                erId.put(id2, 1);
            } else {
                erId.replace(id2,
                        erId.get(id2) + 1);
            }
        }
    }

}
