package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;

import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.Key;

public class ServerCipherWriter extends CipherWriter {


    public ServerCipherWriter(OutputStream outputStream, int bufferSize, Key key, AlgorithmParameters parameters) {
        super(outputStream, bufferSize, key, parameters);
    }

    @Override
    protected boolean checkPackageForEncoding(AbstractDataPackage dataPackage) {
        return super.checkPackageForEncoding(dataPackage) && (
                dataPackage.getHeader().getFrom() == WHO.SERVER.getCode() ||
                dataPackage.getHeader().getCode().equals(CODE.SEND_CALL) ||
                dataPackage.getHeader().getCode().equals(CODE.SEND_ACCEPT_CALL));
    }
}
