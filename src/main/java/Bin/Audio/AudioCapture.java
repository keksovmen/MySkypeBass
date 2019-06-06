package Bin.Audio;

import Bin.Changeable;
import Bin.Networking.Processors.Processor;
import Bin.Networking.Startable;
import Bin.Networking.Writers.ClientWriter;

import java.io.IOException;

public class AudioCapture /*implements Processor, Startable*/ {

    private volatile boolean started;
    private volatile boolean mute;
    private boolean work;

    public AudioCapture() {
        work = true;
    }

    synchronized boolean mute() {
        mute = !mute;
        notify();
        return mute;
    }

    //    @Override
    private void process(final ClientWriter writer, final int from) throws IOException {
        byte[] audio = AudioClient.getInstance().captureAudio();
        if (audio == null) {
            work = false;
            return;
        }
        writer.writeSound(from, audio);
    }

    //    @Override
    void start(final ClientWriter writer, final int from) {
        if (started) {
            return;
        }
        started = true;
        work = true;
        mute = false;
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
                    process(writer, from);
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
            started = false;
        }, "Client capture").start();
    }

    //    @Override
    void close() {
        work = false;
    }

}
