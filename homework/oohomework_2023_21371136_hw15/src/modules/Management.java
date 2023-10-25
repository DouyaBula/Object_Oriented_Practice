package modules;

import cell.Book;
import cell.Item;
import cell.User;
import main.Library;
import template.Server;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Management extends Server {
    private final ArrayList<Book> purchaseOrder;
    private final HashMap<Book, Integer> purchaseCnt;
    private final HashMap<User, HashSet<Book>> record;
    private final Item item;

    public Management(Library owner, Item item) {
        super("purchasing department", owner);
        this.purchaseOrder = new ArrayList<>();
        this.purchaseCnt = new HashMap<>();
        this.record = new HashMap<>();
        this.item = item;
    }

    public void addToCart(Book book) {
        if (!purchaseOrder.contains(book)) {
            purchaseOrder.add(book);
            purchaseCnt.put(book, 1);
        } else {
            purchaseCnt.replace(book, purchaseCnt.get(book) + 1);
        }
    }

    public void purchaseBook(String date) {
        for (Book book : purchaseOrder) {
            System.out.printf("[%s] %s-%s got purchased by %s in %s\n",
                    date, owner(), book, name(), owner());
            addBook(book, Math.max(3, purchaseCnt.get(book)));
            item.purchaseBook(book);
        }
        purchaseOrder.clear();
        purchaseCnt.clear();
    }

    public void importBook(String date, Library from, Book book) {
        System.out.printf("[%s] %s-%s got received by %s in %s\n",
                date, from, book, name(), owner());
        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                date, book, "loop", "loop");
        addBook(book);
    }

    public void remandBook(String date, Library from, User user, Book book) {
        System.out.printf("[%s] %s lent %s-%s to %s-%s\n",
                date, name(), from, book, owner(), user);
        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                date, book, "loop", "loop");
        System.out.printf("(Sequence) [%s] %s sends a message to %s\n",
                date, "Management", "Library");
        System.out.printf("[%s] %s-%s borrowed %s-%s from %s\n",
                date, owner(), user, from, book, name());
        removeBook(book);
        user.borrowBook(LocalDate.parse(date), from, book);
    }

    public boolean exportBook(String date, User user, Book book) {
        if (user.hasB() && book.is("B")) {
            return false;
        }
        if (record.containsKey(user) && book.is("B")) {
            for (Book bookExported : record.get(user)) {
                if (bookExported.is("B")) {
                    return false;
                }
            }
        }
        if (user.hasBook(book) && book.is("C")) {
            return false;
        }
        if (record.containsKey(user) && record.get(user).contains(book)) {
            return false;
        }
        System.out.printf("[%s] %s-%s got transported by %s in %s\n",
                date, owner(), book, name(), owner());
        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                date, book, "loop", "loop");
        if (!record.containsKey(user)) {
            record.put(user, new HashSet<>());
        }
        record.get(user).add(book);
        return true;
    }

    public void resetRecord() {
        record.clear();
    }
}
