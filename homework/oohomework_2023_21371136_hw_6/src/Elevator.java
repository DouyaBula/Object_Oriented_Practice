import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class Elevator extends Thread {
    private static final boolean LOG = false; // 调试信息输出开关
    private final ArrayList<Entry> inRequests;
    private final ArrayList<ArrayList<Integer>> outRequests = new ArrayList<>();
    private int elevatorID;
    private Parameter parameter;
    private int floorNum;
    private int pos;
    private int target = -1;
    private int capacity;
    private int innerCnt = 0;
    private boolean opened = false;
    private boolean exit = false;
    private boolean maintain = false;

    public Elevator(int id, int floorNum, Parameter parameter, ArrayList<Entry> inRequests) {
        this.elevatorID = id;
        this.parameter = parameter;
        this.floorNum = floorNum;
        this.pos = parameter.getInitialPos();
        this.capacity = parameter.getCapacity();
        this.inRequests = inRequests;
        for (int i = 0; i < floorNum; i++) {
            outRequests.add(i, new ArrayList<>());
        }
        inRequests.get(floorNum).getRequests().get(1).add(0, 0);
        inRequests.get(floorNum).getRequests().get(2).add(0, pos);
        inRequests.get(floorNum).getRequests().get(3).add(0, target);
    }

    private void up() throws InterruptedException {
        if (opened) {
            close();
        }
        sleep(parameter.getMoveTime());
        pos++;
        TimableOutput.println("ARRIVE-" + (pos + 1) + "-" + elevatorID);
    }

    private void down() throws InterruptedException {
        if (opened) {
            close();
        }
        sleep(parameter.getMoveTime());
        pos--;
        TimableOutput.println("ARRIVE-" + (pos + 1) + "-" + elevatorID);
    }

    private void open() throws InterruptedException {
        TimableOutput.println("OPEN-" + (pos + 1) + "-" + elevatorID);
        opened = true;
        sleep(parameter.getSwitchTime());
    }

    private void close() throws InterruptedException {
        sleep(parameter.getSwitchTime());
        TimableOutput.println("CLOSE-" + (pos + 1) + "-" + elevatorID);
        opened = false;
    }

    private void check() throws InterruptedException {
        synchronized (inRequests) {
            Entry currentEntry = inRequests.get(pos);
            // 检查是否存在维护请求4-*
            if (!inRequests.get(floorNum).getRequests()
                    .get(4).isEmpty()) {
                maintain();
                return;
            }
            // 若寻找主请求路径中仍没有捎带客人, 则主请求会作为第一个请求加入
            if (innerCnt == 0 && pos == target) {
                if (fill(currentEntry)) {   // 若装载成功
                    return;
                }
            }
            // 出
            if (outRequests.get(pos).size() != 0) {
                out();
            }
            // 捎带
            if (currentEntry.getNum() != 0) {
                if (innerCnt == capacity) { // 若已满员
                    return;
                }
                if (pick(currentEntry)) {   // 若加入新人时满员
                    return;
                }
            }
        }
    }

    private boolean in(Entry currentEntry, int i, Integer passengerID) throws InterruptedException {
        if (!opened) {
            open();
        }
        TimableOutput.println(
                "IN-" + passengerID + "-" + (pos + 1) + "-" + elevatorID);
        currentEntry.removePassenger(i, passengerID);
        outRequests.get(i).add(passengerID);
        innerCnt++;
        if (innerCnt > capacity) {
            System.out.println("!寄");
        }
        if (innerCnt == capacity) {
            inRequests.get(floorNum).getRequests().get(1).set(0, 1);
            return true;
        }
        return false;
    }

    private boolean fill(Entry currentEntry) throws InterruptedException {
        if (currentEntry.getNum() != 0) {
            ArrayList<ArrayList<Integer>> requests = currentEntry.getRequests();
            ArrayList<Integer> passengers;
            int mark = -1;
            int distance = -1;
            for (int i = 0; i < floorNum; i++) {
                // 加入主请求楼层里, 目标楼层最远的一个或一组相同请求
                passengers = requests.get(i);
                if (!passengers.isEmpty()) {
                    if (Math.abs(i - pos) > distance) {
                        distance = Math.abs(i - pos);
                        mark = i;
                    }
                }
            }
            target = mark;
            // 捎带与主请求同向的请求(远者优先)
            boolean rising = target > pos;
            for (int i = target;
                 rising ? i >= pos + 1 : i <= pos - 1;
                 i += rising ? -1 : 1) {
                passengers = requests.get(i);
                while (!passengers.isEmpty()) {
                    Integer passengerID = passengers.get(0);
                    if (in(currentEntry, i, passengerID)) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean pick(Entry currentEntry) throws InterruptedException {
        ArrayList<ArrayList<Integer>> requests = currentEntry.getRequests();
        ArrayList<Integer> passengers;
        if (target == -1) {  // 未装填主请求时不捎带
            return false;
        }
        // 捎带同向请求(远者优先)
        boolean rising = (target - pos) > 0;
        for (int i = (rising ? floorNum - 1 : 0);
             rising ? i >= pos + 1 : i <= pos - 1;
             i += (rising ? -1 : 1)) {
            passengers = requests.get(i);
            while (!passengers.isEmpty()) {
                Integer passengerID = passengers.get(0);
                if (rising) {
                    target = Math.max(i, target);
                } else {
                    target = Math.min(i, target);
                }
                if (LOG) {
                    System.out.println("电梯" + elevatorID + " target设为" + (target + 1));
                }
                if (in(currentEntry, i, passengerID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void out() throws InterruptedException {
        if (!opened) {
            open();
        }
        while (!outRequests.get(pos).isEmpty()) {
            Integer passengerID = outRequests.get(pos).get(0);
            TimableOutput.println("OUT-" + passengerID + "-" + (pos + 1) + "-" + elevatorID);
            outRequests.get(pos).remove(0);
            innerCnt--;
            inRequests.get(floorNum).getRequests().get(1).set(0, 0);
        }
        if (pos == target) {
            if (innerCnt != 0) {
                System.out.println("计数出错!");
            }
            target = -1;
        }
    }

    private void sharePos() {
        synchronized (inRequests) {
            inRequests.get(floorNum).getRequests().get(2).set(0, pos);
        }
    }

    private void update() throws InterruptedException {
        boolean jumpLoop = false;
        synchronized (inRequests) {
            while (target == -1) {
                int distance = -1;
                for (int i = 0; i < floorNum; i++) {
                    if (inRequests.get(i).getNum() != 0) {
                        // 寻找最远请求作为target
                        if (Math.abs(i - pos) > distance) {
                            target = i;
                            distance = Math.abs(i - pos);
                            jumpLoop = true;
                        }
                    }
                }
                if (jumpLoop) {
                    if (LOG) {
                        System.out.println(
                                ">> update: " + "电梯" + elevatorID + "主请求楼层设置为" + (target + 1));
                    }
                    break;
                } else if (!inRequests.get(floorNum).getRequests()
                        .get(0).isEmpty()) {    // 检查是否存在退出请求0-*
                    exit = true;
                    return;
                } else if (!inRequests.get(floorNum).getRequests()
                        .get(4).isEmpty()) {    // 检查是否存在维护请求4-*
                    maintain();
                    return;
                }
                if (LOG) {
                    System.out.println(">> update: " + "电梯" + elevatorID + "睡眠...");
                }
                inRequests.wait();
                if (LOG) {
                    System.out.println(">> update: " + "电梯" + elevatorID + "重启");
                }
            }
        }
    }

    private void maintain() throws InterruptedException {
        maintain = true;
        // 该滚的滚
        if (outRequests.get(pos).size() != 0) {
            out();
        }
        // 不该滚的也滚
        synchronized (inRequests) {
            for (int i = 0; i < floorNum; i++) {
                ArrayList<Integer> outList = outRequests.get(i);
                for (Integer passengerID :
                        outList) {
                    if (!opened) {
                        open();
                    }
                    TimableOutput.println(
                            "OUT-" + passengerID + "-" + (pos + 1) + "-" + elevatorID);
                    inRequests.get(pos).addPassenger(i, passengerID);
                }
            }
            inRequests.notifyAll();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                update();
                if (isDisturbed()) {
                    return;
                }
                check();
                if (isDisturbed()) {
                    return;
                }
                while (pos != target && target != -1) {
                    if (pos < target) {
                        up();
                    }
                    if (pos > target) {
                        down();
                    }
                    sharePos();
                    check();
                    if (isDisturbed()) {
                        return;
                    }
                }
                if (innerCnt != 0) {
                    System.out.println("😅!");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isDisturbed() throws InterruptedException {
        if (exit || maintain) {
            if (opened) {
                close();
            }
            if (maintain) {
                TimableOutput.println("MAINTAIN_ABLE-" + elevatorID);
            }
            if (LOG) {
                System.out.println(">> 电梯" + elevatorID + "关闭");
            }
            return true;
        }
        return false;
    }

}
