package Com.Audio.Input;

import Com.Audio.AudioSupplier;
import Com.Util.Algorithms;
import Com.Util.Collection.ArrayBlockingQueueWithWait;
import Com.Util.Resources;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Capture implements DefaultMic, ChangeableInput {

    private static final float MIN_BASS_LVL = 1f;
    private static final float MAX_BASS_LVL = 20f;

    private final Consumer<byte[]> sendData;

    private volatile TargetDataLine mic = null;
    private Mixer.Info mixer = null;

    private volatile boolean muted = false;

    private volatile boolean work = false;

    private volatile float bassLvl = 1f;

    private volatile ExecutorService executorService;

    public Capture(Consumer<byte[]> sendData) {
        this.sendData = sendData;
    }

    @Override
    public void changeInput(Mixer.Info mixer) {
        if (mixer == null /*&& this.mixer == null*/)
            mixer = AudioSupplier.getDefaultForInput();
        this.mixer = mixer;
        if (mic != null)
            mic.close();

        try {
            mic = AudioSupplier.getInput(this.mixer);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mute() {
        muted = !muted;
        if (!muted) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public void IncreaseBass(int percentage) {
        bassLvl = Algorithms.mean((int) MIN_BASS_LVL, (int) MAX_BASS_LVL, percentage);
    }

    @Override
    public synchronized boolean start(String name) {
        if (work)
            return false;
        work = true;
        muted = false;
        if (mic == null || !mic.isOpen()) {
            try {
                mic = AudioSupplier.getInput(mixer);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return false;
            }
        }

        executorService = getDefaultOne();
        new Thread(() -> {
            while (work) {
                if (muted) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!work)
                    break;
                byte[] bytes = bassBoost(readFromMic());

                synchronized (this) {
                    if (!executorService.isShutdown())
                        executorService.execute(() -> sendData.accept(bytes));
                }
            }
        }, name).start();

        return true;
    }

    @Override
    public synchronized void close() {
        work = false;
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        mic.close();
        bassLvl = 1f;
        this.notify();
    }

    @Override
    public void init() {
        changeInput(null);
    }

    private byte[] readFromMic() {
        byte[] bytes = new byte[AudioSupplier.getMicCaptureSize()];
        mic.read(bytes, 0, bytes.length);
        return bytes;
    }

    private byte[] bassBoost(byte[] data) {
        if (bassLvl == 1f)
            return data;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] * bassLvl);
        }
        return data;
    }

    private ExecutorService getDefaultOne() {
        return new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueueWithWait<>(Resources.getMicQueueSize()));
    }
}
