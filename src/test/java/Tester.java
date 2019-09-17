import Bin.Networking.ClientController;
import Bin.Networking.Protocol.*;
import Bin.Networking.Readers.BaseReader;
import Bin.Networking.Server;
import Bin.Networking.Utility.WHO;
import Bin.Util.Algorithms;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Tester {

    private int anInt;

    public synchronized void ImcreaseI() {
        anInt++;
    }

    public void showInt() {
        System.out.println(anInt);
    }

    public synchronized void showSync() {
        System.out.println("SYNC = " + anInt);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        assert (ProtocolBitMap.MAX_VALUE > 0) : "ProtocolBitMap.MAX_VALUE is negative";
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        assert (CODE.parse(1) == CODE.SEND_NAME);
        System.out.printf("0x%x\t0b%s\n", -128, Integer.toBinaryString(-128).substring(24));
        System.out.printf("0x%x\t0b%s\n", -1, Integer.toBinaryString(-1).substring(24));
        System.out.printf("0x%x\t0b%s\n", 128, Integer.toBinaryString(128));
        System.out.println(Algorithms.combineTwoBytes((byte) 1, (byte) 0));
        assert (Algorithms.combineTwoBytes((byte) 255, (byte) 255) == 65535);
        assert (DataPackageHeader.Test());

        byte packet[] = new byte[]{0, 1, 0, 2, 1, 1, 0, -1, -1, -1};
        BaseReader baseReader = new BaseReader(
                new ByteArrayInputStream(packet),
                32
        );

        AbstractDataPackagePool.init(new DataPackagePool());

        AbstractDataPackage read = baseReader.read();
        assert (read.getHeader().getCode().equals(CODE.SEND_NAME));
        assert (read.getHeader().getLength() == 2);
        assert (read.getHeader().getFrom() == 257);
        assert (read.getHeader().getTo() == 255);

        System.out.println("Creating server");
        Server server = Server.getFromIntegers(
                8188,
                32_000,
                16,
                10);

        System.out.println("Server start checks");
        assert (server.start("Server"));
        assert (!server.start("Server"));

        System.out.println("Creating Client controller");
        ClientController clientController = new ClientController(null);
        System.out.println("Client controller connect");
        assert (clientController.connect(
                "Vasa",
                "127.0.0.1",
                8188,
                8192));

        Thread.sleep(1_000);
        System.out.println("Check users");
        assert (server.getUser(WHO.SIZE).equals(clientController.getMe()));

        System.out.println("Closing");
        clientController.close();
        server.close();
//        int v = ProtocolBitMap.INSTRUCTION_SIZE |
//                ProtocolBitMap.LENGTH_SIZE | ProtocolBitMap.FROM_SIZE |
//                ProtocolBitMap.TO_SIZE;
//        int b = v * 4;
//        System.out.println(b);
//        System.out.println(ProtocolBitMap.PACKET_SIZE);
//        System.out.println(b == ProtocolBitMap.PACKET_SIZE ? 1 : 0);
//        System.out.println(12 == 8 ? 1 : 0);
//        FileOutputStream fileWriter = new FileOutputStream(new File("D:\\Users\\Roma\\Desktop\\1488.ts"), true);
//
//        File file = new File("D:\\Users\\Roma\\Desktop\\Новая папка\\1.ts");
//        Stream<Path> list = Files.list(Paths.get("D:\\Users\\Roma\\Desktop\\Новая папка\\"));
//        Path[] objects = list.toArray(Path[]::new);
//        Arrays.sort(objects, new Comparator<Path>() {
//            @Override
//            public int compare(Path o1, Path o2) {
//                return o1.getFileName().toString().hashCode() - o2.getFileName().toString().hashCode();
//            }
//        });
////        System.out.println(Arrays.toString(objects));
//        for (Path object : objects) {
//            try(FileInputStream fileInputStream = new FileInputStream(object.toFile())){
//                int size = (int) object.toFile().length();
//                byte[] data = new byte[size];
//                fileInputStream.read(data);
//                fileWriter.write(data);
//            }
//        }
//        fileWriter.close();
//        System.setProperty("java.util.logging.config.file", Paths.get("src\\main\\resources\\properties\\logging.properties").toString());
//        LogManager.getLogManager().readConfiguration();
//        Main.main(args);


    }
}
