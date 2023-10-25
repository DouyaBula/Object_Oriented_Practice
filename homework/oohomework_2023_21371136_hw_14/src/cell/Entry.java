package cell;

import main.Library;

public class Entry {
    private final Library from;
    private final Library to;
    private final Book book;

    public Entry(Library from, Library to, Book book) {
        this.from = from;
        this.to = to;
        this.book = book;
    }

    public Library from() {
        return from;
    }

    public Library to() {
        return to;
    }

    public Book book() {
        return book;
    }
}
