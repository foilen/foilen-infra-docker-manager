/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.services.GlobalLockService;
import com.foilen.smalltools.consolerunner.ConsoleRunner;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class CleanupDockerTask extends AbstractBasics {

    @Autowired
    private GlobalLockService globalLockService;

    // Once per 2 hours
    @Scheduled(cron = "33 33 */2 * * *")
    public void cleanupDocker() {

        globalLockService.executeInLock(() -> {

            try {
                logger.info("Cleaning up docker");

                ConsoleRunner consoleRunner = new ConsoleRunner();
                consoleRunner.setCommand("/usr/bin/docker");
                consoleRunner.addArguments("system", "prune", "-a", "-f");
                consoleRunner.setRedirectErrorStream(true);
                int exitCode = consoleRunner.executeWithLogger(logger, Level.INFO);
                if (exitCode != 0) {
                    logger.error("Problem cleaning up docker. Exit code: {}", exitCode);
                }

            } catch (Throwable e) {
                logger.error("Problem cleaning up the stats", e);
            } finally {
                logger.info("Cleaning up docker completed");
            }

        });
    }

}
