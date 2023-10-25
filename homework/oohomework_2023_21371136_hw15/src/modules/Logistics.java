package modules;

import cell.Book;
import cell.User;
import main.Library;
import template.Server;

public class Logistics extends Server {
    public Logistics(Library owner) {
        super("logistics division", owner);
    }

    public void repair(User user, String date, Book book) {
        System.out.printf("[%s] %s-%s got repaired by %s in %s\n",
                date, user.where(book), book, name(), owner());
        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                date, book, "loop", "loop");
        if (user.where(book).equals(owner())) {
            this.addBook(book);
        }
    }
}
