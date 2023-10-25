package modules;

import cell.Book;
import template.Server;

public class Logistics extends Server {
    public Logistics() {
        super("logistics division");
    }

    public void repair(String date, Book book) {
        System.out.printf("[%s] %s got repaired by %s\n", date, book, name());
        this.addBook(book);
    }
}
