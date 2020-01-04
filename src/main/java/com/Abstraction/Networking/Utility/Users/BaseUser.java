package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Cryptographics.Crypto;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
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
     * <p>
     * "Linked together with {@link #algorithmParameters}"
     * if key is null then 100% params too
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
        return sharedKey;
    }

    public AlgorithmParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    /**
     * @return string representation
     */

    @Override
    public final String toString() {
        return name + " - " + id;
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
     * Do not override server sends it and client parse in this format
     * Must look same as {@link #parser}
     *
     * @return this user as a string with full info to be parsed in {@link #parse(String)}
     */

    public final String toNetworkFormat() {
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
        final String name = matcher.group(1);
        final int id = Integer.parseInt(matcher.group(2));
        String keyAndParam = matcher.group(3);
        if (keyAndParam == null) {
            return new BaseUser(name, id);
        } else {
            final Key key = Crypto.createCipherKey(matcher.group(4));
            final AlgorithmParameters parameters = Crypto.createParameters(matcher.group(6));
            if (parameters == null) {
                return new BaseUser(name, id);
            }
            return new BaseUser(name, id, key, parameters);
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
        return Arrays.stream(split).filter(s ->
                BaseUser.parser.matcher(s).matches()).map(BaseUser::parse).toArray(BaseUser[]::new);
    }

}
