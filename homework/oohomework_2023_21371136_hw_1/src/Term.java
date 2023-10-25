import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Term implements Element {
    private HashMap<String, Factor> factors;
    private BigInteger one = new BigInteger("1");
    private BigInteger oneNeg = new BigInteger("-1");
    private BigInteger coefficient = one;
    private boolean expTerm = false;
    private int expFactorCnt = 0;  //TODO: 后续作业可能溢出

    public int getExpFactorCnt() {
        return expFactorCnt;
    }

    public boolean isExpTerm() {
        return expTerm;
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    private ArrayList<Factor> factorsSorted;

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

    public Term() {
        this.factors = new HashMap<>();
    }

    public void coefficientAdd(BigInteger delta) {
        this.coefficient = this.coefficient.add(delta);
    }

    public void coefficientMul(BigInteger mul) {
        this.coefficient = this.coefficient.multiply(mul);
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

    public void addFactor(char sign, Factor factor) throws CloneNotSupportedException {
        if (sign == '+') {
            coefficient = coefficient;
        } else if (sign == '-') {
            coefficient = coefficient.negate();
        } else {
            System.out.println("\033[31;4m" + "Factor符号非法!" + "\033[0m");
        }
        String type = factor.getType();
        switch (type) {
            case "Power":
                type = ((Power) factor).getBase();
                if (factors.get(type) == null) {
                    factors.put(type, factor);
                } else {
                    Power factorNew = ((Power) factors.get(type)).clone();
                    factorNew.setExponent(factorNew.getExponent()
                            + ((Power) factor).getExponent());
                    factors.replace(type, factorNew);
                }
                break;
            case "Constant":
                coefficient = coefficient.multiply(
                        ((Constant) factor).getConstant());
                break;
            case "Expression":
                int exponent = ((Expression) factor).getExponent();
                if (exponent == 0) {
                    break;
                } else {
                    expTerm = true;
                    while (exponent > 0) {
                        expFactorCnt++;
                        Expression factorNew = ((Expression) factor).clone();
                        factors.put("Expression" + expFactorCnt, factorNew);
                        exponent--;
                    }
                }
                break;
            default:
                System.out.println("\033[31;4m" + "Term添加Factor出错! 不可能的情况" + "\033[0m");
                break;
        }
    }

    public HashMap<String, Factor> getFactors() {
        return factors;
    }

    public Factor getFactor(String type) {
        return factors.get(type);
    }

    @Override
    public String getType() {
        return "Term";
    }

    public String getDetailedType() {
        StringBuilder sb = new StringBuilder();
        if (factors.size() == 0) {
            return "OnlyCoefficient";
        } else {
            for (Factor factor :
                    factors.values()) {
                sb.append(factor.toString());
                sb.append("*");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }

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

}
