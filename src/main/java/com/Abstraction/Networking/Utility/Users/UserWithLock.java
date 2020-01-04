package com.Abstraction.Networking.Utility.Users;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Add long term lock functionality
 */

public class UserWithLock extends BaseUser {

    /**
     * Work as long term lock
     */

    private final Lock lock;

    public UserWithLock(String name, int id) {
        super(name, id);
        lock = new ReentrantLock();
    }

    public UserWithLock(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters) {
        super(name, id, sharedKey, algorithmParameters);
        lock = new ReentrantLock();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
