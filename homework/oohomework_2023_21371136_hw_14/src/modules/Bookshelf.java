package modules;

import main.Library;
import template.Server;

public class Bookshelf extends Server {
    public Bookshelf(Library owner) {
        super("Bookshelf", owner);
    }
}
