import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Distributor extends Thread {
    /* floorNum层特殊请求编码 */
    /*----------------------*/
    /* | to | id | meaning |*/
    /* | 0  | *  | exit    |*/
    /* | 1  | 1  | full    |*/
    /* | 2  | *  | pos     |*/
    /* | 3  | *  | target  |*/
    /* |    |    |         |*/
    /*----------------------*/
    private static final boolean LOG = false; // 调试信息输出开关
    private static final int ELEVATOR = 6;  // 电梯总数
    private static final int FLOOR = 11;    // 楼层总数
    private static final int INITIAL = 0;   // 初始位置
    private static final long MOVE = 400; // 移动时间
    private static final long SWITCH = 200;   // 开/关门时间
    private static final int CAPACITY = 6;  // 限乘人数
    private static final int PACK = 6;  // 轮转分配时打包大小
    private int requestNum = 0;
    private int elevatorNum;
    private int floorNum;
    private Parameter parameter = new Parameter(INITIAL, MOVE, SWITCH, CAPACITY);
    private ElevatorInput elevatorInput;
    private ArrayList<Elevator> elevators;
    private ArrayList<ArrayList<Entry>> requestPool = new ArrayList<>();
    private Rotator rotator;

    public Distributor(int elevatorNum, int floorNum) {
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        elevators = new ArrayList<>();
        Elevator elevator;
        for (int i = 0; i < elevatorNum; i++) {
            ArrayList<Entry> inRequests = new ArrayList<>();
            for (int j = 0; j < floorNum + 1; j++) {    // 最高+1层存放退出请求
                inRequests.add(j, new Entry(floorNum));
            }
            requestPool.add(inRequests);
            elevator = new Elevator(i + 1, floorNum, parameter, inRequests);
            elevators.add(elevator);
            elevator.start();
        }
        rotator = new Rotator(elevatorNum, PACK);
    }

    @Override
    public void run() {
        PersonRequest request;
        elevatorInput = new ElevatorInput(System.in);
        while (true) {
            request = elevatorInput.nextPersonRequest();
            if (request == null) {
                for (int i = 0; i < elevatorNum; i++) {
                    close(i);
                }
                break;
            } else {
                requestNum++;
                try {
                    distributeRotate(request);
                    /*
                    if (requestNum <= 10) {
                        distributeRotate(request);
                    } else {
                        if (!distributePick(request)) {
                            if (!distributeClosestIdle(request)) {
                                if (!distributeClosest(request)) {
                                    distributeRandom(request);
                                }
                            }
                        }
                    }
                    */
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
            System.out.println(">> Distributor Thread exited");
        }
    }

    private void close(int elevatorIndex) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(floorNum);
            entry.addPassenger(0, 0);
            requestPool.get(elevatorIndex).notifyAll();
        }
    }

    // 全随机分配
    private void distributeRandom(PersonRequest request) throws InterruptedException {
        Random random = new Random();
        int elevatorIndex = random.nextInt(elevatorNum);
        if (LOG) {
            System.out.println(">>Distributor: 随机分配: 随机至电梯" + (elevatorIndex + 1));
            System.out.println(">>因此向电梯" + (elevatorIndex + 1) +
                    "发送了" + requestNum + "号请求");
        }
        distribute(request, elevatorIndex);
    }

    // 分配至可捎带电梯
    private boolean distributePick(PersonRequest request) throws InterruptedException {
        int from = request.getFromFloor() - 1;
        int to = request.getToFloor() - 1;
        boolean requestRising = to - from > 0;
        int elevatorIndex = -1;
        for (int i = 0; i < elevatorNum; i++) {
            synchronized (requestPool.get(i)) {
                boolean full = requestPool.get(i).get(floorNum).getRequests()
                        .get(1).get(0) == 1;
                int pos = requestPool.get(i).get(floorNum).getRequests()
                        .get(2).get(0);
                int target = requestPool.get(i).get(floorNum).getRequests()
                        .get(3).get(0);
                if (target == -1 || full) {
                    continue;
                }
                boolean elevatorRising = target - pos > 0;
                if (elevatorRising != requestRising) {
                    continue;
                }
                if ((elevatorRising && from > pos)
                        || (!elevatorRising && from < pos)) {
                    elevatorIndex = i;
                    distribute(request, elevatorIndex);
                    if (LOG) {
                        System.out.println(">>Distributor: 捎带分配: 电梯" + (elevatorIndex + 1) +
                                "的所在楼层为" + (pos + 1) + ", 目标楼层为" + (target + 1));
                        System.out.println(">>因此向电梯" + (elevatorIndex + 1) +
                                "发送了" + requestNum + "号请求");
                    }
                    break;
                }
            }
        }
        return elevatorIndex != -1;
    }

    // 分配至最近的空闲 (target == -1) 电梯
    private boolean distributeClosestIdle(PersonRequest request) throws InterruptedException {
        int from = request.getFromFloor() - 1;
        int distance = 100;
        int elevatorIndex = -1;
        for (int i = 0; i < elevatorNum; i++) {
            synchronized (requestPool.get(i)) {
                int target = requestPool.get(i).get(floorNum).getRequests()
                        .get(3).get(0);
                if (target != -1) {
                    continue;
                }
                int pos = requestPool.get(i).get(floorNum).getRequests()
                        .get(2).get(0);
                if (Math.abs(pos - from) < distance) {
                    distance = Math.abs(pos - from);
                    elevatorIndex = i;
                }
            }
        }
        if (elevatorIndex != -1) {
            if (LOG) {
                System.out.println(">>Distributor: 空闲分配: 电梯" + (elevatorIndex + 1) +
                        "处于空闲状态");
                System.out.println(">>因此向电梯" + (elevatorIndex + 1) +
                        "发送了" + requestNum + "号请求");
            }
            distribute(request, elevatorIndex);
            return true;
        } else {
            return false;
        }
    }

    // 分配至未满 (!full) 电梯
    private boolean distributeClosest(PersonRequest request) throws InterruptedException {
        int elevatorIndex = -1;
        for (int i = 0; i < elevatorNum; i++) {
            synchronized (requestPool.get(i)) {
                boolean full = requestPool.get(i).get(floorNum).getRequests()
                        .get(1).get(0) == 1;
                if (!full) {
                    elevatorIndex = i;
                    if (LOG) {
                        System.out.println(">>Distributor: 未满分配: 电梯" + (elevatorIndex + 1) +
                                "未满");
                        System.out.println(">>因此向电梯" + (elevatorIndex + 1) +
                                "发送了" + requestNum + "号请求");
                    }
                    distribute(request, elevatorIndex);
                    break;
                }
            }
        }
        return elevatorIndex != -1;
    }

    // 打包轮流分配
    private void distributeRotate(PersonRequest request) throws InterruptedException {
        int elevatorIndex = rotator.next();
        if (LOG) {
            System.out.println(">>Distributor: 轮流分配-向电梯" + (elevatorIndex + 1) +
                    "发送了" + requestNum + "号请求");
        }
        distribute(request, elevatorIndex);
    }

    private void distribute(PersonRequest request, int elevatorIndex) {
        synchronized (requestPool.get(elevatorIndex)) {
            Entry entry = requestPool.get(elevatorIndex).get(request.getFromFloor() - 1);
            entry.addPassenger(request.getToFloor() - 1, request.getPersonId());
            requestPool.get(elevatorIndex).notifyAll();
        }
    }
}