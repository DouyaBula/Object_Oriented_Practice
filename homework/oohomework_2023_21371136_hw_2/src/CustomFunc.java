import java.util.ArrayList;
import java.util.HashMap;

public class CustomFunc extends Factor implements Cloneable {

    private final HashMap<String, Integer> variables = new HashMap<>();
    private final Expression definition;
    private HashMap<Integer, Expression> factors = new HashMap<>();
    private int numFactors = 0;

    public CustomFunc(String[] variables, Expression definition) {
        for (int i = 0; i < variables.length; i++) {
            this.variables.put(variables[i], i + 1);
        }
        this.definition = definition;
    }

    public void clear() {
        this.numFactors = 0;
        this.factors = new HashMap<>();
    }

    public void addFactor(ArrayList<Factor> arguments) throws CloneNotSupportedException {
        Expression factorConverted;
        for (Factor argument : arguments) {
            numFactors++;
            factorConverted = new Expression();
            if (argument.getType().equals("Expression")) {
                factorConverted = (Expression) argument;
            } else {
                Term term = new Term();
                term.addFactor('+', argument);
                factorConverted.addTerm('+', term);
            }
            factors.put(numFactors, factorConverted);
        }
    }

    private Factor factorMap(Factor factor) throws CloneNotSupportedException {
        Factor factorMapped;
        if (factor.getType().equals("Power")) {
            String base = ((Power) factor).getBase();
            Integer order = variables.get(base);
            factorMapped = factors.get(order).clone();
            ((Expression) factorMapped).setExponent(
                    ((Expression) factorMapped).getExponent() *
                            ((Power) factor).getExponent()); // 寄, 逻辑出问题了
        } else if (factor.getType().equals("Constant")) {
            factorMapped = ((Constant) factor).clone();
        } else if (factor.getType().equals("Trigonometric")) {
            String typeFunc = ((Trigonometric) factor).getTypeFunc();
            Expression innerFactor = ((Trigonometric) factor).getFactor();
            int exponent = ((Trigonometric) factor).getExponent();
            Expression innerFactorMapped = expressionMap(innerFactor);
            if (innerFactorMapped.getType().equals("Expression")) {
                if (innerFactorMapped.toString().equals("0")) {
                    if (typeFunc.equals("sin")) {
                        factorMapped = new Constant("0");
                    } else if (typeFunc.equals("cos")) {
                        factorMapped = new Constant("1");
                    } else {
                        System.out.println("\033[31;4m" + "寄!" + "\033[0m");
                        return null;
                    }
                } else {
                    factorMapped = new Trigonometric(typeFunc,
                            innerFactorMapped, exponent);
                }
            } else {
                System.out.println("\033[31;4m" + "寄!" + "\033[0m");
                return null;
            }
        } else {
            System.out.println("\033[31;4m" + "寄!" + "\033[0m");
            return null;
        }
        return factorMapped;
    }

    private Term termMap(Term term) throws CloneNotSupportedException {
        Term termMapped = new Term();
        termMapped.setCoefficient(term.getCoefficient());
        for (Factor factor :
                term.getFactors().values()) {
            termMapped.addFactor('+', factorMap(factor));
        }
        return termMapped;
    }

    private Expression expressionMap(Expression expression) throws CloneNotSupportedException {
        Expression expressionMapped = new Expression();
        expressionMapped.setExponent(expression.getExponent());
        ArrayList<Term> terms = expression.getTerms();
        for (int i = 1; i < terms.size(); i++) {
            Term debugTemp = terms.get(i);
            Term debugTempNew = termMap(debugTemp);
            expressionMapped.addTerm('+', debugTempNew);
        }
        return expressionMapped;
    }

    public Expression call() throws CloneNotSupportedException {
        return expressionMap(definition);
    }

    @Override
    public String getType() {
        return "CustomFunc";
    }

    @Override
    public CustomFunc clone() throws CloneNotSupportedException {
        return (CustomFunc) super.clone();
    }
}
