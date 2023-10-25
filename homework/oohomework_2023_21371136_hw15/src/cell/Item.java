package cell;

import java.util.HashMap;
import java.util.HashSet;

public class Item {
    private final HashSet<Book> bookList;
    private final HashSet<Book> transportable;
    private final HashMap<String, User> users;

    public Item() {
        this.bookList = new HashSet<>();
        this.transportable = new HashSet<>();
        this.users = new HashMap<>();
    }

    public void purchaseBook(Book book) {
        addBook(book, true);
    }

    public boolean isTransportable(Book book) {
        return transportable.contains(book);
    }

    public void addBook(Book book, boolean transportable) {
        bookList.add(book);
        if (transportable) {
            this.transportable.add(book);
        }
    }

    public void addUser(String userId) {
        if (!users.containsKey(userId)) {
            users.put(userId, new User(userId));
        }
    }

    public boolean hasBook(Book book) {
        return bookList.contains(book);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }
}
