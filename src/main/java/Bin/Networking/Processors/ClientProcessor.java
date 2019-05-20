package Bin.Networking.Processors;

import Bin.Audio.AudioClient;
import Bin.GUI.Main;
import Bin.Networking.DataParser.Package.BaseDataPackage;
import Bin.Networking.DataParser.Package.DataPackage;
import Bin.Networking.Startable;
import Bin.Networking.Writers.BaseWriter;
import Bin.Utility.ClientUser;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProcessor implements Processor, Startable {

    private final Queue<BaseDataPackage> taskStorage;
//    private final MainFrame mainFrame;
    private boolean work;

    public ClientProcessor() {
//        this.mainFrame = mainFrame;
        taskStorage = new ArrayDeque<>();
        work = true;
    }

    @Override
    public void process() {
        BaseDataPackage dataPackage = null;
        synchronized (taskStorage) {
            dataPackage = taskStorage.poll();
            if (dataPackage == null) {
                try {
                    taskStorage.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
             /*
             * wright here all possible instructions
             */
//        switch (dataPackage.getInstruction()) {
//            case BaseWriter.SEND_ID: {
//                Main.getInstance().setMe(dataPackage.getTo());
////                System.out.println("id is " + dataPackage.getTo());
//                break;
//            }
//            case BaseWriter.SEND_AUDIO_FORMAT: {
////                System.out.println("Client - " + Arrays.toString(dataPackage.getData()) + " Length = " + dataPackage.getFullLength() + "\n" + dataPackage.getDataAsString());
//                Main.getInstance().setAudioFormat(parseAudioFormat(dataPackage.getDataAsString()));
//                break;
//            }
//            case BaseWriter.SEND_USERS: {
////                System.out.println("Users are " + dataPackage.getDataAsString());
//                Main.getInstance().resetUsers(parseUsers(dataPackage.getDataAsString()));
//                break;
//            }
//            case BaseWriter.SEND_MESSAGE: {
//                Main.getInstance().getMainFrame().showMessage(dataPackage.getDataAsString(), dataPackage.getFrom());
//                break;
//            }
//            case BaseWriter.SEND_CALL: {
//                Main.getInstance().getMainFrame().showCallDialog(dataPackage.getFrom());
//                break;
//            }
//            case BaseWriter.SEND_DENY: {
//                Main.getInstance().getMainFrame().denyReceived();
//                break;
//            }
//            case BaseWriter.SEND_CANCEL: {
//                Main.getInstance().getMainFrame().cancelReceived();
//                break;
//            }
//            case BaseWriter.SEND_APPROVE: {
//                Main.getInstance().getMainFrame().acceptReceived(dataPackage.getFrom());
//                break;
//            }
//            case BaseWriter.SEND_SOUND: {
//                AudioClient.getInstance().playAudio(dataPackage.getFrom(), true, dataPackage.getData());
//                break;
//            }
//        }
//
//        DataPackage.returnObject(dataPackage);
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (work) {
                process();
            }
        }, "Client processor").start();
    }

    @Override
    public void close() {
        synchronized (taskStorage) {
            work = false;
            taskStorage.notifyAll();
        }
    }

    /*
    * uses by readers to add data in to a stack
    * byte[] data = pocket of data
     */

    public void push(BaseDataPackage data){
        synchronized (taskStorage) {
            taskStorage.offer(data);
            taskStorage.notifyAll();
        }
    }

//    public void wakeUp(){
//        synchronized (taskStorage){
//            taskStorage.notifyAll();
//        }
//    }

    private AudioFormat parseAudioFormat(String data){
//        System.out.println(data);
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

    private ClientUser[] parseUsers(String data){
        if (data.length() < 5) return new ClientUser[0];
        String[] split = data.split("\n");
        return Arrays.stream(split).map(String::trim).filter(s -> ClientUser.parser.matcher(s).matches()).map(ClientUser::parse).toArray(ClientUser[]::new);
    }

}
