import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Sender extends Thread {
    private static final boolean LOG = false; // 调试信息输出开关
    private int passengerId;
    private int dst;
    private Stack<SplitRequest> requestStack;
    private ArrayList<ArrayList<Entry>> requestPool;
    private HashMap<Integer, Info> transferInfo;
    private Counter senderCtr;
    private ArrayList<Integer> elevatorID;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private ArrayList<Elevator> elevators;
    private ArrayList<ArrayList<Boolean>> accessibility;
    private Search search;
    private Rotator rotator;
    private int remainder;
    private Info info;

    public Sender(int passengerId, int dst, Stack<SplitRequest> requestStack,
                  ArrayList<ArrayList<Entry>> requestPool,
                  HashMap<Integer, Info> transferInfo, Counter senderCtr,
                  ArrayList<Integer> elevatorID, HashMap<Integer, Boolean> elevatorMaintained,
                  ArrayList<Elevator> elevators, ArrayList<ArrayList<Boolean>> accessibility,
                  Rotator rotator) {
        this.passengerId = passengerId;
        this.dst = dst;
        this.requestStack = requestStack;
        this.requestPool = requestPool;
        this.transferInfo = transferInfo;
        this.senderCtr = senderCtr;
        this.elevatorID = elevatorID;
        this.elevatorMaintained = elevatorMaintained;
        this.elevators = elevators;
        this.accessibility = accessibility;
        this.search = new Search(
                accessibility, elevators, elevatorMaintained, rotator);
        this.rotator = rotator;
    }

    @Override
    public void run() {
        remainder = requestStack.size();
        info = new Info(remainder, dst);
        synchronized (transferInfo) {
            if (transferInfo.get(passengerId) == null) {
                transferInfo.put(passengerId, info);
            } else {    // 外部重规划
                transferInfo.replace(passengerId, info);
            }
        }
        while (true) {
            remainder = info.getCnt();
            SplitRequest splitRequest = requestStack.peek();
            // TODO: 若有电梯维护, 应再次重新规划
            if (reRun(splitRequest)) {
                continue;
            }
            distribute(splitRequest.getElevatorIndex(),
                    splitRequest.getPersonId(),
                    splitRequest.getFromIndex(),
                    splitRequest.getToIndex());
            synchronized (info) {
                while (remainder == info.getCnt()) {
                    try {
                        if (LOG) {
                            System.out.println(">> Sender: 负责乘客" + passengerId + "的线程睡眠");
                        }
                        info.wait();
                        if (LOG) {
                            System.out.println(">> Sender: 负责乘客" + passengerId + "的线程重启");
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                requestStack.pop();
                if (info.getCnt() == 0) {
                    break;
                }
            }
        }
        if (LOG) {
            System.out.println(">> Sender: 负责乘客" + passengerId + "的线程关闭");
        }
        senderCtr.reduce();
    }

    private boolean reRun(SplitRequest splitRequest) {
        synchronized (elevatorMaintained) {
            if (elevatorMaintained.get(splitRequest.getElevatorIndex()) != null) {
                if (LOG) {
                    System.out.println(">> Sender: 负责乘客" + passengerId + "的线程内部重规划");
                }
                Stack<SplitRequest> requestStackNew = new Stack<>();
                int personId = splitRequest.getPersonId();
                int fromIndex = splitRequest.getFromIndex();
                int toIndex = info.getDest();
                dst = toIndex;
                boolean check = search.search(personId, fromIndex, toIndex, requestStackNew);
                requestStack = requestStackNew;
                if (LOG) {
                    if (check) {
                        System.out.println(">> Sender: 乘客" + personId + "重规划请求");
                        System.out.println(requestStack);
                    } else {
                        System.out.println("!!ERROR: 没有找到可达路径");
                    }
                }
                remainder = requestStack.size();
                info = new Info(remainder, dst);
                synchronized (transferInfo) {
                    if (transferInfo.get(passengerId) == null) {
                        transferInfo.put(passengerId, info);
                    } else {    // 100%
                        transferInfo.replace(passengerId, info);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void distribute(int elevatorIndex, int passengerID, int from, int to) {
        synchronized (requestPool.get(elevatorIndex)) {
            if (LOG) {
                System.out.println(">> Sender: 负责乘客"
                        + passengerId + "的线程向电梯" + elevatorID.get(elevatorIndex)
                        + "发送了" + (from + 1) + "->" + (to + 1));
            }
            Entry entry = requestPool.get(elevatorIndex).get(from);
            entry.addPassenger(to, passengerID);
            requestPool.get(elevatorIndex).notifyAll();
        }
    }

}

