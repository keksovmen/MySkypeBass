import com.sun.jna.ptr.PointerByReference;
import net.tomp2p.opuswrapper.Opus;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class TestCompression {

    //    static {
//        try {
//            System.loadLibrary("opus");
//        } catch (UnsatisfiedLinkError e) {
//            try {
//                File f = Native.extractFromResourcePath("opus");
//                System.load(f.getAbsolutePath());
//            } catch (Exception e1) {
//                e.printStackTrace();
//                e1.printStackTrace();
//            }
//        }
//    }
    private static TargetDataLine input;
    private static SourceDataLine output;

    private static Executor executor = Executors.newSingleThreadExecutor();

    private static final int FRAME_SIZE = 640;

    public static void main(String[] args) throws LineUnavailableException {
        int vals [] = new int[]{-3, -1, 0 , 1 , 3};
//        Arrays.stream(vals).max().getAsInt()
//        Collections.emptyList()
        JFrame frame = new JFrame("TEST");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        Collections.max(Collections.emptyList(), (o1, o2) -> 1);
        AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, true);
        assert testConversionFront();
        assert testConversionBack();
        input = createTargetLine(audioFormat);
        output = createSourceLine(audioFormat);
        long encodeTimeAll = 0;
        long decodeTimeAll = 0;
        long iteration = 0;
        while (true) {
            final ShortBuffer readFromMic = readFromMic(audioFormat, 0.120f, input);
//            play(audioFormat, shortsToByteLittleEndian(readFromMic));
            long timeBeforeNano = System.nanoTime();
            List<ByteBuffer> encoded = encode(audioFormat, readFromMic);
            long timeResultMicro = (System.nanoTime() - timeBeforeNano) / 1000;
            encodeTimeAll += timeResultMicro;

            final int encodedSize = encoded.stream().mapToInt(Buffer::limit).sum();
            timeBeforeNano = System.nanoTime();
            ShortBuffer decoded = decode(audioFormat, encoded);
            timeResultMicro = (System.nanoTime() - timeBeforeNano) / 1000;
            decodeTimeAll += timeResultMicro;

            iteration++;

            System.out.printf("Initial size - %d\tEncoded - %d\tDecoded - %d\tEncode t - %d\tDecode t - %d\n",
                    readFromMic.limit() * 2, encodedSize, decoded.limit() * 2, encodeTimeAll / iteration, decodeTimeAll / iteration);

            executor.execute(() -> {
                try {
                    if (audioFormat.isBigEndian())
                        play(audioFormat, shortsToByteBigEndian(readFromMic), output);
                    else
                        play(audioFormat, shortsToByteLittleEndian(readFromMic), output);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            });
//            play(audioFormat, shortsToByteLittleEndian(decoded));
        }
    }

    private static ShortBuffer readFromMic(AudioFormat audioFormat, float durationSec) throws LineUnavailableException {
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        int bufferSize = (int) (audioFormat.getSampleRate() * audioFormat.getFrameSize() * durationSec);
        bufferSize = bufferSize - bufferSize % audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        targetDataLine.read(buffer, 0, buffer.length);
        targetDataLine.close();
//        return bytesToShortBigEndian(buffer);
        if (audioFormat.isBigEndian())
            return bytesToShortBigEndian(buffer);
        else
            return bytesToShortLittleEndian(buffer);
    }

    private static ShortBuffer readFromMic(AudioFormat audioFormat, float durationSec, TargetDataLine targetDataLine) throws LineUnavailableException {
        int bufferSize = (int) (audioFormat.getSampleRate() * audioFormat.getFrameSize() * durationSec);
        bufferSize = bufferSize - bufferSize % audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        targetDataLine.read(buffer, 0, buffer.length);
//        return bytesToShortBigEndian(buffer);
        if (audioFormat.isBigEndian())
            return bytesToShortBigEndian(buffer);
        else
            return bytesToShortLittleEndian(buffer);
    }

    private static void play(AudioFormat audioFormat, byte[] data) throws LineUnavailableException {
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        sourceDataLine.write(data, 0, data.length);
//        sourceDataLine.drain();
        sourceDataLine.close();
    }

    private static void play(AudioFormat audioFormat, byte[] data, SourceDataLine sourceDataLine) throws LineUnavailableException {
        int write = sourceDataLine.write(data, 0, data.length);
        if (write != data.length) {
            sourceDataLine.write(data, write, data.length - write);
        }
//        sourceDataLine.drain();
    }

    private static List<ByteBuffer> encode(AudioFormat audioFormat, ShortBuffer raw) {
        IntBuffer error = IntBuffer.allocate(4);
        PointerByReference encoder = Opus.INSTANCE.opus_encoder_create(
                (int) audioFormat.getSampleRate(), audioFormat.getChannels(),
                Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY, error);

        List<ByteBuffer> byteBuffers = new ArrayList<>();
        int read = 0;
        while (raw.hasRemaining()) {
            ByteBuffer bytes = ByteBuffer.allocate(2048);//tmp size may not work with every sample rate
            int toRead = Math.min(raw.remaining(), bytes.remaining());

            read = Opus.INSTANCE.opus_encode(encoder, raw, FRAME_SIZE, bytes, toRead);
            if (read < 0)
                throw new RuntimeException("Encoding failed - " + read);
            bytes.position(bytes.position() + read);
            bytes.flip();
            byteBuffers.add(bytes);
            if (raw.position() + FRAME_SIZE > raw.capacity()) {
                raw.position(raw.capacity());
                continue;
            }
            raw.position(raw.position() + FRAME_SIZE);
        }
        raw.flip();
        Opus.INSTANCE.opus_encoder_destroy(encoder);
        return byteBuffers;
    }

    private static ShortBuffer decode(AudioFormat audioFormat, List<ByteBuffer> encoded) {
        IntBuffer error = IntBuffer.allocate(4);
        PointerByReference decoder = Opus.INSTANCE.opus_decoder_create(
                (int) audioFormat.getSampleRate(), audioFormat.getChannels(), error);

        ShortBuffer shortBuffer = ShortBuffer.allocate(1024 * 1024);
        for (ByteBuffer byteBuffer : encoded) {
            byte[] receivedEncoded = new byte[byteBuffer.limit()];
            byteBuffer.get(receivedEncoded);
            int decoded = Opus.INSTANCE.opus_decode(decoder, receivedEncoded, receivedEncoded.length, shortBuffer, FRAME_SIZE, 0);
//            Opus.INSTANCE.opus_packet_get_nb_frames()
            shortBuffer.position(shortBuffer.position() + decoded);
        }
        Opus.INSTANCE.opus_decoder_destroy(decoder);
        shortBuffer.flip();
        return shortBuffer;
    }

    private static ShortBuffer bytesToShortBigEndian(byte[] bytes) {
        ShortBuffer buffer = ShortBuffer.allocate(bytes.length / 2);
        for (int i = 0; i < bytes.length; i += 2) {
            int high = bytes[i] << 8;
            int low = bytes[i + 1] & 0xFF;
            buffer.put((short) (high | low));
        }
        buffer.flip();
        return buffer;
    }

    private static ShortBuffer bytesToShortLittleEndian(byte[] bytes) {
        ShortBuffer buffer = ShortBuffer.allocate(bytes.length / 2);
        for (int i = 0; i < bytes.length; i += 2) {
            int high = bytes[i + 1] << 8;
            int low = bytes[i] & 0xFF;
            buffer.put((short) (high | low));
        }
        buffer.flip();
        return buffer;
    }

    private static byte[] shortsToByteBigEndian(ShortBuffer buffer) {
        byte[] bytes = new byte[buffer.limit() * 2];
        short[] shortArray = buffer.array();
        for (int i = 0; i < buffer.limit(); i++) {
            short s = shortArray[i];
            byte high = (byte) (s >> 8);
            byte low = (byte) s;
            bytes[i * 2] = high;
            bytes[(i * 2) + 1] = low;
        }
        return bytes;
    }

    private static byte[] shortsToByteLittleEndian(ShortBuffer buffer) {
        byte[] bytes = new byte[buffer.limit() * 2];
        short[] shortArray = buffer.array();
        for (int i = 0; i < buffer.limit(); i++) {
            short s = shortArray[i];
            byte high = (byte) (s >> 8);
            byte low = (byte) s;
            bytes[(i * 2) + 1] = high;
            bytes[i * 2] = low;
        }
        return bytes;
    }

    private static boolean testConversionFront() {
        byte[] test = new byte[]{2, -55};
        return bytesToShortBigEndian(test).get() == 712;
    }

    private static boolean testConversionBack() {
        ShortBuffer shortBuffer = ShortBuffer.wrap(new short[]{712});
        return Arrays.equals(shortsToByteBigEndian(shortBuffer), new byte[]{2, -55});
    }

    private static SourceDataLine createSourceLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat);
        line.start();
        return line;
    }

    private static TargetDataLine createTargetLine(AudioFormat audioFormat) throws LineUnavailableException {
        TargetDataLine line = AudioSystem.getTargetDataLine(audioFormat);
        line.open(audioFormat);
        line.start();
        return line;
    }

    private static class JC extends JComponent{

        @Override
        protected void printComponent(Graphics g) {
//            super.printComponent(g);
            Graphics2D g2 = (Graphics2D) g;
//            g2.drawOval();
//            g2.drawLine();
        }
    }
}
