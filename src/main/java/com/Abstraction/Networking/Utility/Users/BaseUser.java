package com.Abstraction.Networking.Utility.Users;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
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

    private final AlgorithmParameterSpec algorithmParameterSpec;

    /**
     * Represent how it should look like
     * Means any letters or digits then space - space and unique id as digits
     */

    public static final Pattern parser = Pattern.compile("(.*) - (\\d+)");


    public BaseUser(String name, int id) {
        this.name = name;
        this.id = id;
        sharedKey = null;
        algorithmParameterSpec = null;
    }

    public BaseUser(String name, int id, Key sharedKey, AlgorithmParameterSpec algorithmParameterSpec) {
        this.name = name;
        this.id = id;
        this.sharedKey = sharedKey;
        this.algorithmParameterSpec = algorithmParameterSpec;
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

    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return algorithmParameterSpec;
    }

    /**
     * DO NOT OVERRIDE
     * MANY FUNCTIONS EXPECT DATA IN THIS FORMAT
     *
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
        return new BaseUser(name, Integer.parseInt(id));
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
