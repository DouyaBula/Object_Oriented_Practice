public class Lexer {    // 词法分析器
    private final String input;
    private int pos = -1;
    private char currentChar;
    private char nextChar;

    public Lexer(String input) {
        this.input = input;
    }

    public void next() {
        next(1);
    }

    public void next(int bias) {
        pos += bias;
        if (pos < input.length()) {
            currentChar = input.charAt(pos);
            nextChar = pos + 1 < input.length() ? input.charAt(pos + 1) : '#';// TODO↓
        } else {
            pos++;
            currentChar = '#';  // TODO↓
        }
    }

    public void back() {
        pos--;
        currentChar = pos < input.length() ? input.charAt(pos) : '#';   // TODO↓
        nextChar = pos + 1 < input.length() ? input.charAt(pos + 1) : '#';  //TODO-: 后续作业可能会涉及到井号
    }

    public Constant getConstant(char sign) {
        StringBuilder sb = new StringBuilder();
        sb.append(sign);
        while (Character.isDigit(currentChar)) {
            sb.append(currentChar);
            next();
        }
        back();
        return new Constant(sb.toString());
    }

    public Power getPower() {
        String base = String.valueOf(currentChar);
        next();
        int exponent = getExponent();
        return new Power(base, exponent);
    }

    public int getExponent() {
        int exponent = 1;
        if (currentChar == '*' && nextChar == '*') {
            next(2);
            if (currentChar == '+') {
                next();
            } else if (currentChar == '-') {
                System.out.println("\033[31;4m" + "错误, 指数不能为负!" + "\033[0m");
            }
            exponent = getConstant('+').getValue().intValue();
        } else {
            back();
        }
        return exponent;
    }

    public char peek() {
        return currentChar;
    }
}
