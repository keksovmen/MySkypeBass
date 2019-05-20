package Bin.Networking.Writers;

import Bin.Networking.DataParser.Package.BaseDataPackage;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class BaseWriter {

    protected DataOutputStream outputStream;
    protected boolean work;
    /*
    * SEND_MESSAGE contain marker if 0 just message to a person if 1 message to a conference//
     */

    public static final int SEND_NAME = 1;
    public static final int SEND_ID = 2;
    public static final int SEND_AUDIO_FORMAT = 3;
    public static final int SEND_USERS = 4;
    public static final int SEND_MESSAGE = 5;
    public static final int SEND_CALL = 6;
    public static final int SEND_APPROVE = 7;
    public static final int SEND_DENY = 8;
    public static final int SEND_CANCEL = 9;
    public static final int SEND_SOUND = 10;
    public static final int SEND_DISCONNECT = 11;
    public static final int SEND_ADD = 12;
    public static final int SEND_REMOVE = 13;

    public static final int NO_NAME = 0;
    public static final int SERVER = 1;
    public static final int CONFERENCE = 2;

    public BaseWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        work = true;
    }

    private void writeBase(BaseDataPackage dataPackage) throws IOException {
        outputStream.writeInt(dataPackage.getLength());
//        System.out.println("data length = " + dataPackage.getLength());
        outputStream.writeInt(dataPackage.getFrom());
        outputStream.writeInt(dataPackage.getTo());
        outputStream.writeInt(dataPackage.getInstruction());
    }

    protected synchronized void write(BaseDataPackage dataPackage) throws IOException {
        writeBase(dataPackage);
        if (dataPackage.getData() != null)
            outputStream.write(dataPackage.getData());
        outputStream.flush();
        BaseDataPackage.returnObject(dataPackage);
    }

}
