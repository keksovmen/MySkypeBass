package com.Abstraction.Networking.Utility.Users;

public interface UserWithLock extends User {

    void lock();

    void unlock();
}
