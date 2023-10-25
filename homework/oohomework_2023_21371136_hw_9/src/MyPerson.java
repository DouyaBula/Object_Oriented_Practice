import com.oocourse.spec1.main.Person;

import java.util.HashMap;
import java.util.HashSet;

public class MyPerson implements Person {
    private final HashMap<Person, Integer> acquaintance = new HashMap<>();
    private int id;
    private String name;
    private int age;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public void addAcquaintance(Person dude, int dudeValue) {
        acquaintance.put(dude, dudeValue);
    }

    public HashSet<Person> getAcquaintaince() {
        return new HashSet<>(acquaintance.keySet());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            return ((Person) obj).getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public boolean isLinked(Person person) {
        return person.getId() == id || acquaintance.containsKey(person);
    }

    @Override
    public int queryValue(Person person) {
        return acquaintance.getOrDefault(person, 0);
    }

    @Override
    public int compareTo(Person o) {
        return name.compareTo(o.getName());
    }
}
