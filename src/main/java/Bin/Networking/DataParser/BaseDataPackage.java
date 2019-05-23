package Bin.Networking.DataParser;

import Bin.Networking.Writers.BaseWriter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BaseDataPackage {

    /*
     * Based on object pool
     */
    private static final Charset charset = StandardCharsets.UTF_16;

    private DataPackageHeader header;
    private byte[] data;

    public BaseDataPackage() {
        header = new DataPackageHeader();
        data = new byte[0];
    }


    public void init(DataPackageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public void init(DataPackageHeader header, String data) {
        init(header, data.getBytes(charset));
    }

    public BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to) {
        header.init(code, 0, from, to);
        return this;
    }

    public BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to, @Nullable final byte[] data) {
        header.init(code, data == null ? 0 : data.length, from, to);
        this.data = data == null ? new byte[0] : data;
        return this;
    }

    public BaseDataPackage init(final BaseWriter.CODE code, final int from, final int to, @NotNull final String data) {
        return init(code, from, to, data.getBytes(charset));
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public DataPackageHeader getHeader() {
        return header;
    }

    public byte[] getData() {
//        System.out.println(Arrays.toString(this.data));
        return data;
    }

    public String getDataAsString() {
        return new String(data, charset);
    }

    void clear() {
        data = new byte[0];
    }

    @Override
    public String toString() {
        return "BaseDataPackage{" +
                "header=" + header +
                ", data=" + Arrays.toString(data) +
                ", dataAsString=" + getDataAsString() + '}';
    }
}
