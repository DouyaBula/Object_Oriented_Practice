package main;

import cell.Book;
import cell.Item;
import cell.User;
import modules.Arrangement;
import modules.Bookshelf;
import modules.Logistics;
import modules.Machine;
import modules.Reception;
import modules.Reservation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Library {
    public static final LocalDate BASE = LocalDate.parse("2022-12-31");
    public static final int CYCLE = 3;
    private final String regex = "\\[(?<date>\\d{4}-\\d{2}-\\d{2})\\] (?<user>.{8}) " +
            "(?<operation>.*) (?<book>.*)";
    private final Pattern pattern = Pattern.compile(regex);
    private final Scanner scanner;
    private final Bookshelf bookshelf;
    private final Machine machine;
    private final Reception reception;
    private final Reservation reservation;
    private final Arrangement arrangement;
    private final Logistics logistics;
    private final Item item;
    private long last;

    public Library(Scanner scanner) {
        this.scanner = scanner;
        this.item = new Item();
        this.bookshelf = new Bookshelf();
        this.machine = new Machine(bookshelf);
        this.reception = new Reception(bookshelf);
        this.reservation = new Reservation();
        this.logistics = new Logistics();
        this.arrangement = new Arrangement(
                bookshelf, machine, reception, logistics, reservation);
        this.last = 1;
    }

    private void purchaseBook() {
        int n;
        n = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < n; i++) {
            String str = scanner.nextLine();
            String[] split = str.split(" ");
            String[] bookStr = split[0].split("-");
            Book book = new Book(bookStr[0], bookStr[1]);
            item.addBook(split[0], book);
            bookshelf.addBook(book, Integer.parseInt(split[1]));
        }
    }

    public void open() {
        purchaseBook();
        loanBook();
    }

    private void loanBook() {
        int n;
        n = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < n; i++) {
            String str = scanner.nextLine();
            Matcher matcher = pattern.matcher(str);
            if (!matcher.find()) {
                System.out.println("MATCHER ERROR. exiting...");
                return;
            }
            String date = matcher.group("date");
            long now = ChronoUnit.DAYS.between(BASE, LocalDate.parse(date));
            if (now > last) {
                update(now);
            }
            String userId = matcher.group("user");
            item.addUser(userId);
            String op = matcher.group("operation");
            String bookStr = matcher.group("book");
            // System.out.println("OP: " + op);
            switch (op) {
                case "borrowed":
                    borrow(date, userId, bookStr);
                    break;
                case "smeared":
                    smear(userId, bookStr);
                    break;
                case "lost":
                    lose(date, userId, bookStr);
                    break;
                case "returned":
                    remand(date, userId, bookStr);
                    break;
                default:
                    break;
            }
        }
    }

    private void update(long now) {
        if (now > last) {
            reservation.reset();
        }
        long lastCycle = (last - 1) / CYCLE;
        long nowCycle = (now - 1) / CYCLE;
        if (nowCycle > lastCycle) {
            LocalDate next = BASE.plus(
                    3 * lastCycle + 4, ChronoUnit.DAYS);
            arrangement.arrange(next.toString());
        }
        last = now;
    }

    private void borrow(String date, String userId, String bookStr) {
        User user = item.getUser(userId);
        Book book = item.getBook(bookStr);
        if (!machine.searchBook(date, user, book)) {
            reservation.reserve(date, user, book);
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

    private void smear(String userId, String bookStr) {
        User user = item.getUser(userId);
        Book book = item.getBook(bookStr);
        user.smearBook(book);
    }

    private void lose(String date, String userId, String bookStr) {
        User user = item.getUser(userId);
        Book book = item.getBook(bookStr);
        user.loseBook(book);
        reception.punish(date, user);
    }

    private void remand(String date, String userId, String bookStr) {
        User user = item.getUser(userId);
        Book book = item.getBook(bookStr);
        switch (book.getType()) {
            case "B":
                if (!reception.remand(reception, date, user, book)) {
                    logistics.repair(date, book);
                }
                break;
            case "C":
                if (!machine.remand(reception, date, user, book)) {
                    logistics.repair(date, book);
                }
                break;
            default:
                break;
        }
    }

}
