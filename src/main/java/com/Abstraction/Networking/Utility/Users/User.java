package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Util.Cryptographics.Crypto;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface User {

    /**
     * Represent how it should look like
     * Means any letters or digits then space - space and unique id as digits
     */

    Pattern parser = Pattern.compile("(.*) - (\\d+)( : ((\\d{1,3} ){16})- ((\\d{1,3} )+))?");


    /**
     * Static factory from string
     *
     * @param data in format baseUser.toString()
     * @return new user
     */

    static User parse(String data) {
        Matcher matcher = parser.matcher(data);
        if (!matcher.matches())
            throw new IllegalArgumentException("Base user is in wrong format - " + data);
        final String name = matcher.group(1);
        final int id = Integer.parseInt(matcher.group(2));
        String keyAndParam = matcher.group(3);
        if (keyAndParam == null) {
            return new PlainUser(name, id);
        } else {
            final Key key = Crypto.createCipherKey(matcher.group(4));
            final AlgorithmParameters parameters = Crypto.createParameters(matcher.group(6));
            return new CipherUser(name, id, key, parameters);
        }
    }

    /**
     * Static factory but for more users
     *
     * @param data same format but with \n after each toString()
     * @return array of new users
     */

    static User[] parseUsers(String data) {
        String[] split = data.split("\n");
        return Arrays.stream(split).filter(s ->
                parser.matcher(s).matches()).map(User::parse).toArray(User[]::new);
    }

    String getName();

    int getId();

    Key getSharedKey();

    AlgorithmParameters getAlgorithmParameters();

    String toNetworkFormat();
}
