import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.ElevatorRequest;
import com.oocourse.elevator3.MaintainRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.Semaphore;

public class Distributor extends Thread {
    /*  floorNum层特殊请求编码  */
    /*-----------------------*/
    /* | to | id | meaning  |*/
    /* | 0  | *  | exit     |*/
    /* | 1  | 1  | full     |*/
    /* | 2  | *  | pos      |*/
    /* | 3  | *  | target   |*/
    /* | 4  | *  | maintain |*/
    /* |    |    |          |*/
    /* |    |    |          |*/
    /*-----------------------*/
    private static final boolean LOG = false; // 调试信息输出开关
    private static final int ELEVATOR = 6;  // 电梯总数
    private static final int FLOOR = 11;    // 楼层总数
    private static final int INITIAL = 0;   // 初始位置
    private static final long MOVE = 400; // 移动时间
    private static final long SWITCH = 200;   // 开/关门时间
    private static final int CAPACITY = 6;  // 限乘人数
    private static final int PACK = 3;  // 轮转分配时打包大小
    private static final int MX = 4;    // 最大服务中个数
    private static final int NX = 2;    // 最大只接人个数
    // 打包轮流分配
    // 轮转调度
    private int indexRotate = 0;
    private int requestNum = 0;
    private int elevatorNum;
    private int floorNum;
    private Parameter parameter = new Parameter(INITIAL, MOVE, SWITCH, CAPACITY);
    private ElevatorInput elevatorInput;
    private ArrayList<Elevator> elevators;
    private ArrayList<Integer> elevatorID;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private ArrayList<ArrayList<Boolean>> accessibility;
    private HashMap<Integer, Info> transferInfo;
    private ArrayList<ArrayList<Entry>> requestPool = new ArrayList<>();
    private Rotator rotator;
    private Counter senderCtr = new Counter();
    private Search search;
    private ArrayList<Semaphore> semaphoresM = new ArrayList<>();
    private ArrayList<Semaphore> semaphoresN = new ArrayList<>();

    public Distributor(int elevatorNum, int floorNum) {
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        elevators = new ArrayList<>();
        elevatorID = new ArrayList<>();
        elevatorMaintained = new HashMap<>();
        accessibility = new ArrayList<>();
        transferInfo = new HashMap<>();
        Elevator elevator;
        for (int i = 0; i < elevatorNum; i++) {
            ArrayList<Entry> inRequests = new ArrayList<>();
            for (int j = 0; j < floorNum + 1; j++) {    // 最高+1层存放退出请求
                inRequests.add(j, new Entry(floorNum));
            }
            requestPool.add(inRequests);

            ArrayList<Boolean> accessibilityPiece = new ArrayList<>();
            for (int j = 0; j < floorNum; j++) {
                accessibilityPiece.add(j, true);
            }
            accessibility.add(accessibilityPiece);

            elevator = new Elevator(i + 1, floorNum, parameter,
                    inRequests, transferInfo, semaphoresM, semaphoresN);
            elevators.add(elevator);
            elevatorID.add(i + 1);
            elevator.start();
        }
        rotator = new Rotator(elevatorNum, PACK,
                elevatorMaintained, accessibility);
        search = new Search(accessibility, elevators, elevatorMaintained, rotator);
        for (int i = 0; i < floorNum; i++) {
            semaphoresM.add(new Semaphore(MX, true));
            semaphoresN.add(new Semaphore(NX, true));
        }
    }

    private void addPerson(PersonRequest request) {
        requestNum++;
        distributeRotate(request);
    }

    private void addElevator(ElevatorRequest request) {
        elevatorNum++;
        ArrayList<Entry> inRequests = new ArrayList<>();
        for (int i = 0; i < floorNum + 1; i++) {
            inRequests.add(i, new Entry(floorNum));
        }
        requestPool.add(inRequests);

        ArrayList<Boolean> accessibilityPiece = new ArrayList<>();
        for (int j = 0; j < floorNum; j++) {
            Boolean accessible = (request.getAccess() & (1 << j)) != 0;
            accessibilityPiece.add(j, accessible);
        }
        accessibility.add(accessibilityPiece);

        Parameter newParameter = new Parameter(
                request.getFloor() - 1,
                (long) (request.getSpeed() * 1000),
                SWITCH,
                request.getCapacity()
        );
        Elevator elevator = new Elevator(
                request.getElevatorId(),
                floorNum,
                newParameter,
                inRequests,
                transferInfo,
                semaphoresM,
                semaphoresN
        );
        elevators.add(elevator);
        elevatorID.add(request.getElevatorId());
        elevator.start();
        rotator.elevatorNumIncrement();
        if (LOG) {
            System.out.printf(">> 添加了第%d部电梯, ID为%d\n", elevatorNum, request.getElevatorId());
            System.out.println(">>>> 其可达楼层为" + accessibilityPiece);
        }
    }

