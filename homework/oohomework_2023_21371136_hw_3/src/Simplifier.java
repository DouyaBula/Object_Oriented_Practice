import java.util.Collections;

public class Simplifier {
    private Expression raw;

    public Simplifier(Expression raw) {
        this.raw = raw;
    }

    public void simplify() {
        // 首项为正
        if (raw.getTerms().get(1).getCoefficient().signum() == -1) {
            for (int i = 2; i < raw.getTerms().size(); i++) {
                if (raw.getTerms().get(i).getCoefficient().signum() == 1) {
                    Collections.swap(raw.getTerms(), 1, i);
                    break;
                }
            }
        }
    }

}
