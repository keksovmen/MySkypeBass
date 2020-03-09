package com.Abstraction.Networking.Utility.Users;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BaseUserWithLock implements UserWithLock {

    private final User user;
    private final Lock lock;

    public BaseUserWithLock(User user) {
        this.user = user;
        lock = new ReentrantLock();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public int getId() {
        return user.getId();
    }

    @Override
    public Key getSharedKey() {
        return user.getSharedKey();
    }

    @Override
    public AlgorithmParameters getAlgorithmParameters() {
        return user.getAlgorithmParameters();
    }

    @Override
    public String toNetworkFormat() {
        return user.toNetworkFormat();
    }

    @Override
    public String toString() {
        return user.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return user.equals(obj);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
