import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public class Expression extends Factor implements Serializable {
    private final ArrayList<Term> terms;
    private int exponent = 1;
    private int termCnt = 0;

    public Expression() {
        this.terms = new ArrayList<>();
        terms.add(null);    // TODO: 第一项设为空
    }

    public int getExponent() {
        return exponent;
    }

    public void setExponent(int num) {
        this.exponent = num;
    }

    public Expression addExp(Expression exp2) throws CloneNotSupportedException {
        Expression merged = new Expression();
        ArrayList<Term> terms1 = this.getTerms();
        ArrayList<Term> terms2 = exp2.getTerms();
        for (int i = 1; i < terms1.size(); i++) {
            merged.addTerm('+', terms1.get(i));
        }
        for (int i = 1; i < terms2.size(); i++) {
            merged.addTerm('+', terms2.get(i));
        }
        return merged;
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
        int expFactorCnt = term.getExpFactorCnt();
        Expression expTemp1;
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
                if (!factor.getType().contains("Expression")) {
                    term2.addFactor('+', factor);
                }
            }
            term2.coefficientMul(term.getCoefficient());
            addNormalTerm(term2);
        }
    }

    public void addNormalTerm(Term term) throws CloneNotSupportedException {
        if (termCnt == 0) {
            termCnt++;
            terms.add(termCnt, term.clone());
        } else {
            boolean flag = false;
            Term termExist;
            for (int i = 1; i <= termCnt; i++) {
                termExist = terms.get(i);
                if (termExist.isSame(term)) {
                    flag = true;
                    termExist.coefficientAdd(term.getCoefficient());
                    break;
                } else if (termExist.canMergeNegTri(term).equals("sin")) {  // 奇偶性优化
                    flag = true;
                    termExist.setCoefficient(
                            termExist.getCoefficient().subtract(term.getCoefficient()));
                    break;
                } else if (termExist.canMergeNegTri(term).equals("cos")) {  // 奇偶性优化
                    flag = true;
                    termExist.setCoefficient(
                            termExist.getCoefficient().add(term.getCoefficient()));
                    break;
                } else if (termExist.isMergeableTriToConst(term)) {  // 三角恒等式
                    flag = true;
                    termExist.setCoefficient(
                            termExist.getCoefficient().subtract(term.getCoefficient()));
                    Constant temp1 = new Constant(term.getCoefficient());
                    Term temp2 = new Term();
                    temp2.addFactor('+', temp1);
                    this.addTerm('+', temp2);
                }
            }
            if (!flag) {
                termCnt++;
                terms.add(termCnt, term.clone());
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

    public int getTermCnt() {
        return termCnt;
    }

    public int getValidTermCnt() {
        int validCnt = 0;
        for (int i = 1; i < terms.size(); i++) {
            if (!terms.get(i).getCoefficient().equals(BigInteger.ZERO)) {
                validCnt++;
            }
        }
        return validCnt;
    }

    public Term getFirstValidTerm() {
        int index = 0;
        for (int i = 1; i < terms.size(); i++) {
            if (!terms.get(i).getCoefficient().equals(BigInteger.ZERO)) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            System.out.println("\033[31;4m" + "寄!" + "\033[0m");
            return null;
        } else {
            return terms.get(index);
        }
    }

    @Override
    public boolean isSame(Factor factor) {
        if (!this.getType().equals(factor.getType())) {
            return false;
        }
        Expression exp = (Expression) factor;
        if (this.getValidTermCnt() != exp.getValidTermCnt()) {
            return false;
        }
        for (int i = 1; i < this.getTerms().size(); i++) {
            Term term1 = this.terms.get(i);
            if (term1.getCoefficient().equals(BigInteger.ZERO)) {
                continue;
            }
            int flag = 0;
            for (int j = 1; j < exp.getTerms().size(); j++) {
                Term term2 = exp.getTerms().get(j);
                if (term2.getCoefficient().equals(BigInteger.ZERO)) {
                    continue;
                }
                if (term1.getCoefficient().equals(term2.getCoefficient())
                        && term1.isSame(term2)) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        int validCnt = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < terms.size(); i++) {
            if (!terms.get(i).getCoefficient().equals(BigInteger.ZERO)) {
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
    public Expression clone() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Expression) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
