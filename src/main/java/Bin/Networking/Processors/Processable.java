package Bin.Networking.Processors;

import Bin.Networking.Protocol.AbstractDataPackage;

import java.util.function.Consumer;

public interface Processable {

    void process(AbstractDataPackage dataPackage);

    void addListener(Consumer<AbstractDataPackage> listener);

    void removeListener(Consumer<AbstractDataPackage> listener);

    void clear();
}
