import java.util.HashMap;

public class CustomFuncField {
    private final HashMap<String, CustomFunc> customFuncs;

    public CustomFuncField() {
        this.customFuncs = new HashMap<>();
    }

    public void addFunc(String name, CustomFunc customFunc) {
        customFuncs.put(name, customFunc);
    }

    public CustomFunc getFunc(String name) {
        return customFuncs.get(name);
    }
}
