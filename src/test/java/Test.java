import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Test {


    public static void main(String[] args) throws Exception {

        AlgorithmsTest.testStringToByteAndOtherwise();
        AlgorithmsTest.usersTest();
        EncryptionDecryptionTest.encryptDecryptTest();

        int alo = -1;
        assert (byte) alo == -1;
        byte darov = -1;
        assert (int) darov == -1;
        int unisgnedInt = Byte.toUnsignedInt(darov);
        assert unisgnedInt == 255;
        assert (byte) unisgnedInt == -1;

        //Create key generator and get key pair such as secret part like A and common
        KeyPairGenerator aliceKeyPairGenerator = KeyPairGenerator.getInstance("DH");
        aliceKeyPairGenerator.initialize(1024);
        KeyPair aliceKeyPair = aliceKeyPairGenerator.generateKeyPair();

        //initialise key agreement object
        KeyAgreement aliceKeyAgreement = KeyAgreement.getInstance("DH");
        aliceKeyAgreement.init(aliceKeyPair.getPrivate());

        byte[] alicePublicEncodedKey = aliceKeyPair.getPublic().getEncoded();
        assert alicePublicEncodedKey != null : "Alice public key doesn't exist";
//        System.out.println("alicePublicEncodedKey = " + alicePublicEncodedKey.length);


        //Here Bob must decode alice key part and generate his part and give to Alice

        KeyFactory bobKeyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec bobKeySpec = new X509EncodedKeySpec(alicePublicEncodedKey);

        PublicKey aliceDecodedPublicKey = bobKeyFactory.generatePublic(bobKeySpec);
        assert aliceKeyPair.getPublic().equals(aliceDecodedPublicKey) : "Keys aren't identical";

        //When Bob generate his own DH pair, he must use Alice part for same secret
        DHParameterSpec dhParamsFromAlicePubKey = ((DHPublicKey) aliceDecodedPublicKey).getParams();

        //Bob creates his own key pair
        KeyPairGenerator bobKeyPairGenerator = KeyPairGenerator.getInstance("DH");
        bobKeyPairGenerator.initialize(dhParamsFromAlicePubKey);
        KeyPair bobKeyPair = bobKeyPairGenerator.generateKeyPair();

        //Bob init his Agreement object
        KeyAgreement bobKeyAgreement = KeyAgreement.getInstance("DH");
        bobKeyAgreement.init(bobKeyPair.getPrivate());

        //Bob send his part to Alice
        byte[] bobEncodedPublicKey = bobKeyPair.getPublic().getEncoded();

        /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */

        KeyFactory aliceKeyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec aliceKeySpec = new X509EncodedKeySpec(bobEncodedPublicKey);
        PublicKey bobDecodedPublicKey = aliceKeyFactory.generatePublic(aliceKeySpec);
        assert bobDecodedPublicKey.equals(bobKeyPair.getPublic()) : "Bob's public key is wrong";
        aliceKeyAgreement.doPhase(bobDecodedPublicKey, true);

        /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */

        bobKeyAgreement.doPhase(aliceDecodedPublicKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */

        byte[] aliceSharedSecret = aliceKeyAgreement.generateSecret();
        int aliceLength = aliceSharedSecret.length;
        byte[] bobSharedSecret = new byte[aliceLength];
        int bobLength = bobKeyAgreement.generateSecret(bobSharedSecret, 0);
        assert aliceLength == bobLength : "Different key sizes";
        assert Arrays.equals(aliceSharedSecret, bobSharedSecret) : "Different keys";
        System.out.printf("Alice key size is - %d shared secret is - %s\n", aliceSharedSecret.length, toHexString(aliceSharedSecret));
        System.out.printf("Bob key size is - %d shared secret is - %s\n", bobSharedSecret.length, toHexString(bobSharedSecret));

        /*
         * Now let's create a SecretKey object using the shared secret
         * and use it for encryption. First, we generate SecretKeys for the
         * "AES" algorithm (based on the raw shared secret data) and
         * Then we use AES in CBC mode, which requires an initialization
         * vector (IV) parameter. Note that you have to use the same IV
         * for encryption and decryption: If you use a different IV for
         * decryption than you used for encryption, decryption will fail.
         *
         * If you do not specify an IV when you initialize the Cipher
         * object for encryption, the underlying implementation will generate
         * a random one, which you have to retrieve using the
         * javax.crypto.Cipher.getParameters() method, which returns an
         * instance of java.security.AlgorithmParameters. You need to transfer
         * the contents of that object (e.g., in encoded format, obtained via
         * the AlgorithmParameters.getEncoded() method) to the party who will
         * do the decryption. When initializing the Cipher for decryption,
         * the (reinstantiated) AlgorithmParameters object must be explicitly
         * passed to the Cipher.init() method.
         */

        SecretKeySpec bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
        SecretKeySpec aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");
        assert bobAesKey.equals(aliceAesKey) : "Key specification is not the same";

        /*
         * Bob encrypts, using AES in CBC mode
         */

        Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, bobAesKey);
//        byte[] clearText = "This is just an example".getBytes();
        byte[] clearText = "1".getBytes();

        ByteBuffer byteBuffer = ByteBuffer.wrap(clearText);
        ByteBuffer secret = ByteBuffer.allocate(byteBuffer.capacity() + 16);


//        byte[] cipherText = bobCipher.doFinal(clearText);
        int f = bobCipher.doFinal(byteBuffer, secret);
        secret.flip();
        byte[] cipherText = new byte[f];
         secret.get(cipherText);


        // Retrieve the parameter that was used, and transfer it to Alice in
        // encoded format
        byte[] encodedParams = bobCipher.getParameters().getEncoded();
        // Instantiate AlgorithmParameters object from parameter encoding
        // obtained from Bob
        AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
        aesParams.init(encodedParams);
        Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, aliceAesKey, aesParams);
        byte[] recovered = aliceCipher.doFinal(cipherText);
        assert Arrays.equals(clearText, recovered) : "Text isn't the same";


        aliceCipher.init(Cipher.ENCRYPT_MODE, aliceAesKey, aesParams);
        byte[] aliceEncryptedString = aliceCipher.doFinal(clearText);

        bobCipher.init(Cipher.DECRYPT_MODE, bobAesKey, bobCipher.getParameters());
        byte[] bobDecryptedAliceString = bobCipher.doFinal(aliceEncryptedString);

        assert Arrays.equals(encodedParams, bobCipher.getParameters().getEncoded()) : "Params are changed";

        assert Arrays.equals(clearText, bobDecryptedAliceString) : "Text isn't the same";
        System.out.printf("%s\t%s\t%s\n", new String(clearText), new String(recovered), new String(bobDecryptedAliceString));


        //Third dude who received everything before encryption
        Cipher carlCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, aliceAesKey, aesParams);

        /*
         * Alice decrypts, using AES in CBC mode
         */

