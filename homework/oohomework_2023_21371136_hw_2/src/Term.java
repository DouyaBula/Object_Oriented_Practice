import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Term implements Element, Serializable {
    private final HashMap<String, Factor> factors;
    private final BigInteger one = new BigInteger("1");
    private final BigInteger oneNeg = new BigInteger("-1");
    private BigInteger coefficient = one;
    private boolean expTerm = false;
    private int expFactorCnt = 0;

    private int triFactorCnt = 0;
    private ArrayList<Factor> factorsSorted;

    private boolean marked = false;
    private boolean powerExpanded = false;

    public Term() {
        this.factors = new HashMap<>();
    }

    public boolean isMarked() {
        return marked;
    }

    public boolean isPowerExpanded() {
        return powerExpanded;
    }

    public boolean isOnlyConstant() {
        return this.factors.size() == 0;
    }

    public boolean isOnlyPower() {
        return this.coefficient.equals(BigInteger.ONE)
                && this.factors.size() == 1
                && (this.factors.get("x") != null
                || this.factors.get("y") != null
                || this.factors.get("z") != null);
    }

    public boolean isMergeableTriToConst(Term term) {
        if (this.isContainOnlyTri() && term.isContainOnlyTri()) {
            Trigonometric tri1 = (Trigonometric) this.getFactor("Trigonometric" + 1);
            Trigonometric tri2 = (Trigonometric) term.getFactor("Trigonometric" + 1);
            return tri1.isMergeableToConst(tri2);
        } else {
            return false;
        }
    }

    public String canMergeNegTri(Term term) throws CloneNotSupportedException {
        if (this.isContainOnlyTri() && term.isContainOnlyTri()) {
            Trigonometric tri1 = (Trigonometric) this.getFactor("Trigonometric" + 1);
            Trigonometric tri2 = (Trigonometric) term.getFactor("Trigonometric" + 1);
            return tri1.isNeg(tri2);
        }
        return "NO";
    }

    private boolean isContainOnlyTri() {
        return this.factors.size() == 1
                && this.triFactorCnt == 1;
    }

    public void mark() {
        this.marked = true;
    }

    public int getExpFactorCnt() {
        return expFactorCnt;
    }

    public boolean isExpTerm() {
        return expTerm;
    }

    public Term mulTerm(Term t2) throws CloneNotSupportedException {
        Term t1 = this;
        Term merged = new Term();
        merged.coefficientMul(t1.getCoefficient());
        merged.coefficientMul(t2.getCoefficient());
        HashMap<String, Factor> factors1 = t1.getFactors();
        HashMap<String, Factor> factors2 = t2.getFactors();
        for (Factor factor :
                factors1.values()) {
            merged.addFactor('+', factor);
        }
        for (Factor factor :
                factors2.values()) {
            merged.addFactor('+', factor);
        }
        return merged;
    }

    public void coefficientMul(BigInteger mul) {
        this.coefficient = this.coefficient.multiply(mul);
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public HashMap<String, Factor> getFactors() {
        return factors;
    }

    public Factor getFactor(String type) {
        return factors.get(type);
    }

    public void addFactor(char sign, Factor factor) throws CloneNotSupportedException {
        if (sign == '-') {
            coefficient = coefficient.negate();
        }
        String type = factor.getType();
        switch (type) {
            case "Power":
                addPower(factor);
                break;
            case "Constant":
                coefficient = coefficient.multiply(
                        ((Constant) factor).getValue());
                break;
            case "Expression":
                addExpression((Expression) factor);
                break;
            case "Trigonometric":
                addTrigonometric((Trigonometric) factor);
                break;
            default:
                System.out.println("\033[31;4m" + "Term添加Factor出错! 不可能的情况" + "\033[0m");
                break;
        }
    }

    private void addTrigonometric(Trigonometric factor) throws CloneNotSupportedException {
        if (triFactorCnt == 0) {
            triFactorCnt++;
            Trigonometric factorNew = factor.clone();
            factors.put("Trigonometric" + triFactorCnt, factorNew);
        } else {
            int flag = 1;
            for (int i = 1; i <= triFactorCnt; i++) {
                Trigonometric temp = (Trigonometric) factors.get("Trigonometric" + i);
                if (temp.isSame(factor)
                        || temp.isNeg(factor).equals("cos")) {
                    flag = 0;
                    temp.setExponent(temp.getExponent() +
                            factor.getExponent());
                    break;
                } else if (temp.isNeg(factor).equals("sin")) {
                    flag = 0;
                    temp.setExponent(temp.getExponent() +
                            factor.getExponent());
                    this.coefficientNeg();
                }
            }
            if (flag == 1) {
                triFactorCnt++;
                Trigonometric factorNew = factor.clone();
                factors.put("Trigonometric" + triFactorCnt, factorNew);
            }
        }
    }

    private void addExpression(Expression factor) {
        int exponent = factor.getExponent();
        if (exponent != 0) {
            expTerm = true;
            while (exponent > 0) {
                expFactorCnt++;
                Expression factorNew = factor.clone();
                factors.put("Expression" + expFactorCnt, factorNew);
                exponent--;
            }
        }
    }

    private void addPower(Factor factor) throws CloneNotSupportedException {
        String type;
        type = ((Power) factor).getBase();
        if (factors.get(type) == null) {
            factors.put(type, factor);
        } else {
            Power factorNew = ((Power) factors.get(type)).clone();
            factorNew.setExponent(factorNew.getExponent()
                    + ((Power) factor).getExponent());
            factors.replace(type, factorNew);
        }
        this.powerExpanded = ((Power) factors.get(type)).isExpanded();
    }

    public void coefficientAdd(BigInteger delta) {
        this.coefficient = this.coefficient.add(delta);
    }

    public void coefficientNeg() {
        this.coefficient = this.coefficient.negate();
    }

    public ArrayList<Factor> getFactorsSorted() {
        return this.factorsSorted;
    }

    public void sort() {
        factorsSorted = new ArrayList<>(factors.values());
        FactorComparator factorComparator = new FactorComparator();
        factorsSorted.sort(factorComparator);
    }

    @Override
    public String getType() {
        return "Term";
    }

    public boolean isSame(Term term) {
        if (this.getFactors().size() != term.getFactors().size()) {
            return false;
        }
        for (Factor factor1 :
                this.getFactors().values()) {
            int flag = 0;
            for (Factor factor2 :
                    term.getFactors().values()) {
                if (factor1.isSame(factor2)) {
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (factors.size() == 0) {
            sb.append(coefficient);
            sb.append("*");
        } else {
            if (!coefficient.equals(one) && !coefficient.equals(oneNeg)) {
                sb.append(coefficient);
                sb.append("*");
            } else if (coefficient.equals(oneNeg)) {
                sb.append("-");
            } // else if coefficient.equals(one), do NOTHING.
            for (Factor factor :
                    factors.values()) {
                sb.append(factor.toString());
                sb.append("*");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String toStringNoExpand() {
        StringBuilder sb = new StringBuilder();
        if (!coefficient.equals(one) && !coefficient.equals(oneNeg)) {
            sb.append(coefficient);
            sb.append("*");
        } else if (coefficient.equals(oneNeg)) {
            sb.append("-");
        } // else if coefficient.equals(one), do NOTHING.
        for (Factor factor :
                factors.values()) {
            sb.append(((Power) factor).toStringNoExpand());
        }
        return sb.toString();
    }

    public Term clone() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Term) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
