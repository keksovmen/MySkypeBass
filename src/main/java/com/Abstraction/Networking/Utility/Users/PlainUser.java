package com.Abstraction.Networking.Utility.Users;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.Objects;

public class PlainUser implements User {


    private final String name;

    /**
     * Supa unique there can't be two with the same id
     */

    private final int id;

    public PlainUser(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Key getSharedKey() {
        throw new UnsupportedOperationException("It's a plain user, only name and ID");
    }

    @Override
    public AlgorithmParameters getAlgorithmParameters() {
        throw new UnsupportedOperationException("It's a plain user, only name and ID");
    }

    @Override
    public String toNetworkFormat() {
        return name + " - " + id;
    }

    @Override
    public String toString() {
        return name + " - " + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof User) {
            User baseUser = (User) obj;
            return id == baseUser.getId() &&
                    name.equals(baseUser.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
