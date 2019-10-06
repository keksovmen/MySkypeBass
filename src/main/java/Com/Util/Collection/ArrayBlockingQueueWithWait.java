package Com.Util.Collection;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueWithWait<Q> extends ArrayBlockingQueue<Q> {

    public ArrayBlockingQueueWithWait(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(Q q) {
        try {
            put(q);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
}
