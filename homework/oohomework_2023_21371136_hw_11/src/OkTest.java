import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OkTest {
    public static int okTest(int limit, ArrayList<HashMap<Integer, Integer>> beforeData,
                             ArrayList<HashMap<Integer, Integer>> afterData, int result) {
        ArrayList<Integer> emojiIdList = new ArrayList<>();
        ArrayList<Integer> oldEmojiIdList = new ArrayList<>();
        ArrayList<Integer> emojiHeatList = new ArrayList<>();
        ArrayList<Integer> oldEmojiHeatList = new ArrayList<>();
        convert2Jml(beforeData, oldEmojiIdList, oldEmojiHeatList);
        convert2Jml(afterData, emojiIdList, emojiHeatList);
        Integer res1 = checkEmoji(limit, emojiIdList, oldEmojiIdList, emojiHeatList,
                oldEmojiHeatList);
        if (res1 != null) {
            return res1;
        }

        Integer res2 = checkMessage(beforeData, afterData, result, emojiIdList);
        if (res2 != null) {
            return res2;
        }
        return 0;
    }

    private static Integer checkMessage(ArrayList<HashMap<Integer, Integer>> beforeData,
                                        ArrayList<HashMap<Integer, Integer>> afterData, int result,
                                        ArrayList<Integer> emojiIdList) {
        HashMap<Integer, Integer> oldMessages = beforeData.get(1);
        HashMap<Integer, Integer> messages = afterData.get(1);
        for (Map.Entry<Integer, Integer> oldMsg : oldMessages.entrySet()) {
            if (oldMsg.getValue() != null && emojiIdList.contains(oldMsg.getValue())) {
                if (!messages.containsKey(oldMsg.getKey())) {
                    return 5;
                }
                if (!messages.get(oldMsg.getKey()).equals(oldMsg.getValue())) {
                    return 5;
                }
            }
        }

        for (Map.Entry<Integer, Integer> oldMsg : oldMessages.entrySet()) {
            if (oldMsg.getValue() == null) {
                if (!messages.containsKey(oldMsg.getKey())) {
                    return 6;
                }
                if (messages.get(oldMsg.getKey()) != null) {
                    return 6;
                }
            }
        }

        int length = 0;
        for (Map.Entry<Integer, Integer> oldMsg : oldMessages.entrySet()) {
            if (oldMsg.getValue() != null &&
                    emojiIdList.contains(oldMsg.getValue())) {
                length++;
            } else if (oldMsg.getValue() == null) {
                length++;
            }
        }
        if (messages.size() != length) {
            return 7;
        }

        if (result != emojiIdList.size()) {
            return 8;
        }
        return null;
    }

    private static Integer checkEmoji(int limit, ArrayList<Integer> emojiIdList,
                                      ArrayList<Integer> oldEmojiIdList,
                                      ArrayList<Integer> emojiHeatList,
                                      ArrayList<Integer> oldEmojiHeatList) {
        for (int i = 0; i < oldEmojiIdList.size(); i++) {
            int id = oldEmojiIdList.get(i);
            if (oldEmojiHeatList.get(i) >= limit && !emojiIdList.contains(id)) {
                return 1;
            }
        }

        for (int i = 0; i < emojiIdList.size(); i++) {
            int id = emojiIdList.get(i);
            int heat = emojiHeatList.get(i);
            if (!oldEmojiIdList.contains(id) || !oldEmojiHeatList.contains(heat)) {
                return 2;
            }
        }

        int length = 0;
        for (int i = 0; i < oldEmojiIdList.size(); i++) {
            length += oldEmojiHeatList.get(i) >= limit ? 1 : 0;
        }
        if (emojiIdList.size() != length) {
            return 3;
        }

        // 4 is always true
        if (emojiIdList.size() != emojiHeatList.size()) {
            return 4;
        }
        return null;
    }

    private static void convert2Jml(
            ArrayList<HashMap<Integer, Integer>> data,
            ArrayList<Integer> emojiIdList, ArrayList<Integer> emojiHeatList) {
        for (Map.Entry<Integer, Integer> emoji : data.get(0).entrySet()) {
            emojiIdList.add(emoji.getKey());
            emojiHeatList.add(emoji.getValue());
        }
    }
}
