/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class GlobalLockService extends AbstractBasics {

    private Lock lock = new ReentrantLock(true);

    public void executeInLock(Runnable runnable) {

        try {
            logger.info("Getting the lock");
            lock.lock();

            logger.info("Got the lock. Starting the execution");
            runnable.run();

        } catch (Throwable e) {
            logger.error("Got an uncaught exception while having the lock", e);
        } finally {
            logger.info("Releasing the lock");
            lock.unlock();
        }

    }

}
