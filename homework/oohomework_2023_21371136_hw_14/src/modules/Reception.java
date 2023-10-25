package modules;

import cell.Book;
import cell.User;
import main.Library;
import template.Terminal;

public class Reception extends Terminal {
    private final Bookshelf bookshelf;

    public Reception(Bookshelf bookshelf, Library owner) {
        super("borrowing and returning librarian", owner);
        this.bookshelf = bookshelf;
    }

    public boolean borrow(String date, User user, Book book) {
        if (user.hasB() && book.is("B")) {
            System.out.printf("[%s] %s refused lending %s-%s to %s-%s\n",
                    date, name(), owner(), book, owner(), user);
            System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                    date, book, "loop", "loop");
            bookshelf.removeBook(book);
            this.addBook(book);
            return false;
        } else {
            System.out.printf("[%s] %s lent %s-%s to %s-%s\n",
                    date, name(), owner(), book, owner(), user);
            System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                    date, book, "loop", "loop");
            System.out.printf("[%s] %s-%s borrowed %s-%s from %s\n",
                    date, owner(), user, owner(), book, name());
            bookshelf.removeBook(book);
            user.borrowBook(owner(), book);
            return true;
        }
    }

    public void punish(String date, User user) {
        System.out.printf("[%s] %s-%s got punished by %s\n",
                date, owner(), user, name());
        System.out.printf("[%s] borrowing and returning librarian received %s-%s's fine\n",
                date, owner(), user);
    }
}
