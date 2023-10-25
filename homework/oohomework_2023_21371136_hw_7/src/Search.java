import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Search {
    private int elevatorNum;
    private int floorNum = 11;
    private ArrayList<ArrayList<Boolean>> accessibility;
    private ArrayList<Elevator> elevators;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private Rotator rotator;

    public Search(ArrayList<ArrayList<Boolean>> accessibility,
                  ArrayList<Elevator> elevators,
                  HashMap<Integer, Boolean> elevatorMaintained,
                  Rotator rotator) {
        this.accessibility = accessibility;
        this.elevators = elevators;
        this.elevatorMaintained = elevatorMaintained;
        this.rotator = rotator;
    }

    public boolean search(int personId, int from,
                          int to, Stack<SplitRequest> requestStack) {
        synchronized (elevators) {
            elevatorNum = elevators.size();
        }
        ArrayList<Boolean> bfsFlags = new ArrayList<>();
        for (int i = 0; i < floorNum; i++) {
            bfsFlags.add(false);
        }
        return bfs(personId, from, to, requestStack, bfsFlags);
    }

    private boolean bfs(int personId, int from, int to,
                        Stack<SplitRequest> requestStack,
                        ArrayList<Boolean> bfsFlags) {
        bfsFlags.set(to, true);
        int selElvtr = rotator.next(to);
        ArrayList<ArrayList<Integer>> subToList = new ArrayList<>();
        for (int i = 0; i < floorNum; i++) {
            subToList.add(new ArrayList<>());
        }
        for (int i = 0; i < elevatorNum;
             i++, selElvtr = (selElvtr + 1) % elevatorNum) {
            if (elevatorMaintained.get(selElvtr) != null) {
                continue;
            }
            if (accessibility.get(selElvtr).get(to)) {
                if (accessibility.get(selElvtr).get(from)) {
                    SplitRequest splitRequest = new SplitRequest(personId, from, to, selElvtr);
                    requestStack.push(splitRequest);
                    return true;
                }
                for (int j = 0; j < floorNum; j++) {
                    if (bfsFlags.get(j)) {
                        continue;
                    }
                    if (accessibility.get(selElvtr).get(j)) {
                        subToList.get(j).add(selElvtr);
                    }
                }
            }
        }

        int subSelected = -1;
        int transferCnt = 114514;
        Stack<SplitRequest> stackAttach = null;
        for (int sub = 0; sub < floorNum; sub++) {
            ArrayList<Integer> subList = subToList.get(sub);
            if (subList.isEmpty()) {
                continue;
            }
            Stack<SplitRequest> stackTemp = new Stack<>();
            ArrayList<Boolean> nextBfsFlags = new ArrayList<>();
            for (int i = 0; i < floorNum; i++) {
                nextBfsFlags.add(bfsFlags.get(i));
            }
            if (bfs(personId, from, sub, stackTemp,
                    nextBfsFlags)) {
                if (stackTemp.size() != 0 &&
                        stackTemp.size() < transferCnt) {
                    subSelected = sub;
                    transferCnt = stackTemp.size();
                    stackAttach = stackTemp;
                }
            }
        }
        if (stackAttach == null) {
            bfsFlags.set(to, false);
            return false;
        } else {
            joinStack(personId, to, requestStack, subToList, subSelected, stackAttach);
            return true;
        }
    }

    private static void joinStack(int personId, int to, Stack<SplitRequest> requestStack,
                                  ArrayList<ArrayList<Integer>> subToList, int subSelected,
                                  Stack<SplitRequest> stackAttach) {
        SplitRequest splitRequest = new SplitRequest(personId, subSelected, to,
                subToList.get(subSelected).get(0));
        requestStack.push(splitRequest);
        Stack<SplitRequest> stackExchange = new Stack<>();
        while (!stackAttach.isEmpty()) {
            stackExchange.push(stackAttach.pop());
        }
        while (!stackExchange.isEmpty()) {
            requestStack.push(stackExchange.pop());
        }
    }
}
