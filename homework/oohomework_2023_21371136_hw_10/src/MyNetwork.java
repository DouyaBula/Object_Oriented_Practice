import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualGroupIdException;
import com.oocourse.spec2.exceptions.EqualMessageIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.GroupIdNotFoundException;
import com.oocourse.spec2.exceptions.MessageIdNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.main.Group;
import com.oocourse.spec2.main.Message;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MyNetwork implements Network {
    public static final boolean DEBUG = false;
    private HashMap<Integer, Person> id2Person;
    private Block block;
    private int tripleCnt;
    private int coupleCnt;
    private HashMap<Integer, Group> id2Group;
    private HashMap<Integer, Message> id2Message;
    private HashMap<Integer, HashSet<Group>> personId2Group;

    public MyNetwork() {
        id2Person = new HashMap<>();
        block = new Block();
        tripleCnt = 0;
        id2Group = new HashMap<>();
        id2Message = new HashMap<>();
        personId2Group = new HashMap<>();
    }

    private void checkPersonId(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    private void checkPersonId(int id1, int id2) throws PersonIdNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
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
        id2Person.put(person.getId(), person);
        block.blockNumUp();
    }

    private void updateTriple(boolean isAdd, Person dude1, Person dude2) {
        HashSet<Person> acquaintance1 = ((MyPerson) dude1).getAcquaintance();
        HashSet<Person> acquaintance2 = ((MyPerson) dude2).getAcquaintance();
        acquaintance1.retainAll(acquaintance2);
        if (isAdd) {
            tripleCnt += acquaintance1.size();
        } else {
            tripleCnt -= acquaintance1.size();
        }
    }

    private void updateCouple() {
        coupleCnt = 0;
        for (Person dude :
                id2Person.values()) {
            coupleCnt += ((MyPerson) dude).updateCouple();
        }
    }

    private void updateValueSum(int id1, int id2, int value) {
        if (personId2Group.get(id1) == null
                || personId2Group.get(id2) == null) {
            return;
        }
        HashSet<Group> groups1 = new HashSet<>(personId2Group.get(id1));
        HashSet<Group> groups2 = new HashSet<>(personId2Group.get(id2));
        groups1.retainAll(groups2);
        for (Group group :
                groups1) {
            ((MyGroup) group).updateValueSum(value);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        checkPersonId(id1, id2);
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        MyPerson dude1 = (MyPerson) getPerson(id1);
        MyPerson dude2 = (MyPerson) getPerson(id2);
        dude1.addAcquaintance(dude2, value);
        dude2.addAcquaintance(dude1, value);
        block.addPair(dude1, dude2);
        updateTriple(true, dude1, dude2);
        updateCouple();
        updateValueSum(id1, id2, value);
    }

    // TODO: 算法点2 - 删边操作带来的影响
    @Override
    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
            EqualPersonIdException, RelationNotFoundException {
        checkPersonId(id1, id2);
        if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        }
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        MyPerson dude1 = (MyPerson) getPerson(id1);
        MyPerson dude2 = (MyPerson) getPerson(id2);
        int valueOld = dude1.queryValue(dude2);
        int valueNew = valueOld + value;
        if (valueNew > 0) {
            dude1.setValue(dude2, valueNew);
            dude2.setValue(dude1, valueNew);
            updateValueSum(id1, id2, value);
        } else {
            dude1.delAcquaintance(dude2);
            dude2.delAcquaintance(dude1);
            block.delPair(dude1, dude2);
            updateTriple(false, dude1, dude2);
            updateValueSum(id1, id2, -valueOld);
        }
        updateCouple();
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        checkPersonId(id1, id2);
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        checkPersonId(id1, id2);
        return block.sameBlock(getPerson(id1), getPerson(id2));
    }

    @Override
    public int queryBlockSum() {
        if (DEBUG) {
            block.print();
            System.out.print(">> total cnt: ");
        }
        return block.getBlockNum();
    }

    @Override
    public int queryTripleSum() {
        return tripleCnt;
    }

    @Override
    public void addGroup(Group group) throws EqualGroupIdException {
        if (getGroup(group.getId()) != null) {
            throw new MyEqualGroupIdException(group.getId());
        }
        id2Group.put(group.getId(), group);
    }

    @Override
    public Group getGroup(int id) {
        return id2Group.get(id);
    }

    @Override
    public void addToGroup(int id1, int id2) throws GroupIdNotFoundException,
            PersonIdNotFoundException, EqualPersonIdException {
        Group temp = getGroup(id2);
        Person dude = getPerson(id1);
        if (temp == null) {
            throw new MyGroupIdNotFoundException(id2);
        }
        if (dude == null) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (temp.hasPerson(dude)) {
            throw new MyEqualPersonIdException(id1);
        }
        if (temp.getSize() > 1111) {
            return;
        }
        if (personId2Group.get(id1) == null) {
            personId2Group.put(id1, new HashSet<>());
        }
        personId2Group.get(id1).add(temp);
        temp.addPerson(dude);
    }

    @Override
    public void delFromGroup(int id1, int id2) throws GroupIdNotFoundException,
            PersonIdNotFoundException, EqualPersonIdException {
        Group temp = getGroup(id2);
        Person dude = getPerson(id1);
        if (temp == null) {
            throw new MyGroupIdNotFoundException(id2);
        }
        if (dude == null) {
            throw new MyPersonIdNotFoundException(id1);
        }
        if (!temp.hasPerson(dude)) {
            throw new MyEqualPersonIdException(id1);
        }
        personId2Group.get(id1).remove(getGroup(id2));
        temp.delPerson(dude);
    }

    @Override
    public int queryGroupValueSum(int id) throws GroupIdNotFoundException {
        Group temp = getGroup(id);
        if (temp == null) {
            throw new MyGroupIdNotFoundException(id);
        }
        return temp.getValueSum();
    }

    @Override
    public int queryGroupAgeVar(int id) throws GroupIdNotFoundException {
        Group temp = getGroup(id);
        if (temp == null) {
            throw new MyGroupIdNotFoundException(id);
        }
        return temp.getAgeVar();
    }

    @Override
    public boolean containsMessage(int id) {
        return id2Message.containsKey(id);
    }

    @Override
    public void addMessage(Message message) throws
            EqualMessageIdException, EqualPersonIdException {
        if (id2Message.containsKey(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        }
        if (message.getType() == 0 &&
                message.getPerson1().equals(message.getPerson2())) {
            throw new MyEqualPersonIdException(message.getPerson1().getId());
        }
        id2Message.put(message.getId(), message);
    }

    @Override
    public Message getMessage(int id) {
        return id2Message.get(id);
    }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
            MessageIdNotFoundException, PersonIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MyMessageIdNotFoundException(id);
        }
        Message message = getMessage(id);
        if (message.getType() == 0 && !message.getPerson1().isLinked(message.getPerson2())) {
            throw new MyRelationNotFoundException(message.getPerson1().getId(),
                    message.getPerson2().getId());
        }
        if (message.getType() == 1 && !message.getGroup().hasPerson(message.getPerson1())) {
            throw new MyPersonIdNotFoundException(message.getPerson1().getId());
        }
        id2Message.remove(message.getId());
        MyPerson dude1 = (MyPerson) message.getPerson1();
        MyPerson dude2 = (MyPerson) message.getPerson2();
        Group group = message.getGroup();
        if (message.getType() == 0) {
            dude1.addSocialValue(message.getSocialValue());
            dude2.addSocialValue(message.getSocialValue());
            dude2.receiveMessage(message);
        } else {
            for (Person dude : ((MyGroup) group).getMember()) {
                dude.addSocialValue(message.getSocialValue());
            }
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        return getPerson(id).getSocialValue();
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        return getPerson(id).getReceivedMessages();
    }

    @Override
    public int queryBestAcquaintance(int id) throws PersonIdNotFoundException,
            AcquaintanceNotFoundException {
        if (!contains(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        if (((MyPerson) getPerson(id)).getAcquaintanceSize() == 0) {
            throw new MyAcquaintanceNotFoundException(id);
        }
        return ((MyPerson) getPerson(id)).getBestId();
    }

    @Override
    public int queryCoupleSum() {
        return coupleCnt / 2;
    }

    @Override
    public int modifyRelationOKTest(
            int id1, int id2, int value, HashMap<Integer, HashMap<Integer, Integer>> beforeData,
            HashMap<Integer, HashMap<Integer, Integer>> afterData) {
        HashSet<Integer> oldPeople = new HashSet<>(beforeData.keySet());
        HashSet<Integer> newPeople = new HashSet<>(afterData.keySet());
        // check exceptional_behavior
        Integer ret = checkExc(id1, id2, beforeData, afterData, oldPeople);
        if (ret != null) {
            return ret;
        }
        // check 1~3
        ret = checkPre(id1, id2, beforeData, afterData, oldPeople, newPeople);
        if (ret != null) {
            return ret;
        }
        // check 4~14
        ret = checkFirstHalf(id1, id2, value, beforeData, afterData);
        if (ret != null) {
            return ret;
        }
        // check 15~21
        ret = checkLastHalf(id1, id2, value, beforeData, afterData);
        if (ret != null) {
            return ret;
        }
        return 0;
    }

    private Integer checkLastHalf(int id1, int id2, int value, HashMap<Integer, HashMap<Integer,
            Integer>> beforeData, HashMap<Integer, HashMap<Integer, Integer>> afterData) {
        if (queryValueT(id1, id2, beforeData) + value <= 0) {
            if (isLinkedT(id1, id2, afterData) || isLinkedT(id2, id1, afterData)) {
                return 15;
            }
            if (getAcquaintanceT(id1, beforeData).size() !=
                    getAcquaintanceT(id1, afterData).size() + 1) {
                return 16;
            }
            if (getAcquaintanceT(id2, beforeData).size() !=
                    getAcquaintanceT(id2, afterData).size() + 1) {
                return 17;
            }
            // 18, 19 are always true.
            for (Integer dude : getAcquaintanceT(id1, afterData)) {
                if (!getAcquaintanceT(id1, beforeData).contains(dude) ||
                        queryValueT(id1, dude, beforeData) != queryValueT(id1, dude, afterData)) {
                    return 20;
                }
            }
            for (Integer dude : getAcquaintanceT(id2, afterData)) {
                if (!getAcquaintanceT(id2, beforeData).contains(dude) ||
                        queryValueT(id2, dude, beforeData) != queryValueT(id2, dude, afterData)) {
                    return 21;
                }
            }
        }
        return null;
    }

    private Integer checkFirstHalf(int id1, int id2, int value, HashMap<Integer, HashMap<Integer,
            Integer>> beforeData, HashMap<Integer, HashMap<Integer, Integer>> afterData) {
        if (queryValueT(id1, id2, beforeData) + value > 0) {
            if (!isLinkedT(id1, id2, afterData) || !isLinkedT(id2, id1, afterData)) {
                return 4;
            }
            if (queryValueT(id1, id2, beforeData) + value !=
                    queryValueT(id1, id2, afterData)) {
                return 5;
            }
            if (queryValueT(id2, id1, beforeData) + value !=
                    queryValueT(id2, id1, afterData)) {
                return 6;
            }
            if (getAcquaintanceT(id1, beforeData).size() !=
                    getAcquaintanceT(id1, afterData).size()) {
                return 7;
            }
            if (getAcquaintanceT(id2, beforeData).size() !=
                    getAcquaintanceT(id2, afterData).size()) {
                return 8;
            }
            if (!getAcquaintanceT(id1, beforeData).equals(
                    getAcquaintanceT(id1, afterData))) {
                return 9;
            }
            if (!getAcquaintanceT(id2, beforeData).equals(
                    getAcquaintanceT(id2, afterData))) {
                return 10;
            }
            for (Integer dude :
                    getAcquaintanceT(id1, afterData)) {
                if (dude == id2) {
                    continue;
                }
                if (queryValueT(id1, dude, beforeData) != queryValueT(id1, dude, afterData)) {
                    return 11;
                }
            }
            for (Integer dude :
                    getAcquaintanceT(id2, afterData)) {
                if (dude == id1) {
                    continue;
                }
                if (queryValueT(id2, dude, beforeData) != queryValueT(id2, dude, afterData)) {
                    return 12;
                }
            }
            // 13, 14 are always true.
        }
        return null;
    }

    private Integer checkPre(
            int id1, int id2, HashMap<Integer, HashMap<Integer, Integer>> beforeData,
            HashMap<Integer, HashMap<Integer, Integer>> afterData,
            HashSet<Integer> oldPeople, HashSet<Integer> newPeople) {
        if (newPeople.size() != oldPeople.size()) {
            return 1;
        }
        for (Integer beforeId : oldPeople) {
            if (!newPeople.contains(beforeId)) {
                return 2;
            }
        }
        for (Integer newId : newPeople) {
            if (newId == id1 || newId == id2) {
                continue;
            }
            if (!beforeData.get(newId).equals(afterData.get(newId))) {
                return 3;
            }
        }
        return null;
    }

    private Integer checkExc(
            int id1, int id2, HashMap<Integer, HashMap<Integer, Integer>> beforeData,
            HashMap<Integer, HashMap<Integer, Integer>> afterData, HashSet<Integer> oldPeople) {
        if (!oldPeople.contains(id1) || !oldPeople.contains(id2)
                || !isLinkedT(id1, id2, beforeData) || id1 == id2) {
            return beforeData.equals(afterData) ? 0 : -1;
        }
        return null;
    }

    private HashSet<Integer> getAcquaintanceT(
            int id, HashMap<Integer, HashMap<Integer, Integer>> beforeData) {
        return new HashSet<>(beforeData.get(id).keySet());
    }

    private boolean isLinkedT(
            int id1, int id2, HashMap<Integer, HashMap<Integer, Integer>> data) {
        return data.get(id1).get(id2) != null;
    }

    private int queryValueT(
            int id1, int id2, HashMap<Integer, HashMap<Integer, Integer>> data) {
        return data.get(id1).get(id2);
    }
}
