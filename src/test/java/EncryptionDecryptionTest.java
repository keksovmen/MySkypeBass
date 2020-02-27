import com.Abstraction.Networking.Utility.Users.CipherUser;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Util.Cryptographics.BaseClientCryptoHelper;
import com.Abstraction.Util.Cryptographics.BaseServerCryptoHelper;
import com.Abstraction.Util.Cryptographics.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class EncryptionDecryptionTest {

    public static void encryptDecryptTest() throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, ShortBufferException, IllegalBlockSizeException {
        BaseClientCryptoHelper clientCryptoHelper = new BaseClientCryptoHelper();

        clientCryptoHelper.initialiseKeyGenerator();
        byte[] publicKeyEncoded = clientCryptoHelper.getPublicKeyEncoded();

        BaseServerCryptoHelper serverCryptoHelper = new BaseServerCryptoHelper();
        serverCryptoHelper.initialiseKeyGenerator(publicKeyEncoded);

        clientCryptoHelper.finishExchange(serverCryptoHelper.getPublicKeyEncoded());

        byte[] algorithmParametersEncoded = serverCryptoHelper.getAlgorithmParametersEncoded();
        clientCryptoHelper.setAlgorithmParametersEncoded(algorithmParametersEncoded);

        User baseUser1 = new CipherUser("1", 1, clientCryptoHelper.getKey(), clientCryptoHelper.getParameters());
        User baseUser2 = new CipherUser("2", 2, serverCryptoHelper.getKey(), serverCryptoHelper.getParameters());

        assert clientCryptoHelper.getKey().equals(serverCryptoHelper.getKey());

//        clientCryptoHelper.initialiseKeyGenerator();
//        publicKeyEncoded = clientCryptoHelper.getPublicKeyEncoded();
//
//        serverCryptoHelper.initialiseKeyGenerator(publicKeyEncoded);
//        algorithmParametersEncoded = serverCryptoHelper.getAlgorithmParametersEncoded();
//        clientCryptoHelper.setAlgorithmParametersEncoded(algorithmParametersEncoded);

        User baseUser3 = User.parse(baseUser1.toNetworkFormat());

        Cipher cipher1 = Crypto.getCipherWithoutExceptions(Crypto.STANDARD_CIPHER_FORMAT);
        cipher1.init(Cipher.ENCRYPT_MODE, baseUser1.getSharedKey(), baseUser1.getAlgorithmParameters());

        byte[] test = "TEST".getBytes();
        ByteBuffer allocate = ByteBuffer.allocate(32);
        int amount = cipher1.doFinal(ByteBuffer.wrap(test), allocate);
        byte[] result = new byte[amount];
        allocate.flip();
        allocate.get(result);

        Cipher cipher2 = Crypto.getCipherWithoutExceptions(Crypto.STANDARD_CIPHER_FORMAT);
        cipher2.init(Cipher.DECRYPT_MODE, baseUser3.getSharedKey(), baseUser3.getAlgorithmParameters());
        byte[] bytes = cipher2.doFinal(result);

        assert Arrays.equals(test, bytes);
    }
}
