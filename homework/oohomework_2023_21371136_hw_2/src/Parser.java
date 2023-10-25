import java.util.ArrayList;

public class Parser {
    private final Lexer lexer;
    private final CustomFuncField customFuncField;

    public Parser(Lexer lexer, CustomFuncField customFuncField) {
        this.lexer = lexer;
        this.customFuncField = customFuncField;
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.customFuncField = new CustomFuncField();
    }

    public Expression expressionParse() throws CloneNotSupportedException {
        Expression expression = new Expression();
        if (lexer.peek() == '+' || lexer.peek() == '-') {
            char sign = lexer.peek();
            lexer.next();
            expression.addTerm(sign, termParse());
        } else {
            expression.addTerm('+', termParse());
        }
        lexer.next();
        while (lexer.peek() == '+' || lexer.peek() == '-') {
            char sign = lexer.peek();
            lexer.next();
            expression.addTerm(sign, termParse());
            lexer.next();
        }
        lexer.back();
        return expression;
    }

    public Term termParse() throws CloneNotSupportedException {
        Term term = new Term();
        if (lexer.peek() == '+' || lexer.peek() == '-') {
            char sign = lexer.peek();
            lexer.next();
            term.addFactor(sign, factorParse());
        } else {
            term.addFactor('+', factorParse());
        }
        lexer.next();
        while (lexer.peek() == '*') {
            lexer.next();
            term.addFactor('+', factorParse());
            lexer.next();
        }
        lexer.back();
        return term;
    }

    public Factor factorParse() throws CloneNotSupportedException {
        Factor factor = new Factor();
        if (lexer.peek() == 'x' || lexer.peek() == 'y' || lexer.peek() == 'z') {
            factor = lexer.getPower();
            if (factor.getType().equals("Dismiss")) {
                factor = new Constant("1");
            }
        } else if (lexer.peek() == '+' || lexer.peek() == '-') {
            char sign = lexer.peek();
            lexer.next();
            factor = lexer.getConstant(sign);
        } else if (Character.isDigit(lexer.peek())) {
            factor = lexer.getConstant('+');
        } else if (lexer.peek() == '(') {
            lexer.next();
            factor = expressionParse();
            lexer.next(2);
            ((Expression) factor).setExponent(lexer.getExponent());
        } else if (lexer.peek() == 's') {
            lexer.next(4);
            Factor factorTemp = factorParse();
            Term termTemp = new Term();
            Expression expTemp = new Expression();
            termTemp.addFactor('+', factorTemp);
            expTemp.addTerm('+', termTemp);
            lexer.next(2);
            int exponent = lexer.getExponent();
            if (exponent == 0) {
                factor = new Constant("1");
            } else if (expTemp.toString().equals("0")) {
                factor = new Constant("0");
            } else {
                factor = new Trigonometric("sin", expTemp, exponent);
            }
        } else if (lexer.peek() == 'c') {
            lexer.next(4);
            Factor factorTemp = factorParse();
            Term termTemp = new Term();
            Expression expTemp = new Expression();
            termTemp.addFactor('+', factorTemp);
            expTemp.addTerm('+', termTemp);
            lexer.next(2);
            int exponent = lexer.getExponent();
            if (exponent == 0 || expTemp.toString().equals("0")) {
                factor = new Constant("1");
            } else {
                factor = new Trigonometric("cos", expTemp, exponent);
            }
        } else if (lexer.peek() == 'f' | lexer.peek() == 'g' | lexer.peek() == 'h') {
            String funcName = String.valueOf(lexer.peek());
            ArrayList<Factor> arguments = getArguments();
            CustomFunc func = customFuncField.getFunc(funcName);
            func.clear();
            func.addFactor(arguments);
            factor = func.call();
        } else {
            System.out.println("\033[31;4m" + "解析Factor出错! 不可能的Expression情况" + "\033[0m");
        }
        return factor;
    }

    private ArrayList<Factor> getArguments() throws CloneNotSupportedException {
        ArrayList<Factor> arguments = new ArrayList<>();
        lexer.next(2);
        arguments.add(factorParse());
        lexer.next();
        if (lexer.peek() == ',') {
            lexer.next();
            arguments.add(factorParse());
            lexer.next();
            if (lexer.peek() == ',') {
                lexer.next();
                arguments.add(factorParse());
                lexer.next();
            }
        }
        return arguments;
    }
}
