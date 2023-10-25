import java.util.Scanner;

public class Debug {
    private Scanner scanner;

    public Debug(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean debug(int type) throws CloneNotSupportedException {
        Preprocessor preprocessor = new Preprocessor(scanner);
        while (scanner.hasNextLine()) {
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
        return true;
    }
}
