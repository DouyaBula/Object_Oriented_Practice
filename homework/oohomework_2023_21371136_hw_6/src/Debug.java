import java.io.IOException;

public class Debug extends Thread {
    public void debug() throws IOException, InterruptedException {
        int x = -1;
        assert (x < 0);
        assert (x > 0);
        System.out.println("233");
    }

    @Override
    public void run() {
        try {
            debug();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
