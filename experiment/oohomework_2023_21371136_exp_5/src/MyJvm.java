import java.util.List;

public class MyJvm {
    private static final int DEFAULT_CAPACITY = 16;
    private final JvmHeap heap;

    MyJvm() {
        heap = new JvmHeap(DEFAULT_CAPACITY);
    }

    // [4] 依据提示，完整书写以下方法的JML
    /*@ public normal_behavior
      @ assignable heap;
      @ requires \old(heap.getSize()) + count < DEFAULT_CAPACITY;
      @ ensures heap.getSize() == \old(heap.getSize()) + count;
      @ ensures (forall int i; 1 <= i && i <= \old(heap.getSize());
                    (\exists int j; 1 <= j && j <= heap.getSize(); heap.getElement(j).equals(\old(heap.getElement(i)))));
      @ also
      @ requires \old(heap.getSize()) + count >= DEFAULT_CAPACITY;
      @ ensures heap.getSize() < DEFAULT_CAPACITY;
      @ ensures (forall int i; 1 <= i && i <= \old(heap.getSize()); \old(heap.getElement(i).isReferenced) =>
            (\exists int j; 1 <= j && j <= heap.getSize(); heap.getElement(j).equals(\old(heap.getElement(i)))));
      @ ensures (forall int i; 1 <= i && i <= \old(heap.getSize()); !\old(heap.getElement(i).isReferenced) =>
            (\forall int j; 1 <= j && j <= heap.getSize(); !heap.getElement(j).equals(\old(heap.getElement(i)))));
      @*/
    public void createObject(int count) {
        for (int i = 0; i < count; i++) {
            MyObject newObject = new MyObject();
            heap.add(newObject);
            if (heap.getSize() == DEFAULT_CAPACITY) {
                System.out.println("Heap reaches its capacity,triggered Garbage Collection.");
                GC();
            }
        }
    }

    public void setUnreferenced(List<Integer> objectId) {
        heap.setUnreferencedId(objectId);
    }

    public void GC() {
        heap.removeUnreferenced();
    }

    public void getSnapShot() {
        System.out.println("Heap: " + heap.getSize());
        for (int i = 1; i <= heap.getSize(); i++) {
            MyObject mo = (MyObject) heap.getElementData()[i];
            System.out.print(mo.getId() + " ");
        }
        System.out.println("");
        MyObject youngest = heap.getYoungestOne();
        if (youngest != null) {
            System.out.print("the youngest one's id is " + youngest.getId());
        }
        System.out.println("");
        System.out.println("\n---------------------------------");
    }
}
