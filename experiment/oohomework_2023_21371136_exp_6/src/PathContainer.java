import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PathContainer {
    //@ public instance model non_null Path[] pList;
    //@ public instance model non_null int[] pidList;
    private HashMap<Path, Integer> pathList;        // 描述id到path之间的映射关系
    private HashMap<Integer, Path> pathIdList;      // 描述path到id之间的映射关系，两个map加在一起对应上面两个数组

    private int idCounter;

    //@ public instance model non_null int[] nodes;
    //@ public instance model non_null int[] nodeToCount;
    private HashMap<Integer, Integer> globalNodesCount;     // 用一个HashMap实现规格中的nodes,
    // nodeToCount两个数组

    public PathContainer() {
        pathList = new HashMap<>();
        pathIdList = new HashMap<>();
        globalNodesCount = new HashMap<>();
        idCounter = 0;
    }

    //@ ensures \result == pList.length;
    public /*@ pure @*/ int size() {
        return pathList.size();
    }

    /*@ requires path != null;
      @ assignable \nothing;
      @ ensures \result == (\exists int i; 0 <= i && i < pList.length;
      @                     pList[i].equals(path));
      @*/
    public /*@ pure @*/ boolean containsPath(Path path) {
        if (path == null) {
            System.err.println("path in containsPath(path) is null !");
            return false;
        }
        return (pathList.get(path) != null);
    }

    /*@ ensures \result == (\exists int i; 0 <= i && i < pidList.length;
      @                      pidList[i] == pathId);
      @*/
    public /*@ pure @*/ boolean containsPathId(int pathId) {
        return (pathIdList.get(pathId) != null);
    }

    /*@ public normal_behavior
      @ requires containsPathId(pathId);
      @ assignable \nothing;
      @ ensures (\exists int i; 0 <= i && i < pList.length; pidList[i] == pathId && \result ==
      pList[i]);
      @ also
      @ public exceptional_behavior
      @ requires !containsPathId(pathId);
      @ assignable \nothing;
      @ signals_only PathIdNotFoundException;
      @*/
    public /*@ pure @*/ Path getPathById(int pathId) throws PathIdNotFoundException {
        if (containsPathId(pathId)) {
            return pathIdList.get(pathId);
        }
        throw new PathIdNotFoundException(pathId);
    }

    /*@ public normal_behavior
      @ requires path != null && path.isValid() && containsPath(path);
      @ assignable \nothing;
      @ ensures (\exists int i; 0 <= i && i < pList.length; pList[i].equals(path) && pidList[i]
      == \result);
      @ also
      @ public exceptional_behavior
      @ requires path == null || !path.isValid() || !containsPath(path);
      @ signals_only PathNotFoundException;
      @*/
    public /*@ pure @*/ int getPathId(Path path) throws PathNotFoundException {
        if (path != null && path.isValid() && containsPath(path)) {
            return pathList.get(path);
        } else {
            throw new PathNotFoundException(path);
        }
    }

    //@ ensures \result == (\exists int i; 0 <= i < nodes.length; nodes[i] == node);
    public /*@ pure @*/ boolean containsNode(int node) {
        return globalNodesCount.containsKey(node);
    }

    /*@ normal_behavior
      @ requires containsNode(node);
      @ ensures (\exists int i; 0 <= i < nodes.length; nodes[i] == node && \result ==
      nodeToCount[i]);
      @ also
      @ normal_behavior
      @ requires !containsNode(node);
      @ ensures \result == 0;
      @*/
    public /*@ pure @*/ int getNodeCount(int node) {
        return globalNodesCount.getOrDefault(node, 0);
    }

    /*@ normal_behavior
      @ requires path != null && path.isValid() && !containsPath(path);
      @ assignable pList, pidList, nodes, nodeToCount;
      @ ensures \result == \old(pList.length);
      @ ensures (\exists int i; 0 <= i && i < pList.length; pList[i].equals(path) &&
      @           \old(pList.length) == pidList[i]);
      @ ensures  pList.length == (\old(pList.length) + 1) &&
      @          pidList.length == (\old(pidList.length) + 1);
      @ ensures (\forall int i; 0 <= i && i < \old(pList.length);
      @           (\exists int j; 0 <= j && j < pList.length;
      @             \old(pList[i]).equals(pList[j]) && \old(pidList[i]) == pidList[j]));
      @ ensures (\forall int i; path.containsNode(i) || \old(this.containsNode(i));
      @          this.getNodeCount(i) == \old(this.getNodeCount(i)) + path.getNodeCount(i));
      @ also
      @ normal_behavior
      @ requires path == null || path.isValid() == false || containsPath(path);
      @ assignable \nothing;
      @ ensures \result == 0;
      @*/
    public int addPath(Path path) {
        if (path != null && path.isValid() && !containsPath(path)) {
            pathList.put(path, idCounter);
            pathIdList.put(idCounter, path);
            for (Integer node : path) {
                Integer prev = globalNodesCount.get(node);
                if (prev == null) {
                    globalNodesCount.put(node, 1);
                } else {
                    globalNodesCount.put(node, prev + 1);
                }
            }
            return idCounter++;
        }
        return 0;
    }

    /*@ public normal_behavior
      @ requires containsPathId(pathId);
      @ assignable pList, pidList, nodes, nodeToCount;
      @ ensures pList.length == (\old(pList.length) - 1) &&
      @          pidList.length == (\old(pidList.length) - 1);
      @ ensures (\forall int i; 0 <= i && i < \old(pList.length) && \old(pidList[i]) != pathId;
      @          (\exists int j; 0 <= j && j < pList.length;
      @             \old(pList[i]).equals(pList[j]) && pidList[i] == pidList[j]));
      @ ensures (\forall int i; 0 <= i && i < pidList.length; pidList[i] != pathId);
      @ ensures (\forall int i; 0 <= i && i < pList.length; !pList[i].equals(\old(getPathById
      (pathId))));
      @ ensures (\forall int i; \old(getPathById(pathId).containsNode(i)); this.getNodeCount(i) ==
      @             \old(this.getNodeCount(i)) - \old(getPathById(pathId).getNodeCount(i)));
      @ also
      @ public exceptional_behavior
      @ assignable \nothing;
      @ signals (PathIdNotFoundException e) !containsPathId(pathId);
      @*/
    public void removePathById(int pathId) throws PathIdNotFoundException {
        if (containsPathId(pathId)) {
            Path path = pathIdList.remove(pathId);
            pathList.remove(path);
            for (Integer node : path) {
                Integer prev = globalNodesCount.get(node);
                globalNodesCount.put(node, prev - 1);
            }
        } else {
            throw new PathIdNotFoundException(pathId);
        }
    }


    /*@
      @ TODO 1: 编写规格
      @ public normal_behavior
      @ requires !(\exist int i; 0 <= i && i < \old(pList.length);
        (\exists int j; i < j && j < \old(pList.length);
            pList[i].isUglyPath && pList[j].isUglyPath && pList[i].equals(pList[j])));
      @ assignable pList, pidList, nodes, nodeToCount;
      @ ensures pList.length == \old(pList.length) &&
      @          pidList.length == \old(pidList.length);
      @ ensures (\forall int i; 0 <= i && i < \old(pList.length) && !\old(pidList[i].isUglyPath);
      @          (\exists int j; 0 <= j && j < pList.length;
      @             \old(pList[i]).equals(pList[j]) && pidList[i] == pidList[j]));
      @ ensures (\forall int i; 0 <= i && i < \old(pList.length) && \old(pidList[i].isUglyPath);
      @          !(\exists int j; 0 <= j && j < pList.length;
      @             \old(pList[i]).equals(pList[j]) && pidList[i] == pidList[j]));
      @ ensures (\forall int i; 0 <= i && i < \old(pList.length) && \old(pidList[i].isUglyPath);
      @          (\exists int j; 0 <= j && j < pList.length;
      @             \old(pList[i].extractLoopPath).equals(pList[j])));
      @ ensures (\forall int i; 0 <= i && i < pidList.length; !pidList[i].isUglyPath);
      @ ensures (\forall int i; this.containsNode[i];
        this.getNodeCount(i) == \old(this.getNodeCount(i)) -
            (\sum int j; \old(pList[j].isUglyList && pList[j].containsNode[i]); \old(pList[j]
            .getNodeCount(i)))
                + (\sum int k; \old(pList[k].isUglyList && pList[k].extractLoopPath
                .containsNode[i]);
                    \old(pList[k].extractLoopPath.getNodeCount(i))));
      @ also
      @ public exceptional_behavior
      @ assignable \nothing;
      @ signals (LoopDuplicateException e) (\exist int i; 0 <= i && i < \old(pList.length);
        (\exists int j; i < j && j < \old(pList.length);
            pList[i].isUglyPath && pList[j].isUglyPath && pList[i].equals(pList[j])));
      @*/
    public boolean beautifyPathsOk(
            HashMap<Path, Integer> beforePathList, HashMap<Path, Integer> afterPathList,
            HashMap<Integer, Path> beforePathIdList, HashMap<Integer, Path> afterPathIdList,
            HashMap<Integer, Integer> beforeNodeCount, HashMap<Integer, Integer> afterNodeCount) {

        ArrayList<Path> pList = new ArrayList<>();              // 对应 pList
        ArrayList<Path> oldPList = new ArrayList<>();           // 对应 \old(pList)
        ArrayList<Integer> pidList = new ArrayList<>();         // 对应 pidList
        ArrayList<Integer> oldPidList = new ArrayList<>();      // 对应 \old(pidList)
        ArrayList<Integer> nodes = new ArrayList<>();           // 对应 nodes
        ArrayList<Integer> oldNodes = new ArrayList<>();        // 对应 \old(nodes)
        ArrayList<Integer> nodeToCount = new ArrayList<>();     // 对应 nodeToCount
        ArrayList<Integer> oldNodeToCount = new ArrayList<>();  // 对应 \old(nodeToCount)

        // 构造 oldPList 和 oldPidList
        for (Map.Entry<Path, Integer> e : beforePathList.entrySet()) {
            if (!beforePathIdList.get(e.getValue()).equals(e.getKey())) {
                return false;
            }
            oldPList.add(e.getKey());
            oldPidList.add(e.getValue());
        }

        // 构造 pList 和 pIdList
        for (Map.Entry<Path, Integer> e : afterPathList.entrySet()) {
            if (!afterPathIdList.get(e.getValue()).equals(e.getKey())) {
                return false;
            }
            pList.add(e.getKey());
            pidList.add(e.getValue());
        }

        // 构造 nodes 和 oldNodes
        nodes.addAll(afterNodeCount.keySet());
        oldNodes.addAll(beforeNodeCount.keySet());

        // 构造 nodeToCount 和 oldNodeToCount
        nodeToCount.addAll(afterNodeCount.values());
        oldNodeToCount.addAll(beforeNodeCount.values());

        // TODO 2: 根据规格编写 ok 方法
        for (int i = 0; i < pList.size(); i++) {
            if (oldPList.get(i).isUglyPath()) {
                for (int j = i + 1; j < oldPList.size(); j++) {
                    if (oldPList.get(j).isUglyPath()) {
                        if (oldPList.get(i).extractLoopPath().
                                equals(oldPList.get(j).extractLoopPath())) {
                            return pList.equals(oldPList)
                                    && pidList.equals(oldPidList)
                                    && nodes.equals(oldNodes)
                                    && nodeToCount.equals(oldNodeToCount);
                        }
                    }
                }
            }
        }
        if (pList.size() != oldPList.size()) {
            return false;
        }
        if (pidList.size() != oldPidList.size()) {
            return false;
        }
        for (Path path : oldPList) {
            if (path.isUglyPath()) {
                if (pList.contains(path)) {
                    return false;
                }
                if (!pList.contains(path.extractLoopPath())) {
                    return false;
                }
            } else {
                if (!pList.contains(path)) {
                    return false;
                }
            }
        }
        for (Path path : pList) {
            if (path.isUglyPath()) {
                return false;
            }
        }
        for (Integer node :
                nodes) {
            int sum1 = 0;
            int sum2 = 0;
            for (int i = 0; i < oldPList.size(); i++) {
                if (!oldPList.get(i).isUglyPath()) {
                    continue;
                }
                if (oldPList.get(i).containsNode(node)) {
                    sum1 += oldPList.get(i).getNodeCount(node);
                }
            }
            for (int i = 0; i < oldPList.size(); i++) {
                if (!oldPList.get(i).isUglyPath()) {
                    continue;
                }
                if (oldPList.get(i).extractLoopPath().containsNode(node)) {
                    sum1 += oldPList.get(i).extractLoopPath().getNodeCount(node);
                }
            }
            if (nodeToCount.get(node) !=
                    oldNodeToCount.get(node) - sum1 + sum2
            ) {
                return false;
            }
        }
        return true;
    }

    public void beautifyPaths() throws LoopDuplicateException {
        // TODO 3: 编写业务代码
        ArrayList<Path> paths = new ArrayList<>(pathList.keySet());
        ArrayList<Path> delPaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Path path1 = paths.get(i);
            if (path1.isUglyPath()) {
                Path path1butf = path1.extractLoopPath();
                for (int j = i + 1; j < paths.size(); j++) {
                    Path path2 = paths.get(j);
                    if (path2.isUglyPath()) {
                        Path path2butf = path2.extractLoopPath();
                        if (path1butf.equals(path2butf)) {
                            throw new LoopDuplicateException();
                        }
                    }
                }
                delPaths.add(path1);
            }
        }
        for (Path delPath :
                delPaths) {
            int id = pathList.get(delPath);
            try {
                removePathById(id);
            } catch (PathIdNotFoundException ignored) {
            }
            addPath(delPath.extractLoopPath());
        }
    }


    /*@ public normal_behavior
      @ assignable \nothing;
      @ ensures (\exists int[] arr;
      @            (\forall int i, j; 0 <= i && i < j && j < arr.length; arr[i] != arr[j]);
      @            (\forall int i; 0 <= i && i < arr.length;
      @                 (\exists Path p; this.containsPath(p); p.containsNode(arr[i]))) &&
      @            (\forall Path p; this.containsPath(p);
      @                 (\forall int node; p.containsNode(node);
      @                     (\exists int i; 0 <= i && i < arr.length; node == arr[i]))) &&
      @            (\result == arr.length));
      @*/
    public /*@ pure @*/ int getDistinctNodeCount() {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : globalNodesCount.entrySet()) {
            if (entry.getValue() > 0) {
                count++;
            }
        }
        return count;
    }
}
