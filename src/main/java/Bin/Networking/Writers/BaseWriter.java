package Bin.Networking.Writers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;

public abstract class BaseWriter {

    protected DataOutputStream outputStream;
    protected boolean work;
    /*
     * SEND_MESSAGE contain marker if 0 just message to a person if 1 message to a conference//
     */

    public enum CODE {
        SEND_NAME(1),
        SEND_ID(2),
        SEND_AUDIO_FORMAT(3),
        SEND_USERS(4),
        SEND_MESSAGE(5),
        SEND_CALL(6),
        SEND_APPROVE(7),
        SEND_DENY(8),
        SEND_CANCEL(9),
        SEND_SOUND(10),
        SEND_DISCONNECT(11),
        SEND_ADD(12),
        SEND_REMOVE(13);

        private int code;

        CODE(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static CODE parse(int code) {
            Optional<CODE> first = Arrays.stream(CODE.values()).filter(code1 -> code1.getCode() == code).findFirst();
            return first.orElse(null);
        }
    }

    public enum WHO {
        NO_NAME(0),
        SERVER(1),
        CONFERENCE(2);

        private int code;

        WHO(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public BaseWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        work = true;
    }

//    private void writeBase(BaseDataPackage dataPackage) throws IOException {
//        outputStream.write(dataPackage.getHeader().getRawHeader());// think about cashe header
//    }


    //Check machine code and compare to synchronise in head
    protected synchronized void write(BaseDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());// think about cashe header
        if (dataPackage.getHeader().getLength() != 0)
            outputStream.write(dataPackage.getData());
        outputStream.flush();
//        System.out.println(dataPackage + " " + Thread.currentThread().getName());
        DataPackagePool.returnPackage(dataPackage);
    }

}
