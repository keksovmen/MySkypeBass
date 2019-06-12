package Bin.Networking.Processors;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ClientProcessor {

    private Executor executor;
    private List<Consumer<BaseDataPackage>> listeners;

    public ClientProcessor() {
        listeners = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
    }

    public void doJob(final BaseDataPackage dataPackage) {
        if (dataPackage == null){
            return;
        }
        executor.execute(() -> {
            listeners.forEach(baseDataPackageConsumer -> baseDataPackageConsumer.accept(dataPackage));
            DataPackagePool.returnPackage(dataPackage);
        });
    }

    public void addTaskListener(Consumer<BaseDataPackage> consumer){
        listeners.add(consumer);
    }

    public void removeTaskListener(Consumer<BaseDataPackage> consumer){
        listeners.remove(consumer);
    }


}
