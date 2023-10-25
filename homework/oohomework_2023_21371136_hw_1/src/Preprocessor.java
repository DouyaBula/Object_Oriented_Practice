public class Preprocessor {
    private String output;

    public Preprocessor(String input) {
        this.output = input.replace(" ", "");
        this.output = this.output.replace("\t", "");
    }

    public String getOutput() {
        return output;
    }
}
