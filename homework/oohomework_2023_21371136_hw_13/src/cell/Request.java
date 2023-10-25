package cell;

public class Request {
    private final User user;
    private final Book book;

    public Request(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }
}