//        System.loadLibrary(Test.class.getResource("").getPath());
        //Test auto accept
//
//        AbstractDataPackagePool.init(new DataPackagePool());
//
//        SimpleServer server = SimpleServer.getFromIntegers(8188, 40_000, 16, 8);
//        server.start("SimpleServer");
//        final int size = 10;
//        List<ClientController> controllers = new ArrayList<>(size);
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        for (int i = 0; i < size ; i++) {
//            ClientController clientController = new ClientController(null, new ClientModelBase());
//            controllers.add(clientController);
////            clientController.getProcessor().setListener(dataPackage -> {
////                if (dataPackage.getHeader().getCode().equals(CODE.SEND_CALL)){
////                    try {
////                        countDownLatch.await();
////                        clientController.getWriter().writeAcceptCall(clientController.getMe().getId(), dataPackage.getHeader().getFrom());
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
//        new ClientResponder();
//        Body body = new Body();
//        Thread t1 = new Thread(() -> {
//            body.acquireSam();
//            body.show();
//            body.releaseSem();
//        }, "First");
//        Thread t2 = new Thread(() -> {
//            body.acquireSam();
//            body.show();
//            body.releaseSem();
//        }, "Second");
//        Thread t3 = new Thread(() -> {
//            body.acquireSam();
//            body.show();
//            body.releaseSem();
//        }, "Third");
//        t1.start();
//        t2.start();
//        t3.start();
//        Pattern compile = Pattern.compile("/bound\\?min_lon=\\d+\\.\\d+&min_lat=\\d+\\.\\d+&max_lon=\\d+\\.\\d+&max_lat=\\d+\\.\\d+&from_ts=\\d+&_=\\d+");
    }

    private static String toHexString(byte[] data) {
        StringBuilder result = new StringBuilder(data.length * 2);
        result.append("0x");
        int counter = 0;
        for (byte b : data) {
            if (counter % 4 == 0 && counter != 0) {
                result.append("\t0x");
            }
            result.append(byteToHex(b & 0xFF));
            counter++;

        }
        return result.toString();
    }

    private static final String[] HEX_MAP = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    private static String byteToHex(int data) {
        int first = data / 16;
        int zero = data % 16;
        return HEX_MAP[first] + HEX_MAP[zero];
    }


    private static class Body {
        private final Semaphore semaphore = new Semaphore(1);

        private void acquireSam() {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " Acquired");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void show() {
            System.out.println(Thread.currentThread().getName());
        }

        private void releaseSem() {
            semaphore.release();
            System.out.println(Thread.currentThread().getName() + " Released");

        }
    }
}
