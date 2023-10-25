package cell;

import main.Library;

public class Request {
    private final Library library;
    private final User user;
    private final Book book;
    private boolean needTransport;

    public Request(Library library, User user, Book book) {
        this.library = library;
        this.user = user;
        this.book = book;
        this.needTransport = true;
    }

    public Library getLibrary() {
        return library;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public boolean needTransport() {
        return needTransport;
    }

    public void setNeedTransport(boolean needTransport) {
        this.needTransport = needTransport;
    }
}
