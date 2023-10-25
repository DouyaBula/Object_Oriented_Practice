import java.math.BigInteger;
import java.util.ArrayList;

public class Expression extends Factor implements Element, Cloneable {
    public int getExponent() {
        return exponent;
    }

    private int exponent = 1; //TODO: hw1:指数最大不超过8
    private ArrayList<Term> terms;

    private int termCnt = 0;  //TODO: hw1:有效长度至多为 200 个字符

    public Expression() {
        this.terms = new ArrayList<>();
        terms.add(null);    // 第一项设为空
    }

    public void exponentUp() {
        this.exponent++;
    }

    public void setExponent(int num) {
        this.exponent = num;
    }

    public Expression mulExp(Expression exp2) throws CloneNotSupportedException {
        Expression exp1 = this;
        ArrayList<Term> terms1 = exp1.getTerms();
        ArrayList<Term> terms2 = exp2.getTerms();
        Term term1;
        Term term2;
        Term term3;
        Expression merged = new Expression();
        for (int i = 1; i < terms1.size(); i++) {
            term1 = terms1.get(i);
            for (int j = 1; j < terms2.size(); j++) {
                term2 = terms2.get(j);

                term3 = term1.mulTerm(term2);
                merged.addNormalTerm(term3);
            }
        }
        return merged;
    }

    public void addTerm(char sign, Term term) throws CloneNotSupportedException {
        if (sign == '-') {
            term.coefficientNeg();
        }
        if (term.isExpTerm()) {
            addExpTerm(term);
        } else {
            addNormalTerm(term);
        }
    }

    public void addExpTerm(Term term) throws CloneNotSupportedException {
        /* TODO: 将一个ExpTerm转换为多个普通的Term并add */
        int expFactorCnt = term.getExpFactorCnt();
        Expression expTemp1 = new Expression();
        Expression expTemp2 = (Expression) term.getFactor("Expression" + expFactorCnt);
        for (int i = expFactorCnt - 1; i >= 1; i--) {
            expTemp1 = expTemp2;
            expTemp2 = ((Expression) (term.getFactor("Expression" + i)))
                    .mulExp(expTemp1);
        }
        ArrayList<Term> terms2 = expTemp2.getTerms();
        for (int i = 1; i < terms2.size(); i++) {
            Term term2 = terms2.get(i);
            for (Factor factor :
                    term.getFactors().values()) {
                if (factor.getType().contains("Expression")) {
                    continue;
                } else {
                    term2.addFactor('+', factor);
                }
            }
            term2.coefficientMul(term.getCoefficient());
            addNormalTerm(term2);
        }
    }

    public void addNormalTerm(Term term) {
        if (termCnt == 0) {
            termCnt++;
            terms.add(termCnt, term);
        } else {
            boolean flag = false;
            Term termExist;
            for (int i = 1; i <= termCnt; i++) {
                termExist = terms.get(i);
                if (termExist.getDetailedType().equals(term.getDetailedType())) {
                    flag = true;
                    termExist.coefficientAdd(term.getCoefficient());
                    break;
                }
            }
            if (!flag) {
                termCnt++;
                terms.add(termCnt, term);
            }
        }
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public int getNum() {
        return terms.size();
    }

    public String getType() {
        return "Expression";
    }

    @Override
    public String toString() {
        int validCnt = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < terms.size(); i++) {
            if (terms.get(i).getCoefficient().equals(BigInteger.ZERO)) {
                continue;
            } else {
                if (validCnt > 0 && terms.get(i).getCoefficient().signum() == 1) {
                    sb.append("+");
                }
                sb.append(terms.get(i).toString());
                validCnt++;
            }
        }
        if (validCnt == 0) {
            sb.append("0");
        }
        return sb.toString();
    }

    @Override
    public Expression clone() throws CloneNotSupportedException {
        return (Expression) super.clone();
    }
}
