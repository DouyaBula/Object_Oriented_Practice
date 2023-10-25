import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws CloneNotSupportedException {
        Scanner scanner = new Scanner(System.in);
        Preprocessor preprocessor = new Preprocessor(scanner);
        preprocessor.analyze();
        String exp = preprocessor.getExp();
        CustomFuncField customFuncField = preprocessor.getCustomFuncField();
        Lexer lexer = new Lexer(exp);
        Parser parser = new Parser(lexer, customFuncField);
        lexer.next();
        Expression expPolished = parser.expressionParse();
        Simplifier simplifier = new Simplifier(expPolished);
        simplifier.simplify();
        System.out.println(expPolished);
    }
}
