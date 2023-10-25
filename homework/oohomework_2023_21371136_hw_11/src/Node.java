import com.oocourse.spec3.main.Person;

public class Node implements Comparable<Node> {
    private final Person person;
    private final int dis;

    public Node(Person person, int dis) {
        this.person = person;
        this.dis = dis;
    }

    public Person getPerson() {
        return person;
    }

    public int getDis() {
        return dis;
    }

    @Override
    public int compareTo(Node o) {
        return this.dis - o.dis;
    }
}
