package com.dp.service;

public interface ILock {

    boolean tryLock(long timeoutSec);

    void unlock();

}
