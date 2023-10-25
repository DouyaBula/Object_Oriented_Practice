package template;

import cell.Book;
import cell.BookStatus;
import cell.User;
import modules.Reception;

public class Terminal extends Server {

    public Terminal(String name) {
        super(name);
    }

    public boolean remand(Reception reception, String date, User user, Book book) {
        boolean ret = true;
        if (user.statusOf(book) == BookStatus.SMEARED) {
            reception.punish(date, user);
            ret = false;
        } else {
            this.addBook(book);
        }
        user.returnBook(book);
        System.out.printf("[%s] %s returned %s to %s\n",
                date, user, book, name());
        return ret;
    }
}
