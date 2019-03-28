/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import java.io.File;
import java.net.ConnectException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.service.InfraApiService;
import com.foilen.infra.docker.manager.model.Alert;
import com.foilen.infra.docker.manager.services.InfraUiApiClientManagementService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.JsonTools;

@Component
public class AlertingTask extends AbstractBasics {

    @Autowired
    private InfraUiApiClientManagementService infraUiApiClientManagementService;

    @Value("${dockerManager.internalDatabasePath}/alerts/")
    private File alertDirectory;

    @Scheduled(cron = "0 52 0 * * *")
    public void cleanupUnsentAlert() {
        try {
            Date expiredBefore = DateTools.addDate(Calendar.WEEK_OF_YEAR, -1);
            DirectoryTools.deleteOlderFilesInDirectory(alertDirectory, expiredBefore);
        } catch (Throwable e) {
            logger.error("Problem cleaning up the alerts", e);
        }
    }

    @PostConstruct
    public void initCreateFolder() {
        // Create path to the storing directory
        DirectoryTools.createPath(alertDirectory);
    }

    // 5 seconds
    @Scheduled(fixedDelay = 5000L)
    public void sendSystemStats() {

        try {
            InfraApiService infraApiService = infraUiApiClientManagementService.getInfraApiService();
            if (infraApiService == null) {
                return;
            }

            // Process max 50 files
            Files.list(alertDirectory.toPath()) //
                    .map(it -> it.toFile()) //
                    .filter(it -> it.isFile()) //
                    .filter(it -> it.getName().endsWith(".json")) //
                    .limit(50) //
                    .forEach(file -> {
                        // Get it
                        Alert alert;
                        try {
                            alert = JsonTools.readFromFile(file, Alert.class);
                        } catch (Exception e) {
                            logger.warn("There was a problem getting one alert", e);
                            return;
                        }

                        // Send it
                        logger.info("Sending alert {}", alert);
                        infraApiService.getInfraAlertApiService().sendAlert(alert.getSubjet(), alert.getContent());

                        // Delete it
                        file.delete();
                    });

        } catch (Exception e) {
            if (e instanceof ConnectException) {
                logger.error("Problem connecting to config server");
            } else {
                logger.error("Problem sending the alerts", e);
            }
        }
    }

}
