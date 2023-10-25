import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws CloneNotSupportedException {
        Scanner scanner = new Scanner(System.in);
        String exp = scanner.nextLine();
        Preprocessor preprocessor = new Preprocessor(exp);
        exp = preprocessor.getOutput();
        Lexer lexer = new Lexer(exp);
        Parser parser = new Parser(lexer);
        lexer.next();
        System.out.println(parser.expressionParse().toString());
    }
}
