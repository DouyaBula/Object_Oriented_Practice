package modules;

import cell.Book;

import java.util.HashMap;
import java.util.Map;

public class Arrangement {
    private final Bookshelf bookshelf;
    private final Machine machine;
    private final Reception reception;
    private final Logistics logistics;
    private final Reservation reservation;
    private final Management management;
    private final HashMap<Book, Integer> buffer;

    public Arrangement(Bookshelf bookshelf, Machine machine, Reception reception,
                       Logistics logistics, Reservation reservation,
                       Management management) {
        this.bookshelf = bookshelf;
        this.machine = machine;
        this.reception = reception;
        this.logistics = logistics;
        this.reservation = reservation;
        this.management = management;
        this.buffer = new HashMap<>();
    }

    public void arrange(String date) {
        recycle(machine.getBooks());
        machine.reset();
        recycle(reception.getBooks());
        reception.reset();
        recycle(logistics.getBooks());
        logistics.reset();
        recycle(management.getBooks());
        management.reset();
        reservation.distribute(date, buffer);
        for (Map.Entry<Book, Integer> entry : buffer.entrySet()) {
            bookshelf.addBook(entry.getKey(), entry.getValue());
        }
        buffer.replaceAll((key, value) -> 0);
    }

    private void recycle(HashMap<Book, Integer> books) {
        for (Map.Entry<Book, Integer> entry : books.entrySet()) {
            buffer.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }
}
