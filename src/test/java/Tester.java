import Com.Client;
import Com.GUI.Forms.AudioFormatStats;
import Com.Model.ClientModel;
import Com.Networking.ClientController;
import Com.Networking.Processors.ClientProcessor;
import Com.Networking.Protocol.*;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Server;
import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.WHO;
import Com.Util.Algorithms;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        AbstractDataPackagePool.init(new DataPackagePool());
//        testPackage();
//        testServer();
//        clientTest();
        testFullConstruction();

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

    private static void testPackage() throws IOException {
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

//        AbstractDataPackagePool.init(new DataPackagePool());

        AbstractDataPackage read = baseReader.read();
        assert (read.getHeader().getCode().equals(CODE.SEND_NAME));
        assert (read.getHeader().getLength() == 2);
        assert (read.getHeader().getFrom() == 257);
        assert (read.getHeader().getTo() == 255);
    }

    private static void testServer() throws InterruptedException, IOException {
        System.out.println("SERVER TEST BEGINS");

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
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<ClientController> clientControllers = new ArrayList<>();
        ClientModel clientModel = new ClientModel();
        clientModel.setMe(new BaseUser("Unique", 0));
        ClientModel clientModel1 = new ClientModel();
        clientModel1.setMe(new BaseUser("Vasa", 0));
        ClientModel clientModel2 = new ClientModel();
        clientModel2.setMe(new BaseUser("Loh", 0));
        ClientProcessor processor = new ClientProcessor();
        ClientController clientController = new ClientController(processor, clientModel);
        System.out.println("Client controller connect");
        assert (clientController.connect(
                "127.0.0.1",
                8188,
                8192));
        assert clientController.start("Client controller");
        for (int i = 0; i < 20; i++) {
            ClientController clientController1 = new ClientController(processor, clientModel1);
            clientControllers.add(clientController1);
            ClientController clientController2 = new ClientController(processor, clientModel2);
            executorService.submit(() ->
            {
//                try {
                assert clientController1.connect(
                        "127.0.0.1",
                        8188,
                        8192);
                assert clientController1.start("Client controller");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    System.exit(-1);
//                }
            });
            executorService.submit(() ->
            {
//                try {
                assert clientController2.connect(
                        "127.0.0.1",
                        8188,
                        8192);
                assert clientController2.start("Client controller");
                clientController2.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    System.exit(-1);
//                }
            });
        }

        Thread.sleep(3_000);
        System.out.println("Check users");
        assert (server.getUser(WHO.SIZE).equals(clientController.getMe()));
        int controllersSize = server.getControllersSize();
        assert (controllersSize == 21) : "Controller size is wrong and equal to = " + controllersSize;
        System.out.println("Closing");
        clientController.close();
        server.close();
        clientControllers.forEach(ClientController::close);
        executorService.shutdown();
        processor.close();

        System.out.println("SERVER TEST ENDS");
    }

    private static void clientTest() throws IOException, InterruptedException {
        System.out.println("CLIENT TEST BEGINS");
        Server server = Server.getFromIntegers(
                8188,
                32000,
                16,
                12
        );

        server.start("Server");

        ClientModel model = new ClientModel();
        model.setMe(new BaseUser("Test", WHO.NO_NAME.getCode()));

        ClientProcessor processor = new ClientProcessor();

        processor.getOnAddUserToList().setListener(
                abstractDataPackage -> {
                    String dataAsString = abstractDataPackage.getDataAsString();
                    model.addToModel(BaseUser.parse(dataAsString));
                });

        processor.getOnRemoveUserFromList().setListener(
                abstractDataPackage -> {
                    int dataAsInt = abstractDataPackage.getDataAsInt();
                    model.removeFromModel(dataAsInt);
                });

        ClientController clientController = new ClientController(processor, model);

        System.out.println("Connecting");

        assert clientController.connect("127.0.0.1", 8188, 8192);
        assert clientController.start("Client controller");
        ExecutorService service = Executors.newFixedThreadPool(10);

        System.out.println("Connected");

        List<ClientController> controllers = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
//            service.execute(() -> {
            ClientModel tmpModel = new ClientModel();
            tmpModel.setMe(new BaseUser("Pidr", WHO.NO_NAME.getCode()));

            ClientProcessor tmpProcessor = new ClientProcessor();

            ClientController tmpClientController = new ClientController(tmpProcessor, tmpModel);
            controllers.add(tmpClientController);

            service.execute(() -> {
                assert tmpClientController.connect("127.0.0.1", 8188, 8192);
                tmpClientController.start("Pidr");
            });

//            });
        }

        Thread.sleep(2_000);

        int size = model.getUserMap().size();
        assert size == 10 : size;

        controllers.forEach(ClientController::close);

        Thread.sleep(2_000);

        assert model.getUserMap().size() == 0;

        clientController.close();
        server.close();
        service.shutdown();
//        clientController.close();

        System.out.println("CLIENT TEST ENDS");
    }

    public static void testFullConstruction() throws IOException {
//        Server server = Server.getFromIntegers(8188, 32_000, 16, 12);
//        server.start("Server");
        Client client = new Client();
    }
}
