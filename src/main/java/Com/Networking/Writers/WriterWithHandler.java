package Com.Networking.Writers;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Utility.ErrorHandler;

import java.io.IOException;
import java.io.OutputStream;

public abstract class WriterWithHandler extends BaseWriter {

    /**
     * Basically handles cases when network get ruined
     */

    final ErrorHandler mainErrorHandler;

    /**
     * Can use bot write() and writeA() methods
     *
     * @param outputStream     where to write
     * @param mainErrorHandler handler in case of error not null
     */

    public WriterWithHandler(OutputStream outputStream, ErrorHandler mainErrorHandler) {
        super(outputStream, 8192);
        this.mainErrorHandler = mainErrorHandler;
    }

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     */

    synchronized void writeWithHandler(AbstractDataPackage dataPackage) {
        try {
            outputStream.write(dataPackage.getHeader().getRawHeader());// cashed in other implementation @see serverWriter
            if (dataPackage.getHeader().getLength() != 0) {
                outputStream.write(dataPackage.getData());
            }
            outputStream.flush();
            AbstractDataPackagePool.returnPackage(dataPackage);
        } catch (IOException e) {
            e.printStackTrace();
            mainErrorHandler.errorCase();
        }
    }
}
