package modules;

import cell.Book;
import cell.User;
import template.Terminal;

public class Reception extends Terminal {
    private final Bookshelf bookshelf;

    public Reception(Bookshelf bookshelf) {
        super("borrowing and returning librarian");
        this.bookshelf = bookshelf;
    }

    public boolean borrow(String date, User user, Book book) {
        if (user.hasB() && book.is("B")) {
            bookshelf.removeBook(book);
            this.addBook(book);
            return false;
        } else {
            System.out.printf("[%s] %s borrowed %s from %s\n",
                    date, user, book, name());
            bookshelf.removeBook(book);
            user.borrowBook(book);
            return true;
        }
    }

    public void punish(String date, User user) {
        System.out.printf("[%s] %s got punished by %s\n",
                date, user, name());
    }
}
