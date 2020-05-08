import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;
import net.tomp2p.opuswrapper.Opus;

import javax.sound.sampled.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCompression {

    static {
        try {
            System.loadLibrary("opus");
        } catch (UnsatisfiedLinkError e) {
            try {
                File f = Native.extractFromResourcePath("opus");
                System.load(f.getAbsolutePath());
            } catch (Exception e1) {
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
    }

    private static final int FRAME_SIZE = 80;

    public static void main(String[] args) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, true);
        assert testConversionFront();
        assert testConversionBack();
        while (true) {
            ShortBuffer readFromMic = readFromMic(audioFormat, 3);
            play(audioFormat, shortsToByteBigEndian(readFromMic));
            List<ByteBuffer> encoded = encode(audioFormat, readFromMic);
            ShortBuffer decoded = decode(audioFormat, encoded);
            play(audioFormat, shortsToByteBigEndian(decoded));
        }
    }

    private static ShortBuffer readFromMic(AudioFormat audioFormat, int durationSec) throws LineUnavailableException {
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        byte[] buffer = new byte[(int) (audioFormat.getSampleRate() * audioFormat.getFrameSize() * durationSec)];
        targetDataLine.read(buffer, 0, buffer.length);
        targetDataLine.close();
        return bytesToShortBigEndian(buffer);
    }

    private static void play(AudioFormat audioFormat, byte[] data) throws LineUnavailableException {
        SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        sourceDataLine.write(data, 0, data.length);
//        sourceDataLine.drain();
        sourceDataLine.close();
    }

    private static List<ByteBuffer> encode(AudioFormat audioFormat, ShortBuffer raw) {
        IntBuffer error = IntBuffer.allocate(4);
        PointerByReference encoder = Opus.INSTANCE.opus_encoder_create(
                (int) audioFormat.getSampleRate(), audioFormat.getChannels(),
                Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY, error);

        List<ByteBuffer> byteBuffers = new ArrayList<>();
        int read = 0;
        while (raw.hasRemaining()) {
            ByteBuffer bytes = ByteBuffer.allocate(1024);//tmp size may not work with every sample rate
            int toRead = Math.min(raw.remaining(), bytes.remaining());

            read = Opus.INSTANCE.opus_encode(encoder, raw, FRAME_SIZE, bytes, toRead);
            if (read < 0)
                throw new RuntimeException("Encoding failed");
            bytes.position(bytes.position() + read);
            bytes.flip();
            byteBuffers.add(bytes);
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

    private static boolean testConversionFront() {
        byte[] test = new byte[]{2, -55};
        return bytesToShortBigEndian(test).get() == 712;
    }

    private static boolean testConversionBack() {
        ShortBuffer shortBuffer = ShortBuffer.wrap(new short[]{712});
        return Arrays.equals(shortsToByteBigEndian(shortBuffer), new byte[]{2, -55});
    }
}
