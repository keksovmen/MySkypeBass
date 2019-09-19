package Com.Networking.Protocol;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Basic object pool
 * For increase performance only
 * And just wanted to try it
 * <p>
 * Doesn't have a method for clear its cache
 * Need to add and think when to call
 * Maybe on client when conversation is end
 * And on server when client is disconnected from conversation
 */

public class DataPackagePool extends AbstractDataPackagePool {

    /**
     * Where all instance are live
     */

    private final Queue<AbstractDataPackage> pool;

    private final int INITIAL_SIZE = 10;

    private final int MIN_CLEAR_SIZE = 10;

    /**
     * Private constructor
     */

    public DataPackagePool() {
        pool = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < INITIAL_SIZE; i++) {
            pool.offer(new BaseDataPackage());
        }
    }

    /**
     * Simple getter
     *
     * @return new or already existed package
     */

    @Override
    public AbstractDataPackage getPackageInst() {
        AbstractDataPackage poll = pool.poll();
        return poll == null ? new BaseDataPackage() : poll;
    }

    /**
     * Call when you are done with package
     *
     * @param dataPackage to try to rerun it home
     */

    @Override
    public void returnPackageInst(AbstractDataPackage dataPackage) {
        dataPackage.clear();
        pool.offer(dataPackage);
    }

    @Override
    void clearInst() {
        if (pool.size() > MIN_CLEAR_SIZE) {
            pool.clear();
        }
    }
}
