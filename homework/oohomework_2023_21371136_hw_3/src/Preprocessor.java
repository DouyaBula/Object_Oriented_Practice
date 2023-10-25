import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
    private final String patternFunc = "(?<name>[fgh])\\((?<variables>.*)\\)=(?<definition>.*)";
    private final Pattern pattern = Pattern.compile(patternFunc);
    private final Scanner scanner;
    private String exp;
    private CustomFuncField customFuncField = new CustomFuncField();

    public Preprocessor(Scanner scanner) {
        this.scanner = scanner;
    }

    public void analyze() throws CloneNotSupportedException {
        customFuncField = new CustomFuncField();
        int n;
        n = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < n; i++) {
            String func = scanner.nextLine();
            func = removeSpaces(func);
            Matcher matcher = pattern.matcher(func);
            if (matcher.find()) {
                String name = matcher.group("name");
                String[] variables = matcher.group("variables").split(",");
                String definition = matcher.group("definition");
                Lexer lexer = new Lexer(definition);
                Parser parser = new Parser(lexer, this.customFuncField);
                lexer.next();
                Expression expression = parser.expressionParse();
                CustomFunc customFunc = new CustomFunc(variables, expression);
                customFuncField.addFunc(name, customFunc);
            } else {
                System.out.println("\033[31;4m" + "matcher寄了!" + "\033[0m");
            }
        }
        exp = scanner.nextLine();
        exp = removeSpaces(exp);
    }

    public String removeSpaces(String string) {
        String modified;
        modified = string.replace(" ", "");
        modified = modified.replace("\t", "");
        return modified;
    }

    public String getExp() {
        return exp;
    }

    public CustomFuncField getCustomFuncField() {
        return customFuncField;
    }
}
