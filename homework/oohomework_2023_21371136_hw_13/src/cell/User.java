package cell;

import java.util.HashMap;

public class User {
    private final String id;
    private final HashMap<Book, BookStatus> books;
    private boolean bookb;

    public User(String id) {
        this.id = id;
        this.books = new HashMap<>();
        this.bookb = false;
    }

    public boolean hasB() {
        return bookb;
    }

    public boolean hasBook(Book book) {
        return books.containsKey(book);
    }

    public void borrowBook(Book book) {
        books.put(book, BookStatus.NORMAL);
        if (book.is("B")) {
            bookb = true;
        }
    }

    public BookStatus statusOf(Book book) {
        return books.get(book);
    }

    public void returnBook(Book book) {
        books.remove(book);
        if (book.is("B")) {
            bookb = false;
        }
    }

    public void smearBook(Book book) {
        books.replace(book, BookStatus.SMEARED);
    }

    public void loseBook(Book book) {
        books.remove(book);
        if (book.is("B")) {
            bookb = false;
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
