import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MyNetwork implements Network {
    private HashMap<Person, Integer> person2Id;
    private HashMap<Integer, Person> id2Person;
    private Group group;
    private int tripleCnt;

    public MyNetwork() {
        person2Id = new HashMap<>();
        id2Person = new HashMap<>();
        group = new Group();
        tripleCnt = 0;
    }

    @Override
    public boolean contains(int id) {
        return id2Person.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return id2Person.get(id);
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (contains(person.getId())) {
            throw new MyEqualPersonIdException(person.getId());
        }
        person2Id.put(person, person.getId());
        id2Person.put(person.getId(), person);
        group.groupNumUp();
    }

    private void updateTriple(Person dude1, Person dude2) {
        HashSet<Person> acquaintance1 = ((MyPerson) dude1).getAcquaintaince();
        HashSet<Person> acquaintance2 = ((MyPerson) dude2).getAcquaintaince();
        acquaintance1.retainAll(acquaintance2);
        tripleCnt += acquaintance1.size();
    }

    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        MyPerson dude1 = (MyPerson) getPerson(id1);
        MyPerson dude2 = (MyPerson) getPerson(id2);
        dude1.addAcquaintance(dude2, value);
        dude2.addAcquaintance(dude1, value);
        group.addPair(dude1, dude2);
        updateTriple(dude1, dude2);
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
        return group.sameGroup(getPerson(id1), getPerson(id2));
    }

    @Override
    public int queryBlockSum() {
        return group.getGroupNum();
    }

    @Override
    public int queryTripleSum() {
        return tripleCnt;
    }

    private boolean isLinkedOkTest(
            HashMap<Integer, HashMap<Integer, Integer>> data,
            int id1, int id2) {
        if (data.get(id1) == null) {
            return false;
        }
        return data.get(id1).get(id2) != null;
    }

    @Override
    public boolean queryTripleSumOKTest(
            HashMap<Integer, HashMap<Integer, Integer>> beforeData,
            HashMap<Integer, HashMap<Integer, Integer>> afterData, int result) {
        boolean isPure = beforeData.equals(afterData);
        if (!isPure) {
            return false;
        }
        int realRes = 0;
        ArrayList<Integer> idList = new ArrayList<>(beforeData.keySet());
        for (int i = 0; i < idList.size(); i++) {
            for (int j = i + 1; j < idList.size(); j++) {
                for (int k = j + 1; k < idList.size(); k++) {
                    int id1 = idList.get(i);
                    int id2 = idList.get(j);
                    int id3 = idList.get(k);
                    if (isLinkedOkTest(beforeData, id1, id2)
                            && isLinkedOkTest(beforeData, id2, id3)
                            && isLinkedOkTest(beforeData, id3, id1)) {
                        realRes++;
                    }
                }
            }
        }
        return (result == realRes);
    }
}
