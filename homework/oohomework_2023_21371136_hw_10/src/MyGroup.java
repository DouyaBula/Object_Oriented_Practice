import com.oocourse.spec2.main.Group;
import com.oocourse.spec2.main.Person;

import java.util.HashMap;
import java.util.HashSet;

public class MyGroup implements Group {
    private int id;
    private HashMap<Integer, Person> id2Person;
    private int valueSum;
    private int ageSum;
    private int ageSquareSum;

    public MyGroup(int id) {
        this.id = id;
        id2Person = new HashMap<>();
        valueSum = 0;
        ageSum = 0;
        ageSquareSum = 0;
    }

    public HashSet<Person> getMember() {
        return new HashSet<>(id2Person.values());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            return ((Group) obj).getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public void addPerson(Person person) {
        for (Person dude :
                id2Person.values()) {
            if (person.isLinked(dude)) {
                valueSum += 2 * person.queryValue(dude);
            }
        }
        ageSum += person.getAge();
        ageSquareSum += person.getAge() * person.getAge();
        id2Person.put(person.getId(), person);
    }

    @Override
    public void delPerson(Person person) {
        id2Person.remove(person.getId());
        for (Person dude :
                id2Person.values()) {
            if (person.isLinked(dude)) {
                valueSum -= 2 * person.queryValue(dude);
            }
        }
        ageSum -= person.getAge();
        ageSquareSum -= person.getAge() * person.getAge();
    }

    public void updateValueSum(int value) {
        valueSum += 2 * value;
    }

    @Override
    public boolean hasPerson(Person person) {
        return id2Person.get(person.getId()) != null;
    }

    // TODO: 算法点1
    @Override
    public int getValueSum() {
        return valueSum;
    }

    @Override
    public int getAgeMean() {
        if (getSize() == 0) {
            return 0;
        } else {
            return ageSum / getSize();
        }
    }

    @Override
    public int getAgeVar() {
        if (getSize() == 0) {
            return 0;
        } else {
            int n = getSize();
            int mean = getAgeMean();
            return (ageSquareSum - 2 * mean * ageSum + n * mean * mean) / n;
        }
    }

    @Override
    public int getSize() {
        return id2Person.values().size();
    }
}
