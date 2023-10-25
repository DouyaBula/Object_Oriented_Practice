import java.io.Serializable;
import java.math.BigInteger;

public class Constant extends Factor implements Cloneable, Serializable {
    private final BigInteger constant;

    public Constant(BigInteger constant) {
        this.constant = constant;
    }

    public Constant(String input) {
        this.constant = new BigInteger(input);
    }

    public String toString() {
        return constant.toString();
    }

    public BigInteger getValue() {
        return constant;
    }

    @Override
    public boolean isSame(Factor factor) {
        if (!this.getType().equals(factor.getType())) {
            return false;
        }
        Constant constant = (Constant) factor;
        return this.getValue().equals(constant.getValue());
    }

    @Override
    public String getType() {
        return "Constant";
    }

    @Override
    public Constant clone() throws CloneNotSupportedException {
        return (Constant) super.clone();
    }
}
