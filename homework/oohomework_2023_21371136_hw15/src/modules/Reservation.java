package modules;

import cell.Book;
import cell.Request;
import cell.User;
import main.Library;
import template.Server;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Reservation extends Server {
    private final ArrayList<Request> requests;
    private final HashMap<User, Integer> cnt;
    private final HashMap<User, HashSet<Book>> record;

    public Reservation(Library owner) {
        super("ordering librarian", owner);
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
                    if (!(user.hasB() && book.is("B"))
                            && !(user.hasBook(book) && book.is("C"))) {
                        user.borrowBook(LocalDate.parse(date), owner(), book);
                        if (book.is("B")) {
                            clearB(user);
                        }
                        System.out.printf("[%s] %s lent %s-%s to %s-%s\n",
                                date, name(), owner(), book, owner(), user);
                        System.out.printf("(State) [%s] %s transfers from %s to %s\n",
                                date, book, "loop", "loop");
                        System.out.printf("(Sequence) [%s] %s sends a message to %s\n",
                                date, "Reservation", "Library");
                        System.out.printf("[%s] %s-%s borrowed %s-%s from %s\n",
                                date, owner(), user, owner(), book, name());
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

    public boolean reserve(String date, User user, Book book) {
        if (user.hasB() && book.is("B")) {
            return false;
        }
        if (user.hasBook(book) && book.is("C")) {
            return false;
        }
        if (record.containsKey(user) && record.get(user).contains(book)) {
            return false;
        }
        if (cnt.getOrDefault(user, 0) == 3) {
            return false;
        }
        System.out.printf("[%s] %s-%s ordered %s-%s from %s\n",
                date, owner(), user, owner(), book, name());
        System.out.printf("[%s] ordering librarian recorded %s-%s's order of %s-%s\n",
                date, owner(), user, owner(), book);
        System.out.printf("(Sequence) [%s] %s sends a message to %s\n",
                date, "Reservation", "Library");
        if (!cnt.containsKey(user)) {
            cnt.put(user, 1);
            record.put(user, new HashSet<>());
        } else {
            cnt.replace(user, cnt.get(user) + 1);
        }
        record.get(user).add(book);
        requests.add(new Request(owner(), user, book));
        return true;
    }

    public void clearB(User user) {
        if (cnt.containsKey(user)) {
            requests.removeIf(e -> (
                    e.getUser().equals(user) && e.getBook().is("B")));
            record.get(user).removeIf(e -> e.is("B"));
        }
    }
}
