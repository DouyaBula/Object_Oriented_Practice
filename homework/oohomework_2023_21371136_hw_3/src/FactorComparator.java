import java.util.Comparator;

public class FactorComparator implements Comparator<Factor> {
    @Override
    public int compare(Factor factor1, Factor factor2) {
        String type1 = factor1.getType();
        String type2 = factor2.getType();
        if (!type1.equals(type2)) {
            if (type1.equals("Expression")) {
                return -1;
            } else if (type2.equals("Expression")) {
                return 1;
            } else if (type1.contains("Power")) {
                return -1;
            } else if (type2.contains("Power")) {
                return 1;
            } else {
                System.out.println("\033[31;4m" + "排序有误!不可能情况01!" + "\033[0m");
                return 0;
            }
        } else {
            if (type1.equals("Expression")) {
                return ((Expression) factor2).getNum() - ((Expression) factor1).getNum();
            } else if (type1.contains("Power")) {
                return ((Power) factor1).getBase().compareTo(
                        ((Power) factor2).getBase());
            } else if (type1.equals("Constant")) {
                System.out.println("\033[31;4m" + "排序有误!不可能情况02" + "\033[0m");
                return 0;
            } else {
                System.out.println("\033[31;4m" + "排序有误!不可能情况03" + "\033[0m");
                return 0;
            }
        }
    }
}
