/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import java.io.File;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.api.service.InfraApiService;
import com.foilen.infra.docker.manager.services.InfraUiApiClientManagementService;
import com.foilen.infra.docker.manager.services.SystemStatisticsService;
import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;

@Component
public class MonitoringTask extends AbstractBasics {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

    @Autowired
    private InfraUiApiClientManagementService infraUiApiClientManagementService;
    @Autowired
    private SystemStatisticsService systemStatisticsService;

    @Value("${dockerManager.internalDatabasePath}/stats/")
    private File systemStatFolder;
    private String machineName = JavaEnvironmentValues.getHostName();

    @Scheduled(cron = "0 52 0 * * *")
    public void cleanupUnsentStats() {
        try {
            Date expiredBefore = DateTools.addDate(Calendar.WEEK_OF_YEAR, -1);
            DirectoryTools.deleteOlderFilesInDirectory(systemStatFolder, expiredBefore);
        } catch (Throwable e) {
            logger.error("Problem cleaning up the stats", e);
        }
    }

    @PostConstruct
    public void initCreateFolder() {
        // Create path to the storing folder
        DirectoryTools.createPath(systemStatFolder);
    }

    @Scheduled(cron = "15 * * * * *")
    public void saveSystemStat() {

        // Get the stats
        SystemStats systemStats = systemStatisticsService.retrieveSystemStats();

        // Save them
        String systemStatsFileName = systemStatFolder.getAbsolutePath() + File.separator + sdf.format(new Date()) + ".json";
        OutputStream systemStatsOutputStream = FileTools.createStagingFile(systemStatsFileName + ".tmp", systemStatsFileName);
        JsonTools.writeToStream(systemStatsOutputStream, systemStats);
        CloseableTools.close(systemStatsOutputStream);
    }

    // 1 minute
    @Scheduled(fixedRate = 60000L, initialDelay = 60000L)
    public void sendSystemStats() {

        try {
            logger.info("Starting send system stats that are in folder {}", systemStatFolder.getAbsolutePath());

            InfraApiService infraApiService = infraUiApiClientManagementService.getInfraApiService();
            if (infraApiService == null) {
                logger.info("The API service is not configured. Skipping");
                return;
            }

            // Get max 50 files
            List<File> files = Files.list(systemStatFolder.toPath()) //
                    .map(it -> it.toFile()) //
                    .filter(it -> it.isFile()) //
                    .filter(it -> it.getName().endsWith(".json")) //
                    .limit(50) //
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                logger.info("No stats to send");
                return;
            }

            // Get the stats
            List<SystemStats> systemStatsList = new ArrayList<>();
            for (File file : files) {
                try {
                    systemStatsList.add(JsonTools.readFromFile(file, SystemStats.class));
                } catch (Exception e) {
                    logger.warn("There was a problem getting one stat", e);
                }
            }

            // Send them
            infraApiService.getInfraMachineApiService().sendSystemStats(machineName, systemStatsList);

            // Delete them
            for (File file : files) {
                file.delete();
            }

            logger.info("Completed send system stats");

        } catch (Exception e) {
            if (e instanceof ConnectException) {
                logger.error("Problem connecting to config server");
            } else {
                logger.error("Problem sending the system stats", e);
            }
        }
    }

}
