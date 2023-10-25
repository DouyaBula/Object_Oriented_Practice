import com.oocourse.spec2.main.Message;
import com.oocourse.spec2.main.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MyPerson implements Person {
    private final HashMap<Person, Integer> acquaintance = new HashMap<>();
    private int id;
    private String name;
    private int age;
    private int socialValue;
    private ArrayList<Message> messages = new ArrayList<>();
    private boolean flag;
    private Person bestDude;
    private int couple;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.socialValue = 0;
        this.flag = false;
        this.bestDude = null;
        this.couple = 0;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getBestId() {
        return bestDude.getId();
    }

    private void updateBestDude(Person dude, int dudeValue) {
        if (bestDude == null) {
            bestDude = dude;
        } else {
            if (dudeValue > acquaintance.get(bestDude)) {
                bestDude = dude;
            }
            if (dudeValue == acquaintance.get(bestDude)) {
                bestDude = bestDude.getId() < dude.getId() ? bestDude : dude;
            }
        }
    }

    public int updateCouple() {
        if (bestDude == null) {
            return 0;
        }
        if (((MyPerson) bestDude).getBestDude().equals(this)) {
            couple = 1;
        } else {
            couple = 0;
        }
        return couple;
    }

    public Person getBestDude() {
        return bestDude;
    }

    public void addAcquaintance(Person dude, int dudeValue) {
        acquaintance.put(dude, dudeValue);
        updateBestDude(dude, dudeValue);
    }

    public void delAcquaintance(Person dude) {
        acquaintance.remove(dude);
        if (bestDude == dude) {
            bestDude = null;
            for (Person otherDude : acquaintance.keySet()) {
                updateBestDude(otherDude, acquaintance.get(otherDude));
            }
        }
    }

    public void setValue(Person person, int valueNew) {
        acquaintance.replace(person, valueNew);
        for (Person otherDude : acquaintance.keySet()) {
            updateBestDude(otherDude, acquaintance.get(otherDude));
        }
    }

    public int getAcquaintanceSize() {
        return acquaintance.keySet().size();
    }

    public HashSet<Person> getAcquaintance() {
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

    @Override
    public void addSocialValue(int num) {
        socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    public void receiveMessage(Message message) {
        messages.add(0, message);
    }

    @Override
    public List<Message> getReceivedMessages() {
        ArrayList<Message> res = new ArrayList<>();
        for (int i = 0; i < Math.min(5, messages.size()); i++) {
            res.add(messages.get(i));
        }
        return res;
    }

}
