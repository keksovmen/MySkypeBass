package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Util.Algorithms;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable
 * <p>
 * Represents a user on any side
 */

public class BaseUser implements Cloneable {


    private final String name;

    /**
     * Supa unique there can't be two with the same id
     */

    private final int id;

    /**
     * Shared secret with server and this dude
     */

    private final Key sharedKey;

    private final AlgorithmParameters algorithmParameters;

    /**
     * Represent how it should look like
     * Means any letters or digits then space - space and unique id as digits
     */

    public static final Pattern parser = Pattern.compile("(.*) - (\\d+)( : ((\\d{1,3} ){16})- ((\\d{1,3} )+))?");


    public BaseUser(String name, int id) {
        this.name = name;
        this.id = id;
        sharedKey = null;
        algorithmParameters = null;
    }

    public BaseUser(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters) {
        this.name = name;
        this.id = id;
        this.sharedKey = sharedKey;
        this.algorithmParameters = algorithmParameters;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Key getSharedKey() {
        if (sharedKey == null)
            throw new NullPointerException("Was created without cipher parameters");
        return sharedKey;
    }

    public AlgorithmParameters getAlgorithmParameters() {
        if (algorithmParameters == null)
            throw new NullPointerException("Was created without cipher parameters");
        return algorithmParameters;
    }

    /**
     * DO NOT OVERRIDE
     * MANY FUNCTIONS EXPECT DATA IN THIS FORMAT
     *
     * @return string representation
     */

    @Override
    public final String toString() {
        String base = name + " - " + id;
        String addition = "";
        try {
            addition = sharedKey == null ? "" :
                    " : " + Algorithms.byteArrayToString(sharedKey.getEncoded()) +
                            "- " + Algorithms.byteArrayToString(algorithmParameters.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
            //won't happen i hope so
        }

        return base + addition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (o instanceof BaseUser) {
            BaseUser baseUser = (BaseUser) o;
            return id == baseUser.id &&
                    name.equals(baseUser.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    /**
     * Static factory from string
     *
     * @param data in format baseUser.toString()
     * @return new user
     */

    public static BaseUser parse(String data) {
        Matcher matcher = parser.matcher(data);
        if (!matcher.matches())
            throw new IllegalArgumentException("Base user is in wrong format - " + data);
        String name = matcher.group(1);
        String id = matcher.group(2);
        String keyAndParam = matcher.group(3);
        if (keyAndParam == null) {
            return new BaseUser(name, Integer.parseInt(id));
        } else {
            Key key = new SecretKeySpec(Algorithms.stringToByteArray(matcher.group(4)), "AES");
            AlgorithmParameters parameters = null;
            try {
                parameters = AlgorithmParameters.getInstance("AES");
                parameters.init(Algorithms.stringToByteArray(matcher.group(6)));
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return new BaseUser(name, Integer.parseInt(id));
            }
            return new BaseUser(name, Integer.parseInt(id), key, parameters);
        }
    }

    /**
     * Static factory but for more users
     *
     * @param data same format but with \n after each toString()
     * @return array of new users
     */

    public static BaseUser[] parseUsers(String data) {
        String[] split = data.split("\n");
        return Arrays.stream(split).map(String::trim).filter(s ->
                BaseUser.parser.matcher(s).matches()).map(BaseUser::parse).toArray(BaseUser[]::new);
    }

}
