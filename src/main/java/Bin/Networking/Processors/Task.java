package Bin.Networking.Processors;

import Bin.Networking.DataParser.BaseDataPackage;

public interface Task {

    void doJob(BaseDataPackage dataPackage);
}
