package cell;

import java.util.HashMap;

public class Item {
    private final HashMap<String, Book> books;
    private final HashMap<String, User> users;

    public Item() {
        this.books = new HashMap<>();
        this.users = new HashMap<>();
    }

    public void addBook(String bookStr, Book book) {
        books.put(bookStr, book);
    }

    public void addUser(String userId) {
        if (!users.containsKey(userId)) {
            users.put(userId, new User(userId));
        }
    }

    public Book getBook(String bookStr) {
        return books.get(bookStr);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }
}
