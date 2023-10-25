package main;

import cell.Book;
import cell.Item;
import cell.Request;
import cell.User;
import modules.Arrangement;
import modules.Bookshelf;
import modules.Logistics;
import modules.Machine;
import modules.Management;
import modules.Reception;
import modules.Reservation;

public class Library {
    private final Network network;
    private final String name;
    private final Bookshelf bookshelf;
    private final Machine machine;
    private final Reception reception;
    private final Reservation reservation;
    private final Arrangement arrangement;
    private final Management management;
    private final Logistics logistics;
    private final Item item;

    public Library(Network network, String name) {
        this.network = network;
        this.name = name;
        this.item = new Item();
        this.bookshelf = new Bookshelf(this);
        this.machine = new Machine(bookshelf, this);
        this.reception = new Reception(bookshelf, this);
        this.reservation = new Reservation(this);
        this.logistics = new Logistics(this);
        this.management = new Management(this, item);
        this.arrangement = new Arrangement(
                bookshelf, machine, reception, logistics, reservation, management);
    }

    public void initialize(Book book, int cnt, boolean transportable) {
        item.addBook(book, transportable);
        bookshelf.addBook(book, cnt);
    }

    public void perform(String date, String userId, String op, Book book) {
        item.addUser(userId);
        switch (op) {
            case "borrowed":
                borrow(date, userId, book);
                break;
            case "smeared":
                smear(userId, book);
                break;
            case "lost":
                lose(date, userId, book);
                break;
            case "returned":
                remand(date, userId, book);
                break;
            default:
                break;
        }
    }

    public void reserveBook(String date, Request req) {
        if (reservation.reserve(date, req.getUser(), req.getBook())) {
            if (!item.hasBook(req.getBook())) {
                management.addToCart(req.getBook());
            }
        }
    }

    public boolean exportBook(String date, Request req) {
        return management.exportBook(date, req.getUser(), req.getBook());
    }

    public void importBook(String date, Library from, Book book) {
        management.importBook(date, from, book);
    }

    public void remandBook(String date, Library from, Request req) {
        management.remandBook(date, from, req.getUser(), req.getBook());
    }

    public void purchaseBook(String date) {
        management.purchaseBook(date);
    }

    public void arrangeBook(String date) {
        arrangement.arrange(date);
    }

    public void removeBook(Book book) {
        bookshelf.removeBook(book);
    }

    public void resetCnt() {
        reservation.reset();
    }

    public void resetRecord() {
        management.resetRecord();
    }

    public void addBook(Book book) {
        bookshelf.addBook(book);
    }

    public boolean find(Book book) {
        return bookshelf.hasBook(book) && item.isTransportable(book);
    }

    private void borrow(String date, String userId, Book book) {
        User user = item.getUser(userId);
        if (!machine.searchBook(date, user, book)) {
            network.pend(this, user, book);
        } else {
            switch (book.getType()) {
                case "B":
                    if (reception.borrow(date, user, book)) {
                        reservation.clearB(user);
                    }
                    break;
                case "C":
                    machine.borrow(date, user, book);
                    break;
                default:
                    break;
            }
        }
    }

    private void smear(String userId, Book book) {
        User user = item.getUser(userId);
        user.smearBook(book);
    }

    private void lose(String date, String userId, Book book) {
        User user = item.getUser(userId);
        user.loseBook(book);
        reception.punish(date, user);
    }

    private void remand(String date, String userId, Book book) {
        User user = item.getUser(userId);
        if (!user.where(book).equals(this)) {
            network.returnBook(this, user.where(book), book);
        }
        switch (book.getType()) {
            case "B":
                if (!reception.remand(reception, date, user, book)) {
                    logistics.repair(user, date, book);
                }
                break;
            case "C":
                if (!machine.remand(reception, date, user, book)) {
                    logistics.repair(user, date, book);
                }
                break;
            default:
                break;
        }
        user.returnBook(book);
    }

    @Override
    public String toString() {
        return name;
    }
}
