import java.io.Serializable;

public class Power extends Factor implements Cloneable, Serializable {
    private final String base;
    private boolean expanded = false;
    private int exponent;   //TODO: hw1:指数最大不超过8

    public Power(String base, int exponent) {
        this.base = base;
        this.exponent = exponent;
        this.expanded = this.exponent == 2;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String getBase() {
        return base;
    }

    public int getExponent() {
        return exponent;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
        this.expanded = this.exponent == 2;
    }

    @Override
    public boolean isSame(Factor factor) {
        if (!this.getType().equals(factor.getType())) {
            return false;
        }
        Power power = (Power) factor;
        return this.getBase().equals(power.getBase())
                && this.getExponent() == power.getExponent();
    }

    @Override
    public String getType() {
        if (exponent == 0) {
            return "Dismiss";
        } else {
            return "Power";
        }
    }

    public String toStringNoExpand() {
        if (exponent > 1) {
            return base + "**" + exponent;
        } else {
            return base;
        }
    }

    @Override
    public String toString() {
        if (exponent > 1) {
            if (exponent == 2) {
                return base + "*" + base;
            } else {
                return base + "**" + exponent;
            }
        } else {
            return base;
        }
    }

    @Override
    public Power clone() throws CloneNotSupportedException {
        return (Power) super.clone();
    }
}
