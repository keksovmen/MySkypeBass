package Bin.Audio;

import Bin.Changeable;
import Bin.Networking.Processors.Processor;
import Bin.Networking.Startable;
import Bin.Networking.Writers.ClientWriter;

import java.io.IOException;

public class AudioCapture implements Processor, Startable, Changeable {

    private ClientWriter writer;
    private boolean work;
    private int multiplier = 1;
    private int from;
    private boolean mute;
    private boolean started;


    public AudioCapture(ClientWriter writer, int from) {
        this.writer = writer;
        work = true;
        this.from = from;
    }

    public synchronized void mute(){
        mute = !mute;
        notify();
    }

    @Override
    public void process() throws IOException {
        byte[] audio = AudioClient.getInstance().captureAudio();
        if (audio == null){
            work = false;
            return;
        }
        if (multiplier > 1){
            for (int i = 0; i < audio.length; i++) {
                audio[i] = (byte) (audio[i] * multiplier);
            }
        }
        writer.writeSound(from, audio);
    }

    @Override
    public void start() {
        if (started) return;
        started = true;
        new Thread(() -> {
            while (work) {
                synchronized (this) {
                    if (mute) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }, "Client capture").start();
    }

    @Override
    public void close() {
        work = false;
    }

    @Override
    public void change(int amount) {
        if (amount >= 1)
            multiplier = amount;
    }
}
