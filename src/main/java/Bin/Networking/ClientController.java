package Bin.Networking;

import Bin.Audio.AudioClient;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Readers.ClientReader;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ClientWriter;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ClientUser;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController {

    private Socket socket;
    private ClientWriter writer;
    private ClientReader reader;
    private ClientProcessor processor;
    private BaseUser me;

    public ClientController() {

    }

    public boolean connect(String hostName, int port, String name) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostName, port), 5_000);
            writer = new ClientWriter(socket.getOutputStream());
            processor = new ClientProcessor();
            reader = new ClientReader(socket.getInputStream(), processor);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return authenticate(name);
    }

    private boolean authenticate(String name){
        try {
            writer.writeName(name);
            BaseDataPackage read = reader.read();
            AudioFormat audioFormat = parseAudioFormat(read.getDataAsString());
            DataPackagePool.returnPackage(read);
            //stopped here need to verify is able to use audio format or not
            if (!AudioClient.isFormatSupported(audioFormat)) {
                writer.writeDeny(BaseWriter.WHO.NO_NAME.getCode(), BaseWriter.WHO.SERVER.getCode());
                return false;
            }
            writer.writeAccept(BaseWriter.WHO.NO_NAME.getCode(), BaseWriter.WHO.SERVER.getCode());
            read = reader.read();
            me = new BaseUser(name, read.getHeader().getTo());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        reader.start();
        return true;
    }

    public void disconnect(){
        try {
            writer.writeDisconnect(me.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private ClientUser[] parseUsers(String data) {
        if (data.length() < 5) return new ClientUser[0];
        String[] split = data.split("\n");
        return Arrays.stream(split).map(String::trim).filter(s -> ClientUser.parser.matcher(s).matches()).map(ClientUser::parse).toArray(ClientUser[]::new);
    }
}
