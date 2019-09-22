import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Test {



    public static void main(String[] args) throws IOException, InterruptedException {

        //Test auto accept
//
//        AbstractDataPackagePool.init(new DataPackagePool());
//
//        Server server = Server.getFromIntegers(8188, 40_000, 16, 8);
//        server.start("Server");
//        final int size = 10;
//        List<ClientController> controllers = new ArrayList<>(size);
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        for (int i = 0; i < size ; i++) {
//            ClientController clientController = new ClientController(null, new ClientModel());
//            controllers.add(clientController);
////            clientController.getProcessor().setListener(dataPackage -> {
////                if (dataPackage.getHeader().getCode().equals(CODE.SEND_CALL)){
////                    try {
////                        countDownLatch.await();
////                        clientController.getWriter().writeAccept(clientController.getMe().getId(), dataPackage.getHeader().getFrom());
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////            });
//            clientController.connect("127.0.0.1", 8188, 8192);
//        }
//
//        for (int i = 0; i < size; i++) {
//            ClientController first = controllers.get(i);
//            for (int j = 0; j < size; j++) {
//                if (j != i) {
////                    first.getWriter().writeCall(first.getMe().getId(), j + 3);
//                }
//            }
//        }
//
//        Thread.sleep(1_000);
//        countDownLatch.countDown();
//        CODE.uniqueIdCheck();
//        WHO.uniqueIdCheck();
//        AbstractDataPackagePool.init(new DataPackagePool());
//        new Client();
        Body body = new Body();
        Thread t1 = new Thread(() -> {
            body.acquireSam();
            body.show();
            body.releaseSem();
        }, "First");
        Thread t2 = new Thread(() -> {
            body.acquireSam();
            body.show();
            body.releaseSem();
        }, "Second");
        Thread t3 = new Thread(() -> {
            body.acquireSam();
            body.show();
            body.releaseSem();
        }, "Third");
        t1.start();
        t2.start();
        t3.start();
//        Pattern compile = Pattern.compile("/bound\\?min_lon=\\d+\\.\\d+&min_lat=\\d+\\.\\d+&max_lon=\\d+\\.\\d+&max_lat=\\d+\\.\\d+&from_ts=\\d+&_=\\d+");
    }



    private static class Body{
        private Semaphore semaphore = new Semaphore(1);

        private void acquireSam(){
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " Acquired");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void show(){
            System.out.println(Thread.currentThread().getName());
        }

        private void releaseSem(){
            semaphore.release();
            System.out.println(Thread.currentThread().getName() + " Released");

        }
    }
}
