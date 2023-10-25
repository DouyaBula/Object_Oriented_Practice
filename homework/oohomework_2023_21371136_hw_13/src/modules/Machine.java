package modules;

import cell.Book;
import cell.User;
import template.Terminal;

public class Machine extends Terminal {
    private final Bookshelf bookshelf;

    public Machine(Bookshelf bookshelf) {
        super("self-service machine");
        this.bookshelf = bookshelf;
    }

    public boolean searchBook(String date, User user, Book book) {
        System.out.printf("[%s] %s queried %s from %s\n",
                date, user, book, name());
        return bookshelf.hasBook(book);
    }

    public void borrow(String date, User user, Book book) {
        if (user.hasBook(book) && book.is("C")) {
            bookshelf.removeBook(book);
            this.addBook(book);
        } else {
            System.out.printf("[%s] %s borrowed %s from %s\n",
                    date, user, book, name());
            bookshelf.removeBook(book);
            user.borrowBook(book);
        }
    }

}
