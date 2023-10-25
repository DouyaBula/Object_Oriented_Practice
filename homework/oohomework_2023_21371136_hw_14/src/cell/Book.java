package cell;

public class Book {
    private final String type;
    private final String id;

    public Book(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public boolean is(String type) {
        return this.type.equals(type);
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "-" + id;
    }
}
