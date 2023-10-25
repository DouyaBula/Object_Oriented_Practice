package modules;

import cell.Book;
import cell.User;
import main.Library;
import template.Terminal;

public class Machine extends Terminal {
    private final Bookshelf bookshelf;

    public Machine(Bookshelf bookshelf, Library owner) {
        super("self-service machine", owner);
        this.bookshelf = bookshelf;
    }

    public boolean searchBook(String date, User user, Book book) {
        System.out.printf("[%s] %s-%s queried %s from %s\n",
                date, owner(), user, book, name());
        System.out.printf("[%s] self-service machine provided information of %s\n",
                date, book);
        return bookshelf.hasBook(book);
    }

    public void borrow(String date, User user, Book book) {
        if (user.hasBook(book) && book.is("C")) {
            System.out.printf("[%s] %s refused lending %s-%s to %s-%s\n",
                    date, name(), owner(), book, owner(), user);
            System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                    date, book, "loop", "loop");
            bookshelf.removeBook(book);
            this.addBook(book);
        } else {
            System.out.printf("[%s] %s lent %s-%s to %s-%s\n",
                    date, name(), owner(), book, owner(), user);
            System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                    date, book, "loop", "loop");
            System.out.printf("[%s] %s-%s borrowed %s-%s from %s\n",
                    date, owner(), user, owner(), book, name());
            bookshelf.removeBook(book);
            user.borrowBook(owner(), book);
        }
    }

}
