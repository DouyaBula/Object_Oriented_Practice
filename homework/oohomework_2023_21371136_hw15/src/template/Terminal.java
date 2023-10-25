package template;

import cell.Book;
import cell.BookStatus;
import cell.User;
import main.Library;
import modules.Reception;

public class Terminal extends Server {

    public Terminal(String name, Library owner) {
        super(name, owner);
    }

    public boolean remand(Reception reception, String date, User user, Book book) {
        boolean ret = true;
        if (user.statusOf(book) == BookStatus.SMEARED) {
            reception.punish(date, user);
            ret = false;
        } else if (user.where(book).equals(owner())) {
            this.addBook(book);
        }
        System.out.printf("[%s] %s-%s returned %s-%s to %s\n",
                date, owner(), user, user.where(book), book, name());
        System.out.printf("[%s] %s collected %s-%s from %s-%s\n",
                date, name(), user.where(book), book, owner(), user);
        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                date, book, "loop", "loop");
        return ret;
    }
}
