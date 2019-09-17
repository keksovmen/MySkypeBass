import Bin.Networking.ClientController;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.CODE;
import Bin.Networking.Protocol.DataPackagePool;
import Bin.Networking.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {

        //Test auto accept

        AbstractDataPackagePool.init(new DataPackagePool());

        Server server = Server.getFromIntegers(8188, 40_000, 16, 8);
        server.start("Server");
        final int size = 10;
        List<ClientController> controllers = new ArrayList<>(size);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < size ; i++) {
            ClientController clientController = new ClientController(null);
            controllers.add(clientController);
            clientController.getProcessor().addListener(dataPackage -> {
                if (dataPackage.getHeader().getCode().equals(CODE.SEND_CALL)){
                    try {
                        countDownLatch.await();
                        clientController.getWriter().writeAccept(clientController.getMe().getId(), dataPackage.getHeader().getFrom());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            clientController.connect(String.valueOf(i), "127.0.0.1", 8188, 8192);
        }

        for (int i = 0; i < size; i++) {
            ClientController first = controllers.get(i);
            for (int j = 0; j < size; j++) {
                if (j != i) {
                    first.getWriter().writeCall(first.getMe().getId(), j + 3);
                }
            }
        }

        Thread.sleep(1_000);
        countDownLatch.countDown();

    }
}
