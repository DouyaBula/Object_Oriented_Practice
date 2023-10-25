public class Factor implements Element {
    public Factor() {
    }

    public boolean isSame(Factor factor) {
        System.out.println(factor.getType());
        System.out.println("\033[31;4m" + "漏写isSame!" + "\033[0m");
        return false;
    }

    @Override
    public String getType() {
        System.out.println("\033[31;4m" + "漏写getType!" + "\033[0m");
        return null;
    }

}
