public class Parser {
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
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
            lexer.next();
            lexer.next();
            ((Expression)factor).setExponent(lexer.getExponent());
        } else {
            System.out.println("\033[31;4m" + "解析Factor出错! 不可能的Expression情况" + "\033[0m");
        }

        return factor;
    }

}
