package cell;

import main.Library;

import java.util.HashMap;

public class User {
    private final String id;
    private final HashMap<Book, BookStatus> bookStatus;
    private final HashMap<Book, Library> bookFrom;
    private boolean hasB;

    public User(String id) {
        this.id = id;
        this.bookStatus = new HashMap<>();
        this.hasB = false;
        this.bookFrom = new HashMap<>();
    }

    public boolean hasB() {
        return hasB;
    }

    public boolean hasBook(Book book) {
        return bookStatus.containsKey(book);
    }

    public Library where(Book book) {
        return bookFrom.get(book);
    }

    public void borrowBook(Library library, Book book) {
        bookStatus.put(book, BookStatus.NORMAL);
        bookFrom.put(book, library);
        if (book.is("B")) {
            hasB = true;
        }
    }

    public BookStatus statusOf(Book book) {
        return bookStatus.get(book);
    }

    public void returnBook(Book book) {
        bookStatus.remove(book);
        bookFrom.remove(book);
        if (book.is("B")) {
            hasB = false;
        }
    }

    public void smearBook(Book book) {
        bookStatus.replace(book, BookStatus.SMEARED);
    }

    public void loseBook(Book book) {
        bookStatus.remove(book);
        bookFrom.remove(book);
        if (book.is("B")) {
            hasB = false;
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
