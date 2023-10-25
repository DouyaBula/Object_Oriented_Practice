import java.io.Serializable;
import java.math.BigInteger;

public class Derivator implements Serializable {
    public Expression derivativeExp(Expression exp, String var) throws CloneNotSupportedException {
        Expression expDerivative = new Expression();
        expDerivative.setExponent(exp.getExponent());
        if (expDerivative.getExponent() != 1) {
            System.out.println("huh? 😰");
        }
        Term zero = new Term();
        zero.addFactor(new Constant("0"));
        expDerivative.addTerm('+', zero);
        for (int i = 1; i <= exp.getTermCnt(); i++) {
            Term term = exp.getTerms().get(i);
            derivativeTerm(expDerivative, term, var);
        }
        return expDerivative;
    }

    private void derivativeTerm(Expression expDerivative,
                                Term term, String var) throws CloneNotSupportedException {
        for (Factor factor :
                term.getFactors().values()) {
            Term termDerivative = new Term();
            termDerivative.setCoefficient(term.getCoefficient());
            derivativeFactor(termDerivative, factor, var);
            for (Factor factorOthers :
                    term.getFactors().values()) {
                if (factorOthers.equals(factor)) {
                    continue;
                }
                termDerivative.addFactor('+', factorOthers);
            }
            expDerivative.addTerm('+', termDerivative);
        }
    }

    private void mulTermCo(Term term, BigInteger co) {
        term.setCoefficient(term.getCoefficient().multiply(co));
    }

    private void derivativeFactor(Term term, Factor factor, String var)
            throws CloneNotSupportedException {
        switch (factor.getType()) {
            case "Power":
                Power power = ((Power) factor).clone();
                if (!power.getBase().equals(var)) {
                    term.addFactor(new Constant("0"));
                } else {
                    mulTermCo(term, BigInteger.valueOf(power.getExponent()));
                    power.setExponent(power.getExponent() - 1);
                    if (power.getExponent() == 0) {
                        term.addFactor(new Constant("1"));
                    } else {
                        term.addFactor(power);
                    }
                }
                break;
            case "Trigonometric":
                Trigonometric tri = ((Trigonometric) factor).clone();
                mulTermCo(term, BigInteger.valueOf(tri.getExponent()));
                tri.setExponent(tri.getExponent() - 1);
                if (tri.getExponent() == 0) {
                    term.addFactor(new Constant("1"));
                } else {
                    term.addFactor(tri);
                }
                Expression innerExp = tri.getFactor();
                Expression subExp = derivativeExp(innerExp, var);
                term.addFactor(subExp);
                if (tri.getTypeFunc().equals("sin")) {
                    Trigonometric subTri = new Trigonometric("cos", innerExp, 1);
                    term.addFactor(subTri);
                } else if (tri.getTypeFunc().equals("cos")) {
                    Trigonometric subTri = new Trigonometric("sin", innerExp, 1);
                    term.addFactor('-', subTri);
                } else {
                    System.out.println("huh?! 😰");
                }
                break;
            default:
                System.out.println("huh?? 😰");
                break;
        }
    }
}
