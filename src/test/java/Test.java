import Bin.Audio.AudioClient;
import Bin.Main;
import Bin.Networking.ClientController;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Server;
import Bin.Networking.Writers.BaseWriter;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, InterruptedException {
//        Server server = new Server(8188, 20_000, 16);
//        server.start();

        Main.main(args);

//        AudioClient.getInstance().playMessageSound();
//        Thread.sleep(5_000);
////
//        ClientController clientController = new ClientController();
//        System.out.println(clientController.connect("loh pidr", "127.0.0.1", 8188) + " CLIENT");

//        BaseDataPackage aPackage = DataPackagePool.getPackage();
//        aPackage.init(BaseWriter.CODE.SEND_NAME, BaseWriter.WHO.NO_NAME.getCode(), BaseWriter.WHO.SERVER.getCode(), "LOH");
//        System.out.println(aPackage);
//        System.out.println(Arrays.toString(aPackage.getHeader().getRawHeader()));
//        aPackage.getHeader().init(new byte[]{0, 1, 0, 8, 0, 0, 0, 1});
//        System.out.println(aPackage);


//        System.out.println(123);
//        AudioFormat audioFormat = new AudioFormat(48_000f, 16, 1, true, true);
//        AudioClient audioClient = AudioClient.getInstance();
//        boolean isSet = audioClient.setAudioFormat(audioFormat);
//        System.out.println(isSet);
//        System.out.println(audioClient.add(1));
//        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFormat,
//                AudioSystem.getAudioInputStream(new File("E:\\Programm\\Java\\Projects\\MySkypeBass\\src\\main\\resources\\file.aiff")));
//
//        byte [] sound = new byte[10_000];
//        int i;
//        while ((i = audioInputStream.read(sound)) != -1){
//            audioClient.playAudio(1, true, sound);
//        }

//        Tester tester = new Tester();
//        new Thread(() -> {
//            while(true){
//                tester.ImcreaseI();
//            }
//        }).createServerSocket();
//        new Thread(() -> {
//            while (true) {
//                tester.showInt();
//            }
//        }).createServerSocket();

//        System.out.println(audioClient.add(2));
//        System.out.println(audioClient.add(3));
//        System.out.println(audioClient.add(4));
//        System.out.println(audioClient.add(5));
//        System.out.println(audioClient.add(6));
//        System.out.println(audioClient.add(7));
//        System.out.println(audioClient.add(8));
//        System.out.println(audioClient.add(9));
//        System.out.println(audioClient.add(10));
//        System.out.println(audioClient.add(11));
//        System.out.println(audioClient.add(12));
//        System.out.println(audioClient.add(13));
//        System.out.println(audioClient.add(14));
//        System.out.println(audioClient.add(15));
//        System.out.println(audioClient.toString());
//        audioClient.remove(12);
//        audioClient.remove(1234);
//        System.out.println(audioClient.toString());
//        audioClient.close();
//        System.out.println(audioClient.toString());

//        ClientProcessor clientProcessor = new ClientProcessor(new MyParser());
//        clientProcessor.push(new byte[]{0, 0, 0, 1, 0, 0, 1, 0, 5, 1});
//        clientProcessor.process();

//        JFrame jFrame = new JFrame();
//        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        jFrame.setSize(400, 400);
//        JPanel jPanel = new JPanel(new GridLayout(2, 1));
//        jFrame.add(jPanel);
//
//        JTextField textField = new JFormattedTextField();
//        jPanel.add(textField);
//
//        JButton jButton = new JButton("Show");
//        jButton.addActionListener(e -> JOptionPane.showMessageDialog(jPanel, textField.getText() + "\nDefault charset is " + Charset.defaultCharset()
//            + "\nText converted into UTF-16 " + new String(textField.getText().getBytes(StandardCharsets.UTF_16), StandardCharsets.UTF_16) ));
//        jPanel.add(jButton);
//
//        jFrame.setVisible(true);
//        int i = Integer.MAX_VALUE;
//        System.out.println((i >> 24) & 0xFF);

//        Server instance = Server.getInstance();
//        instance.init(8188, 44100, 16);
//        new Thread(() -> {
//            try {
//                instance.createServerSocket();
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("server createServerSocket fail");
//            }
//        }, "SERVER").start();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Socket socket = new Socket(InetAddress.getLocalHost(), 8188);
//        ClientProcessor clientProcessor = new ClientProcessor(new MainFrame());
//        clientProcessor.start();
////        new Thread(clientProcessor::process, "Client processor").createServerSocket();
//        new ClientReader(socket.getInputStream(), clientProcessor).start();
////        new Thread(new ClientReader(socket.getInputStream(), clientProcessor)::createServerSocket, "Client reader").createServerSocket();
//        ClientWriter clientWriter = new ClientWriter(socket.getOutputStream());
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        clientWriter.writeName("loh");
//        clientWriter.writeDisconnect(0);

//        java.lang.String a = "asd";
//        CharBuffer decode = Charset.defaultCharset().decode(ByteBuffer.wrap(a.getBytes()));
//        System.out.println(decode.toString());
//        byte[] bytes = a.getBytes(StandardCharsets.UTF_16);
//        System.out.println(a);
//        System.out.println(new java.lang.String(bytes, StandardCharsets.UTF_16));
//        Object o = new Object();
//        Class<?> c = o.getClass();
//        for (Method method : c.getMethods()) {
////            c.getdeclaredfie("asd").get();
//        }
    }
}
