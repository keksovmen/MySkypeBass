package Bin.Networking.Processors;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientProcessor /*implements Processor, Startable*/ {

    private Executor executor;
    private List<Task> listeners;

    public ClientProcessor() {
        listeners = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
    }

    public void doJob(final BaseDataPackage dataPackage) {
        executor.execute(() -> {
            listeners.forEach(task -> task.doJob(dataPackage));
            DataPackagePool.returnPackage(dataPackage);
        });
    }

    public void addTaskListener(Task task){
        listeners.add(task);
    }

    public void removeTaskListener(Task task){
        listeners.remove(task);
    }

//    @Override
//    public void process() {
//        BaseDataPackage dataPackage = null;
//        synchronized (taskStorage) {
//            dataPackage = taskStorage.poll();
//            if (dataPackage == null) {
//                try {
//                    taskStorage.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return;
//            }
//        }
//
//    }
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
//    }

//    @Override
//    public void start() {
//        new Thread(() -> {
//            while (work) {
//                process();
//            }
//        }, "Client processor").start();
//    }
//
//    @Override
//    public void close() {
//        synchronized (taskStorage) {
//            work = false;
//            taskStorage.notifyAll();
//        }
//    }

    /*
     * uses by readers to add data in to a stack
     * byte[] data = pocket of data
     */

//    public void push(BaseDataPackage data){
//        synchronized (taskStorage) {
//            taskStorage.offer(data);
//            taskStorage.notifyAll();
//        }
//    }

//    public void wakeUp(){
//        synchronized (taskStorage){
//            taskStorage.notifyAll();
//        }
//    }



}
