import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Trigonometric extends Factor implements Serializable {
    private final String typeFunc;
    private Expression factor;
    private int exponent;

    public Trigonometric(String typeFunc,
                         Expression factor, int exponent) throws CloneNotSupportedException {
        this.typeFunc = typeFunc;
        this.exponent = exponent;
        if (!factor.getType().equals("Expression")) {
            System.out.println("\033[31;4m" + "三角函数添加-不可能的情况!" + "\033[0m");
        }
        this.factor = factor.clone();
    }

    public String getTypeFunc() {
        return typeFunc;
    }

    public Expression getFactor() {
        return this.factor;
    }

    public int getExponent() {
        return exponent;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public String isNeg(Factor factor) throws CloneNotSupportedException {
        if (!this.getType().equals(factor.getType())) {
            System.out.println("\033[31;4m" + "寄!" + "\033[0m");
            return "NO";
        }
        Trigonometric temp = (Trigonometric) factor;
        if (!this.getTypeFunc().equals(temp.getTypeFunc())) {
            return "NO";
        }
        if (!this.getFactor().addExp(temp.getFactor())
                .toString().equals("0")) {
            return "NO";
        }
        if (this.getExponent() == temp.getExponent()) {
            if (this.getExponent() % 2 == 0) {
                return "cos";
            } else {
                return this.getTypeFunc();
            }
        } else {
            return "NO";
        }
    }

    public boolean isMergeableToConst(Trigonometric trigonometric) {
        return this.getExponent() == 2
                && trigonometric.getExponent() == 2
                && !this.getTypeFunc().equals(trigonometric.getTypeFunc())
                && this.getFactor().isSame(trigonometric.getFactor());
    }

    public boolean isSame(Trigonometric tri) {
        if (!this.getTypeFunc().equals(tri.getTypeFunc())) {
            return false;
        }
        Factor factor1 = this.getFactor();
        Factor factor2 = tri.getFactor();
        return factor1.isSame(factor2);
    }

    @Override
    public boolean isSame(Factor factor) {
        if (!this.getType().equals(factor.getType())) {
            return false;
        }
        Trigonometric trigonometric = (Trigonometric) factor;
        if (!this.getTypeFunc().equals(trigonometric.getTypeFunc())) {
            return false;
        }
        if (this.getExponent() != ((Trigonometric) factor).getExponent()) {
            return false;
        }
        return this.getFactor().isSame(trigonometric.getFactor());
    }

    @Override
    public String getType() {
        return "Trigonometric";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeFunc).append("(");
        if (factor.getType().equals("Expression")) {
            if (factor.getValidTermCnt() == 1
                    && factor.getFirstValidTerm().isOnlyConstant()) {
                sb.append(factor.toString());
            } else if (factor.getValidTermCnt() == 1
                    && factor.getFirstValidTerm().isOnlyPower()) {
                Term temp = factor.getFirstValidTerm();
                if (temp.isPowerExpanded()) {
                    sb.append(temp.toStringNoExpand());
                } else {
                    sb.append(temp.toString());
                }
            } else {
                sb.append("(").append(factor.toString()).append(")");
            }
        } else {
            System.out.println("寄!");
            sb.append(factor.toString());
        }
        sb.append(")");
        if (exponent > 1) {
            sb.append("**").append(exponent);
        }
        return sb.toString();
    }

    public Trigonometric clone() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Trigonometric) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
