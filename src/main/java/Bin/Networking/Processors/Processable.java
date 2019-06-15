package Bin.Networking.Processors;

import Bin.Networking.Protocol.AbstractDataPackage;

import java.util.function.Consumer;

public interface Processable {

    void process(AbstractDataPackage dataPackage);

    <T extends Consumer<AbstractDataPackage>> void addListener(T listener);

    <T extends Consumer<AbstractDataPackage>> void removeListener(T listener);

    void clear();
}
