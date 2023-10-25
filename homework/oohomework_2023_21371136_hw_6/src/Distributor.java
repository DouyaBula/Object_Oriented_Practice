import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.ElevatorRequest;
import com.oocourse.elevator2.MaintainRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    private int requestNum = 0;
    private int elevatorNum;
    private int floorNum;
    private Parameter parameter = new Parameter(INITIAL, MOVE, SWITCH, CAPACITY);
    private ElevatorInput elevatorInput;
    private ArrayList<Elevator> elevators;
    private ArrayList<Integer> elevatorID;
    private HashMap<Integer, Boolean> elevatorMaintained;
    private ArrayList<ArrayList<Entry>> requestPool = new ArrayList<>();
    private Rotator rotator;

    public Distributor(int elevatorNum, int floorNum) {
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        elevators = new ArrayList<>();
        elevatorID = new ArrayList<>();
        elevatorMaintained = new HashMap<>();
        Elevator elevator;
        for (int i = 0; i < elevatorNum; i++) {
            ArrayList<Entry> inRequests = new ArrayList<>();
            for (int j = 0; j < floorNum + 1; j++) {    // 最高+1层存放退出请求
                inRequests.add(j, new Entry(floorNum));
            }
            requestPool.add(inRequests);
            elevator = new Elevator(i + 1, floorNum, parameter, inRequests);
            elevators.add(elevator);
            elevatorID.add(i + 1);
            elevator.start();
        }
        rotator = new Rotator(elevatorNum, PACK, elevatorMaintained);
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
                inRequests
        );
        elevators.add(elevator);
        elevatorID.add(request.getElevatorId());
        elevator.start();
        rotator.elevatorNumIncrement();
        if (LOG) {
            System.out.printf(">> 添加了第%d部电梯, ID为%d\n", elevatorNum, request.getElevatorId());
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
            elevatorMaintained.put(elevatorIndex, true);
            if (LOG) {
                System.out.println(">> Distributor: 电梯" + elevatorID.get(elevatorIndex) +
                        "进入维护");
            }
            Entry entry = requestPool.get(elevatorIndex).get(floorNum);
            entry.addPassenger(4, 0);
            requestPool.get(elevatorIndex).notifyAll();
            requestPool.get(elevatorIndex).wait(); // TODO: 不放心, 需要测试
            // 请求再分配
            for (int pos = 0; pos < floorNum; pos++) {
                Entry rearrange = requestPool.get(elevatorIndex).get(pos);
                for (int to = 0; to < floorNum; to++) {
                    for (Integer passengerID :
                            rearrange.getRequests().get(to)) {
                        distributeRotate(passengerID, pos, to);
                    }
                }
            }
        }
    }

    // 打包轮流分配
    private void distributeRotate(PersonRequest request) {
        int elevatorIndex = rotator.next();
        if (LOG) {
            //System.out.println(">> Rotator: 下一个index为: " + elevatorIndex);
            System.out.println(">> Distributor: 向电梯" + elevatorID.get(elevatorIndex) +
                    "发送了乘客" + request.getPersonId() + "请求");
        }
        distribute(request, elevatorIndex);
    }

    private void distributeRotate(int passengerID, int from, int to) {
        int elevatorIndex = rotator.next();
        if (LOG) {
            //System.out.println(">> Rotator: 下一个index为: " + elevatorIndex);
            System.out.println(">> Distributor: 将乘客" + passengerID +
                    "重分配至电梯" + elevatorID.get(elevatorIndex));
        }
        distribute(elevatorIndex, passengerID, from, to);
    }

    private void distribute(PersonRequest request, int elevatorIndex) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(request.getFromFloor() - 1);
            entry.addPassenger(request.getToFloor() - 1, request.getPersonId());
            requestPool.get(elevatorIndex).notifyAll();
        }
    }

    private void distribute(int elevatorIndex, int passengerID, int from, int to) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(from);
            entry.addPassenger(to, passengerID);
            requestPool.get(elevatorIndex).notifyAll();
        }
    }
}