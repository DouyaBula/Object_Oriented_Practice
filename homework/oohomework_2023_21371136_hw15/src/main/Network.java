package main;

import cell.Book;
import cell.Entry;
import cell.Request;
import cell.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Network {
    private final String regex = "\\[(?<date>\\d{4}-\\d{2}-\\d{2})] (?<library>.*)-(?<user>.*) "
            + "(?<operation>.*) (?<book>.*)";
    private final Pattern pattern = Pattern.compile(regex);
    private static final LocalDate BASE = LocalDate.parse("2022-12-31");
    private static final int CYCLE = 3;
    private long last;
    private final Scanner scanner;
    private final ArrayList<Library> libraries;
    private final HashMap<String, Library> name2Library;
    private final HashMap<String, Book> name2Book;
    private final ArrayList<Request> pendingQueue;
    private final ArrayList<Entry> returnQueue;

    public Network(Scanner scanner) {
        this.scanner = scanner;
        this.libraries = new ArrayList<>();
        this.name2Library = new HashMap<>();
        this.name2Book = new HashMap<>();
        this.last = 1;
        this.pendingQueue = new ArrayList<>();
        this.returnQueue = new ArrayList<>();
    }

    public void launch() {
        int n = Integer.parseInt(scanner.nextLine());
        String[] strings;
        for (int i = 0; i < n; i++) {
            strings = scanner.nextLine().split(" ");
            Library library = new Library(this, strings[0]);
            name2Library.put(strings[0], library);
            libraries.add(library);
            int m = Integer.parseInt(strings[1]);
            for (int j = 0; j < m; j++) {
                strings = scanner.nextLine().split(" ");
                String[] bookStrings = strings[0].split("-");
                Book book;
                if (!name2Book.containsKey(strings[0])) {
                    book = new Book(bookStrings[0], bookStrings[1]);
                    name2Book.put(strings[0], book);
                } else {
                    book = name2Book.get(strings[0]);
                }
                libraries.get(i).initialize(book, Integer.parseInt(strings[1]),
                        strings[2].equals("Y"));
            }
        }
        System.out.println("[2023-01-01] arranging librarian arranged all the books");
        n = Integer.parseInt(scanner.nextLine());
        String str;
        long now = 0;
        for (int i = 0; i < n; i++) {
            str = scanner.nextLine();
            Matcher matcher = pattern.matcher(str);
            if (!matcher.find()) {
                System.out.println("MATCHER ERROR. exiting...");
                return;
            }
            String date = matcher.group("date");
            now = ChronoUnit.DAYS.between(BASE, LocalDate.parse(date));
            if (now > last) {
                update(now, false);
            }
            String bookStr = matcher.group("book");
            String[] bookStrings = bookStr.split("-");
            if (!name2Book.containsKey(bookStr)) {
                Book book = new Book(bookStrings[0], bookStrings[1]);
                name2Book.put(bookStr, book);
            }
            name2Library.get(matcher.group("library"))
                    .perform(date, matcher.group("user"),
                            matcher.group("operation"),
                            name2Book.get(matcher.group("book")));
        }
        update(now + 1, true);
    }

    private Library findBook(Book book) {
        for (Library lib : libraries) {
            if (lib.find(book)) {
                return lib;
            }
        }
        return null;
    }

    // TODO: 日期模拟
    private void update(long now, boolean end) {
        HashMap<Book, Library> bookSource = new HashMap<>();
        LocalDate dateLast = BASE.plus(last, ChronoUnit.DAYS);
        // 单日
        if (now > last) {
            singleDay(bookSource, dateLast, end);
        }
        if (end) {
            return;
        }
        long lastCycle = (last - 1) / CYCLE;
        long nowCycle = (now - 1) / CYCLE;
        // 整理日
        if (nowCycle > lastCycle) {
            cycleDay(lastCycle, nowCycle);
        }
        last = now;
    }

    private void singleDay(HashMap<Book, Library> bookSource, LocalDate dateLast, boolean end) {
        for (Request req : pendingQueue) {
            Library lib = findBook(req.getBook());
            if (lib == null) {
                req.setNeedTransport(false);
            } else {
                req.setNeedTransport(true);
                lib.removeBook(req.getBook());
                bookSource.put(req.getBook(), lib);
            }
        }
        // 当日闭馆
        // 闭馆后, 校内预定
        for (Library lib : libraries) {
            for (Request req : pendingQueue) {
                if (req.getLibrary().equals(lib) && !req.needTransport()) {
                    lib.reserveBook(dateLast.toString(), req);
                }
            }
        }
        // 闭馆后, 运出图书
        // 校际借书运出
        for (Request req : pendingQueue) {
            if (req.needTransport()) {
                Library lib = bookSource.get(req.getBook());
                // 若无需运出, 放回
                if (!lib.exportBook(dateLast.toString(), req)) {
                    req.setNeedTransport(false);
                    lib.addBook(req.getBook());
                }
            }
        }

        // 校际还书运出
        for (Entry entry : returnQueue) {
            System.out.printf("[%s] %s-%s got transported by purchasing department in %s\n",
                    dateLast, entry.to(), entry.book(), entry.from());
            System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                    dateLast, entry.book(), "loop", "loop");
        }

        if (end) { return; }

        // 次日开馆
        LocalDate dateNext = BASE.plus(last + 1, ChronoUnit.DAYS);
        // 校际借书运入
        for (Request req : pendingQueue) {
            if (req.needTransport()) {
                Library to = req.getLibrary();
                Library from = bookSource.get(req.getBook());
                to.importBook(dateNext.toString(), from, req.getBook());
            }
        }
        // 校际还书运入
        for (Entry entry : returnQueue) {
            entry.to().importBook(dateNext.toString(), entry.to(), entry.book());
        }
        // 发放校际借阅图书
        for (Request req : pendingQueue) {
            if (req.needTransport()) {
                Library to = req.getLibrary();
                Library from = bookSource.get(req.getBook());
                to.remandBook(dateNext.toString(), from, req);
            }
        }

        // 重置状态
        for (Library lib : libraries) {
            lib.resetRecord();
            lib.resetCnt();
        }
        pendingQueue.clear();
        returnQueue.clear();
    }

    private void cycleDay(long lastCycle, long nowCycle) {
        LocalDate dateNext;
        dateNext = BASE.plus(3 * lastCycle + 4, ChronoUnit.DAYS);
        // 购入图书(要配合hw13写的逻辑)
        for (Library lib : libraries) {
            lib.purchaseBook(dateNext.toString());
        }
        // 整理图书, 发放校内预定(调用hw13)
        System.out.printf("[%s] arranging librarian arranged all the books\n", dateNext);
        for (Library lib : libraries) {
            lib.arrangeBook(dateNext.toString());
        }
        // 输出剩余整理信息
        for (long i = lastCycle + 2; i <= nowCycle; i++) {
            System.out.printf("[%s] arranging librarian arranged all the books\n",
                    BASE.plus(3 * i + 1, ChronoUnit.DAYS));
        }
    }

    public void pend(Library library, User user, Book book) {
        pendingQueue.add(new Request(library, user, book));
    }

    public void returnBook(Library from, Library to, Book book) {
        returnQueue.add(new Entry(from, to, book));
    }
}
