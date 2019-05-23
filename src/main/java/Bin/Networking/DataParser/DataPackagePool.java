package Bin.Networking.DataParser;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DataPackagePool {

    private static final Queue<BaseDataPackage> pool = new ConcurrentLinkedDeque<>();

    private DataPackagePool() {
    }

    public static BaseDataPackage getPackage(){
        BaseDataPackage poll = pool.poll();
        return poll == null ? new BaseDataPackage() : poll;
    }

    public static void returnPackage(BaseDataPackage dataPackage){
        dataPackage.clear();
        pool.offer(dataPackage);
    }
}
