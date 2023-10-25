import com.oocourse.spec1.main.Person;

import java.util.HashMap;
import java.util.HashSet;

public class Group {
    private int groupNum;
    private int groupIndex;
    private HashMap<Person, Integer> person2Group;
    private HashMap<Integer, HashSet<Person>> group2Persons;

    public Group() {
        this.groupNum = 0;
        this.groupIndex = 0;
        this.person2Group = new HashMap<>();
        this.group2Persons = new HashMap<>();
    }

    private void addPairSub(Person dude1, Person dude2, int group) {
        person2Group.put(dude1, group);
        person2Group.put(dude2, group);
        group2Persons.put(group, new HashSet<>());
        group2Persons.get(group).add(dude1);
        group2Persons.get(group).add(dude2);
    }

    private void addPairSub(Person dude, int group) {
        person2Group.put(dude, group);
        group2Persons.get(group).add(dude);
    }

    public void addPair(Person dude1, Person dude2) {
        int dude1Group = person2Group.getOrDefault(dude1, -1);
        int dude2Group = person2Group.getOrDefault(dude2, -1);
        groupNum--;
        // 两者均未入群: 新建.
        if (dude1Group == -1 && dude2Group == -1) {
            groupIndex++;
            addPairSub(dude1, dude2, groupIndex);
            return;
        }
        // 两者其一未入群: 加入.
        if (dude1Group == -1) {
            addPairSub(dude1, dude2Group);
            return;
        }
        if (dude2Group == -1) {
            addPairSub(dude2, dude1Group);
            return;
        }
        // 两者皆已入群, 但是群友: 跳过.
        if (dude1Group == dude2Group) {
            groupNum++;
            return;
        }
        // 两者皆已入群, 但不是群友: 合并.
        HashSet<Person> group1 = group2Persons.get(dude1Group);
        HashSet<Person> group2 = group2Persons.get(dude2Group);
        if (group1.size() <= group2.size()) {
            for (Person dude : group1) {
                person2Group.replace(dude, dude2Group);
            }
            group2.addAll(group1);
            group2Persons.remove(dude1Group);
        } else {
            for (Person dude : group2) {
                person2Group.replace(dude, dude1Group);
            }
            group1.addAll(group2);
            group2Persons.remove(dude2Group);
        }
    }

    public boolean sameGroup(Person dude1, Person dude2) {
        return dude1.equals(dude2) ||
                person2Group.getOrDefault(dude1, -1)
                        .equals(person2Group.getOrDefault(dude2, -2));
    }

    public void groupNumUp() {
        groupNum++;
    }

    public int getGroupNum() {
        return groupNum;
    }
}
