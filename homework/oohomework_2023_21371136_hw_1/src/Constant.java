import java.math.BigInteger;

public class Constant extends Factor implements Element {
    private BigInteger constant;

    public Constant(String input) {
        this.constant = new BigInteger(input);
    }

    public BigInteger getConstant() {
        return constant;
    }

    public String toString() {
        return constant.toString();
    }

    @Override
    public String getType() {
        return "Constant";
    }
}
