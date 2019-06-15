package Bin.Networking.Protocol;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Basic object pool
 * For increase performance only
 * And just wanted to try it
 *
 * Doesn't have a method for clear its cache
 * Need to add and think when to call
 * Maybe on client when conversation is end
 * And on server when client is disconnected from conversation
 *
 */

public class DataPackagePool extends AbstractDataPackagePool {

    /**
     * Where all instance are live
     */

    private final Queue<AbstractDataPackage> pool;

    /**
     * Private constructor
     */

    public DataPackagePool() {
        pool = new ConcurrentLinkedDeque<>();
    }

    /**
     * Simple getter
     * @return new or already existed package
     */

    @Override
    public AbstractDataPackage getPackageInst(){
        AbstractDataPackage poll = pool.poll();
        return poll == null ? new BaseDataPackage() : poll;
    }

    /**
     * Call when you are done with package
     * @param dataPackage to try to rerun it home
     */

    @Override
    public void returnPackageInst(AbstractDataPackage dataPackage){
        dataPackage.clear();
        pool.offer(dataPackage);
    }

    @Override
    void clearInst() {
        if (pool.size() > 10){
            pool.clear();
        }
    }
}
