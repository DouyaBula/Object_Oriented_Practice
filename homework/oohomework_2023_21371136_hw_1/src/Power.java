public class Power extends Factor implements Element, Cloneable {
    private String base;
    private int exponent;   //TODO: hw1:指数最大不超过8

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public Power(String base, int exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public String getBase() {
        return base;
    }

    public int getExponent() {
        return exponent;
    }

    @Override
    public String getType() {
        if (exponent == 0) {
            return "Dismiss";
        } else {
            return "Power";
        }
    }

    @Override
    public String toString() {
        if (exponent > 1) {
            return base + "**" + exponent;
        } else {
            return base;
        }
    }

    @Override
    public Power clone() throws CloneNotSupportedException {
        return (Power) super.clone();
    }
}
