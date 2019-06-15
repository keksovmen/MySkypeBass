package Bin.Networking;

import Bin.Audio.AudioClient;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Processors.Processable;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Readers.BaseReader;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ClientWriter;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController implements ErrorHandler {

    private Socket socket;
    private ClientWriter writer;
    private BaseReader reader;
    private Processable processor;
    private BaseUser me;

    private ErrorHandler mainErrorHandler;

    /**
     * Uses only for holder of network stuff
     * and handle networking
     */

    public ClientController(ErrorHandler mainErrorHandler) {
        this.mainErrorHandler = mainErrorHandler;
        processor = new ClientProcessor();
    }

    /**
     * Try to establish a TCP connection
     *
     * @param hostName ip address
     * @param port     to connect
     * @param name     your nickname to others
     * @return true if audio format is supported false otherwise
     * @throws IOException if server doesn't exist
     */

    public boolean connect(final String name, final String hostName, final int port) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostName, port), 7_000);
            writer = new ClientWriter(socket.getOutputStream(), mainErrorHandler);
            reader = new BaseReader(socket.getInputStream(), processor, mainErrorHandler);
        } catch (IOException e) {
            e.printStackTrace();
//            mainErrorHandler.errorCase();
//            return null;
            socket.close();
            throw e;
        }
        return authenticate(name);
    }

    public boolean connect(final String name, final String hostName, final String port) throws IOException {
        return connect(name, hostName, Integer.parseInt(port));
    }

    /**
     * Trying to authenticate first writes your name
     * second read audio format and checks it if supported
     * third creates client user with unique id from the server
     * then starts a new thread for readings from socket
     *
     * @param name your nickname
     * @return true only if audio format accepted in mic and speakers false otherwise
     */

    private boolean authenticate(String name) {
        try {
            writer.writeName(name);
            AbstractDataPackage read = reader.read();
            AudioFormat audioFormat = parseAudioFormat(read.getDataAsString());
            //sets audio format and return true only if mic and speaker is set
            if (!AudioClient.getInstance().setAudioFormat(audioFormat)) {
                writer.writeDeny(BaseWriter.WHO.NO_NAME.getCode(), BaseWriter.WHO.SERVER.getCode());
                return false;
            }
            writer.writeAccept(BaseWriter.WHO.NO_NAME.getCode(), BaseWriter.WHO.SERVER.getCode());
            me = new BaseUser(name, read.getHeader().getTo());
            AbstractDataPackagePool.returnPackage(read);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        reader.start("Client reader");
        return true;
    }

    /**
     * Default action for disconnecting the user
     */

    public void disconnect() {
        reader.close();
        writer.writeDisconnect(me.getId());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Parse string like this Sample rate = 01...n\nSample size = 01....n
     * retrive from them digits
     *
     * @param data got from the server
     * @return default audio format
     */

    private AudioFormat parseAudioFormat(String data) {
        String[] strings = data.split("\n");
        Pattern pattern = Pattern.compile("\\d+?\\b");
        Matcher matcher = pattern.matcher(strings[0]);
        matcher.find();
        int sampleRate = Integer.valueOf(matcher.group());
        matcher = pattern.matcher(strings[1]);
        matcher.find();
        int sampleSize = Integer.valueOf(matcher.group());
        return new AudioFormat(sampleRate, sampleSize, 1, true, true);
    }


    public BaseUser getMe() {
        return me;
    }

    public ClientWriter getWriter() {
        return writer;
    }

    public Processable getProcessor() {
        return processor;
    }

    @Override
    public void errorCase() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            iterate();
        }
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{reader};
    }
}
