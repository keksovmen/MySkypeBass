import com.Abstraction.Networking.Utility.Users.CipherUser;
import com.Abstraction.Networking.Utility.Users.PlainUser;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Util.Algorithms;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AlgorithmsTest {

    public static void testStringToByteAndOtherwise() {
        String origin = "Loh Pidor";
        String originInBytesAsString = Algorithms.byteArrayToString(origin.getBytes());
        byte[] bytesFromBteString = Algorithms.stringToByteArray(originInBytesAsString);
        String recovered = new String(bytesFromBteString);
        assert origin.equals(recovered);
    }

    public static void usersTest() throws NoSuchAlgorithmException, IOException {
        String stringSimpleUser = "Loh - 123";
        User simpleParsedUser = User.parse(stringSimpleUser);
        User constrictedSimpleUser = new PlainUser("Loh", 123);
        assert simpleParsedUser.equals(constrictedSimpleUser);
        assert simpleParsedUser.toString().equals(constrictedSimpleUser.toString());

        String stringKey = "38 11 207 10 122 134 224 120 211 83 90 93 202 63 237 134 ";
        byte[] originalKeyBytes = {38, 11, -49, 10, 122, -122, -32, 120, -45, 83, 90, 93, -54, 63, -19, -122};
        assert Arrays.equals(Algorithms.stringToByteArray(stringKey), originalKeyBytes);
        assert stringKey.equals(Algorithms.byteArrayToString(originalKeyBytes));

        String par = "4 16 38 67 80 231 236 17 18 113 188 160 9 160 212 230 254 17 ";
        byte[] originBytePar = {4, 16, 38, 67, 80, -25, -20, 17, 18, 113, -68, -96, 9, -96, -44, -26, -2, 17};
        assert Arrays.equals(Algorithms.stringToByteArray(par), originBytePar);
        assert par.equals(Algorithms.byteArrayToString(originBytePar));

        AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
        parameters.init(originBytePar);

        String stringComplexUser = "Loh - 123 : " + stringKey + "- " + par;

        User complexParsedUser = User.parse(stringComplexUser);
        User constrictedComplexUser = new CipherUser("Loh", 123,
                new SecretKeySpec(originalKeyBytes, "AES"),
                parameters
        );
        assert complexParsedUser.equals(constrictedComplexUser);
        assert complexParsedUser.toString().equals(constrictedComplexUser.toString());

    }
}
