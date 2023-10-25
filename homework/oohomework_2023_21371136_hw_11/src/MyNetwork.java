import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualGroupIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.GroupIdNotFoundException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.main.Group;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Person;

import java.util.ArrayList;
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
    private HashMap<Integer, Integer> emojiId2Heat;

    public MyNetwork() {
        id2Person = new HashMap<>();
        block = new Block();
        tripleCnt = 0;
        id2Group = new HashMap<>();
        id2Message = new HashMap<>();
        personId2Group = new HashMap<>();
        emojiId2Heat = new HashMap<>();
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
            EqualMessageIdException, EqualPersonIdException, MyEmojiIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        }
        if (message instanceof MyEmojiMessage &&
                !containsEmojiId(message.getSocialValue())) {
            throw new MyEmojiIdNotFoundException(message.getSocialValue());
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
        if (message instanceof MyEmojiMessage) {
            MyEmojiMessage emoji = (MyEmojiMessage) message;
            emojiId2Heat.replace(emoji.getEmojiId(), emojiId2Heat.get(emoji.getEmojiId()) + 1);
        }
        if (message.getType() == 0) {
            dude1.addSocialValue(message.getSocialValue());
            dude2.addSocialValue(message.getSocialValue());
            dude2.receiveMessage(message);
            if (message instanceof MyRedEnvelopeMessage) {
                MyRedEnvelopeMessage envelop = (MyRedEnvelopeMessage) message;
                dude1.addMoney(-envelop.getMoney());
                dude2.addMoney(envelop.getMoney());
            }
        } else {
            int money = 0;
            if (message instanceof MyRedEnvelopeMessage) {
                money = ((MyRedEnvelopeMessage) message).getMoney() / group.getSize();
                dude1.addMoney(-money * group.getSize());
            }
            for (Person dude : ((MyGroup) group).getMember()) {
                dude.addSocialValue(message.getSocialValue());
                dude.addMoney(money);
            }
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        checkPersonId(id);
        return getPerson(id).getSocialValue();
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        checkPersonId(id);
        return getPerson(id).getReceivedMessages();
    }

    @Override
    public int queryBestAcquaintance(int id) throws PersonIdNotFoundException,
            AcquaintanceNotFoundException {
        checkPersonId(id);
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
    public boolean containsEmojiId(int id) {
        return emojiId2Heat.containsKey(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new MyEqualEmojiIdException(id);
        }
        emojiId2Heat.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        checkPersonId(id);
        return getPerson(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new MyEmojiIdNotFoundException(id);
        }
        return emojiId2Heat.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        id2Message.entrySet().removeIf(m -> m.getValue() instanceof MyEmojiMessage
                && emojiId2Heat.get(m.getValue().getSocialValue()) < limit);
        emojiId2Heat.entrySet().removeIf(e -> e.getValue() < limit);
        return emojiId2Heat.size();
    }

    @Override
    public void clearNotices(int personId) throws PersonIdNotFoundException {
        checkPersonId(personId);
        ((MyPerson) getPerson(personId)).clearNotices();
    }

    @Override
    public int queryLeastMoments(int id) throws PersonIdNotFoundException, PathNotFoundException {
        checkPersonId(id);
        Person root = getPerson(id);
        HashMap<Person, Integer> solved = block.dijkstra(root);
        if (DEBUG) {
            for (Person dude : solved.keySet()) {
                System.out.printf("person%d\tdis: %d\troot: %d\n",
                        dude.getId(), solved.get(dude), getRoot(dude).getId());
            }
        }
        if (solved.size() < 3) {
            throw new MyPathNotFoundException(id);
        }
        int ret = Integer.MAX_VALUE;
        for (Person dude : solved.keySet()) {
            if (!dude.equals(root) && dude.isLinked(root) &&
                    !((MyPerson) dude).getPrev().equals(dude)) {
                ret = Math.min(ret, dude.queryValue(root) + solved.get(dude));
            }
        }
        for (Person dude1 : solved.keySet()) {
            for (Person dude2 : ((MyPerson) dude1).getAc()) {
                if (!dude1.equals(root) && !dude2.equals(root) && !dude1.equals(dude2)
                        && dude1.isLinked(dude2) && !getRoot(dude1).equals(getRoot(dude2))) {
                    ret = Math.min(ret, dude1.queryValue(dude2)
                            + solved.get(dude1) + solved.get(dude2));
                }
            }
        }
        if (ret == Integer.MAX_VALUE) {
            throw new MyPathNotFoundException(id);
        }
        return ret;
    }

    private Person getRoot(Person dude) {
        MyPerson root = (MyPerson) dude;
        while (!root.getPrev().equals(root)) {
            root = (MyPerson) root.getPrev();
        }
        return root;
    }

    @Override
    public int deleteColdEmojiOKTest(int limit, ArrayList<HashMap<Integer, Integer>> beforeData,
                                     ArrayList<HashMap<Integer, Integer>> afterData, int result) {
        return OkTest.okTest(limit, beforeData, afterData, result);
    }

}
