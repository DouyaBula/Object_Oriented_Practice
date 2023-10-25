package modules;

import cell.Book;
import cell.Request;
import cell.User;
import template.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Reservation extends Server {
    private final ArrayList<Request> requests;
    private final HashMap<User, Integer> cnt;
    private final HashMap<User, HashSet<Book>> record;

    public Reservation() {
        super("ordering librarian");
        this.requests = new ArrayList<>();
        this.cnt = new HashMap<>();
        this.record = new HashMap<>();
    }

    @Override
    public void reset() {
        cnt.replaceAll((key, value) -> 0);
    }

    public void distribute(String date, HashMap<Book, Integer> buffer) {
        boolean finish;
        do {
            finish = true;
            for (Request request : requests) {
                User user = request.getUser();
                Book book = request.getBook();
                if (buffer.getOrDefault(book, 0) > 0) {
                    if (!(user.hasB() && book.is("B"))) {
                        user.borrowBook(book);
                        if (book.is("B")) {
                            clearB(user);
                        }
                        System.out.printf(
                                "[%s] %s borrowed %s from %s\n", date, user, book, name());
                        buffer.replace(book, buffer.get(book) - 1);
                    }
                    requests.remove(request);
                    record.get(user).remove(book);
                    finish = false;
                    break;
                }
            }
        } while (!finish);
    }

    public void reserve(String date, User user, Book book) {
        if (user.hasB() && book.is("B")) {
            return;
        }
        if (user.hasBook(book) && book.is("C")) {
            return;
        }
        if (record.containsKey(user) && record.get(user).contains(book)) {
            return;
        }
        if (cnt.getOrDefault(user, 0) == 3) {
            return;
        }
        System.out.printf("[%s] %s ordered %s from %s\n", date, user, book, name());
        if (!cnt.containsKey(user)) {
            cnt.put(user, 1);
            record.put(user, new HashSet<>());
        } else {
            cnt.replace(user, cnt.get(user) + 1);
        }
        record.get(user).add(book);
        requests.add(new Request(user, book));
    }

    public void clearB(User user) {
        if (cnt.containsKey(user)) {
            requests.removeIf(e -> (
                    e.getUser().equals(user) && e.getBook().is("B")));
            record.get(user).removeIf(e -> e.is("B"));
        }
    }
}
