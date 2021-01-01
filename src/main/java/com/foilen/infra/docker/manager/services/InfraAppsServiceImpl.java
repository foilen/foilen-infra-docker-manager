/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.DockerManagerException;
import com.foilen.smalltools.hash.HashMd5sum;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.SystemTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
import com.google.common.io.Files;

@Component
public class InfraAppsServiceImpl extends AbstractBasics implements InfraAppsService {

    private static final String INFRA_APPLICATION_PATH = SystemTools.getPropertyOrEnvironment("HOSTFS", "/") + "var/infra-apps/";

    private Map<String, String> finalNameByPath = new ConcurrentHashMap<>();

    @Override
    public void copy(String sourceFullPath) {

        // Prepare names
        File sourceFile = new File(sourceFullPath);
        String sourceHash = HashMd5sum.hashFile(sourceFile);
        String targetName = sourceFile.getName() + "-" + sourceHash;
        finalNameByPath.put(sourceFile.getName(), targetName);

        // Copy if target doesn't exist
        File targetFile = new File(INFRA_APPLICATION_PATH + File.separatorChar + targetName);
        logger.info("Copy {} to {}", sourceFullPath, targetFile.getAbsolutePath());
        if (targetFile.exists()) {
            logger.info("{} already exists. Skipping", targetFile.getAbsolutePath());
        } else {
            logger.info("{} does not exist. Copy", targetFile.getAbsolutePath());
            try {
                Files.copy(sourceFile, targetFile);
                FileTools.changePermissions(targetFile.getAbsolutePath(), false, "755");
            } catch (IOException e) {
                throw new DockerManagerException("Could not copy the file", e);
            }
        }

    }

    @PostConstruct
    public void copyFiles() {

        DirectoryTools.createPath(INFRA_APPLICATION_PATH, "root", "root", "755");

        copy("/usr/sbin/haproxy");
        copy("/usr/sbin/services-execution");

        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {

            ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                    .clear() //
                    .appendText("Delete Old Infra Apps") //
                    .change();

            boolean completed = false;
            while (!completed) {
                try {
                    logger.info("Try to delete old infra apps");

                    List<String> allFiles = DirectoryTools.listFilesAndFoldersRecursively(INFRA_APPLICATION_PATH, true);
                    for (String fullFileName : allFiles) {
                        File file = new File(fullFileName);
                        String onlyName = file.getName();
                        if (finalNameByPath.containsValue(onlyName)) {
                            logger.info("Keeping {}", onlyName);
                            continue;
                        }

                        logger.info("Deleting {}", fullFileName);
                        AssertTools.assertTrue(file.delete(), "The file " + fullFileName + " was not deleted");
                    }

                    completed = true;
                } catch (Exception e) {
                    logger.info("Could not delete all infra apps", e);

                    logger.info("Waiting 1 minute");
                    ThreadTools.sleep(60000);
                }

            }

            logger.info("Deletion of old infra apps completed");
            threadNameStateTool.revert();

        });
    }

    @Override
    public String getName(String sourceName) {
        return finalNameByPath.get(sourceName);
    }

}