    private void setMaintain(MaintainRequest request) throws InterruptedException {
        for (int i = 0; i < elevatorID.size(); i++) {
            if (elevatorID.get(i).equals(request.getElevatorId())) {
                maintain(i);
                break;
            }
        }
    }

    @Override
    public void run() {
        Request request;
        elevatorInput = new ElevatorInput(System.in);
        while (true) {
            request = elevatorInput.nextRequest();
            if (request == null) {
                synchronized (senderCtr) {
                    while (!senderCtr.isZero()) {
                        try {
                            if (LOG) {
                                System.out.println("Distributor: "
                                        + "因Sender剩余" + senderCtr + "而睡眠");
                            }
                            senderCtr.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                for (int i = 0; i < elevatorNum; i++) {
                    close(i);
                }
                break;
                // TODO: 可能所有电梯都被维护, 不存在可用电梯, 从而分配失败
            } else if (request instanceof PersonRequest) {
                addPerson((PersonRequest) request);
            } else if (request instanceof ElevatorRequest) {
                addElevator((ElevatorRequest) request);
            } else if (request instanceof MaintainRequest) {
                try {
                    setMaintain((MaintainRequest) request);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (LOG) {
            System.out.println(">> Distributor关闭");
        }
    }

    private void close(int elevatorIndex) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(floorNum);
            entry.addPassenger(0, 0);
            requestPool.get(elevatorIndex).notifyAll();
        }
    }

    private void maintain(int elevatorIndex) throws InterruptedException {
        synchronized (requestPool.get(elevatorIndex)) {
            synchronized (elevatorMaintained) {
                elevatorMaintained.put(elevatorIndex, true);
            }
            if (LOG) {
                System.out.println(">> Distributor: 电梯" + elevatorID.get(elevatorIndex) +
                        "进入维护");
            }
            Entry entry = requestPool.get(elevatorIndex).get(floorNum);
            entry.addPassenger(4, 0);
            requestPool.get(elevatorIndex).notifyAll();
            requestPool.get(elevatorIndex).wait();
            // 请求再分配
            for (int pos = 0; pos < floorNum; pos++) {
                Entry rearrange = requestPool.get(elevatorIndex).get(pos);
                for (int to = 0; to < floorNum; to++) {
                    for (Integer passengerID :
                            rearrange.getRequests().get(to)) {
                        // 若为需要换乘的请求, 同时关闭其Sender, 并设置为正确的to值
                        PersonRequest personRequest;
                        synchronized (transferInfo) {
                            if (transferInfo.get(passengerID) != null) {
                                Info info = transferInfo.get(passengerID);
                                synchronized (info) {
                                    info.clear();
                                    info.notifyAll();
                                }
                                personRequest = new PersonRequest(
                                        pos + 1, info.getDest() + 1, passengerID);
                            } else {
                                personRequest = new PersonRequest(pos + 1, to + 1, passengerID);
                            }
                        }
                        distributeRotate(personRequest);
                    }
                }
            }
        }
    }

    private void distributeRotate(PersonRequest request) {
        Stack<SplitRequest> requestStack = new Stack<>();
        int personId = request.getPersonId();
        int fromIndex = request.getFromFloor() - 1;
        int toIndex = request.getToFloor() - 1;
        boolean check = search.search(personId, fromIndex, toIndex, requestStack);
        if (LOG) {
            if (check) {
                System.out.println(">> Distributor: 发送乘客" + personId + "请求");
                System.out.println(requestStack);
            } else {
                System.out.println("!!ERROR: 没有找到可达路径");
            }
        }
        // 对于直达请求, 直接发送
        if (requestStack.size() == 1) {
            SplitRequest splitRequest = requestStack.pop();
            distribute(request, splitRequest.getElevatorIndex());
            return;
        }
        // 对于需要换乘的请求, 新开一个线程处理
        senderCtr.increase();
        Sender sender = new Sender(personId, toIndex, requestStack,
                requestPool, transferInfo, senderCtr, elevatorID,
                elevatorMaintained, elevators, accessibility, rotator);
        sender.start();
    }

    private void distribute(PersonRequest request, int elevatorIndex) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(request.getFromFloor() - 1);
            entry.addPassenger(request.getToFloor() - 1, request.getPersonId());
            requestPool.get(elevatorIndex).notifyAll();
        }
    }
}