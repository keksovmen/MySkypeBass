import Bin.Networking.Protocol.CODE;
import Bin.Networking.Protocol.DataPackageHeader;
import Bin.Networking.Protocol.ProtocolBitMap;
import Bin.Util.Algorithms;

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


    public static void main(String[] args) {
        assert (ProtocolBitMap.MAX_VALUE > 0) : "ProtocolBitMap.MAX_VALUE is negative";
        CODE.uniqueIdCheck();
        assert (CODE.parse(1) == CODE.SEND_NAME);
        System.out.printf("0x%x\t0b%s\n", -128, Integer.toBinaryString(-128).substring(24));
        System.out.printf("0x%x\t0b%s\n", -1, Integer.toBinaryString(-1).substring(24));
        System.out.printf("0x%x\t0b%s\n", 128, Integer.toBinaryString(128));
        System.out.println(Algorithms.combineTwoBytes((byte) 1, (byte) 0));
        assert (Algorithms.combineTwoBytes((byte) 255, (byte) 255) == 65535);
        assert (DataPackageHeader.Test());
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
