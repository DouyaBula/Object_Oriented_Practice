package template;

import cell.Book;

import java.util.HashMap;

public class Server {
    private final HashMap<Book, Integer> books;
    private final String name;

    public Server(String name) {
        this.books = new HashMap<>();
        this.name = name;
    }

    public HashMap<Book, Integer> getBooks() {
        return books;
    }

    public boolean hasBook(Book book) {
        return books.getOrDefault(book, 0) > 0;
    }

    public void addBook(Book book, Integer cnt) {
        if (!books.containsKey(book)) {
            books.put(book, cnt);
        } else {
            books.replace(book, books.get(book) + cnt);
        }
    }

    public void addBook(Book book) {
        addBook(book, 1);
    }

    public void removeBook(Book book) {
        if (!books.containsKey(book) || books.get(book) <= 0) {
            System.out.println("INVALID OPERATION");
        } else {
            books.replace(book, books.get(book) - 1);
        }
    }

    public void reset() {
        books.replaceAll((key, value) -> 0);
    }

    public String name() {
        return name;
    }
}
