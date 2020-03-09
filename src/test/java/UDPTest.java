import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class UDPTest {

    public static void main(String[] args) throws IOException {
        //Server thread
        DatagramSocket server = new DatagramSocket(8188);
        new Thread(() -> {
            while (true) {
                byte[] bytes = new byte[2048];
                DatagramPacket datagramPacket = new DatagramPacket(bytes, 0, bytes.length);
                try {
                    server.receive(datagramPacket);
                    System.out.println(Arrays.toString(datagramPacket.getData()));
                    server.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        byte[] bytes = new byte[2048];
//        Arrays.fill(bytes, (byte) 1);
        ThreadLocalRandom.current().nextBytes(bytes);
        DatagramPacket datagramPacket = new DatagramPacket(bytes, 0, bytes.length, new InetSocketAddress("37.146.153.41", 8188));
        DatagramSocket client = new DatagramSocket(9999);
        client.send(datagramPacket);
        client.receive(datagramPacket);
        System.out.println(Arrays.toString(datagramPacket.getData()));

    }
}
