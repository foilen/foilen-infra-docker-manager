/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.InfraApiService;
import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.docker.manager.db.services.DbService;
import com.foilen.infra.docker.manager.services.InfraUiApiClientManagementService;
import com.foilen.infra.docker.manager.tasks.callback.NotFailedCallback;
import com.foilen.infra.plugin.system.utils.DockerUtils;
import com.foilen.infra.plugin.system.utils.UnixUsersAndGroupsUtils;
import com.foilen.infra.plugin.system.utils.impl.DockerUtilsImpl;
import com.foilen.infra.plugin.system.utils.impl.UnixUsersAndGroupsUtilsImpl;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.UnixUserDetail;
import com.foilen.infra.plugin.v1.core.base.resources.UnixUser;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.outputter.docker.DockerContainerOutputContext;
import com.foilen.smalltools.TimeoutRunnableHandler;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.tuple.Tuple2;

@Component
public class ApplyStateTask extends AbstractBasics implements Runnable {

    @Autowired
    private DbService dbService;
    @Autowired
    private InfraUiApiClientManagementService infraUiApiClientManagementService;
    @Autowired
    private NotFailedCallback notFailedCallback;

    @Value("${dockerManager.persistedConfigPath}")
    private String persistedConfigPath;
    @Value("${dockerManager.imageBuildPath}")
    private String imageBuildPath;

    private DockerUtils dockerUtils = new DockerUtilsImpl();
    private UnixUsersAndGroupsUtils unixUsersAndGroupsUtils = new UnixUsersAndGroupsUtilsImpl();

    private void applyState(DockerState dockerState, MachineSetup machineSetup) {

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .clear() //
                .setSeparator(" - ") //
                .appendDate() //
                .appendText("Apply State") //
                .change();

        logger.info("Starting to apply the desired setup. Has {} unix users and {} applications", machineSetup.getUnixUsers().size(), machineSetup.getApplications().size());

        long hardTimeoutMs = 10000; // 10s
        hardTimeoutMs += machineSetup.getUnixUsers().size() * 30_000; // 30s per unix user
        hardTimeoutMs += machineSetup.getApplications().size() * 600_000; // 10m per application
        logger.info("Hard timeout set to {} ms. Max {}", hardTimeoutMs, DateTools.formatFull(DateTools.addDate(Calendar.MILLISECOND, (int) hardTimeoutMs)));

        try {
            TimeoutRunnableHandler timeoutRunnableHandler = new TimeoutRunnableHandler(hardTimeoutMs, () -> {

                // Remove unused unix users
                logger.info("Removing unneeded unix users");
                List<String> neededUnixUsers = machineSetup.getUnixUsers().stream() //
                        .map(UnixUser::getName) //
                        .sorted() //
                        .collect(Collectors.toList());
                List<String> toDeleteUnixUsers = dbService.unixUserFindAllNotNeeded(neededUnixUsers);
                for (String toDeleteUnixUser : toDeleteUnixUsers) {
                    logger.info("UnixUser: {}", toDeleteUnixUser);
                    if (unixUsersAndGroupsUtils.userRemove(toDeleteUnixUser)) {
                        dbService.unixUserDelete(toDeleteUnixUser);
                    } else {
                        logger.error("UnixUser: {} was not deleted succesfully", toDeleteUnixUser);
                    }
                }

                // Install unix users
                logger.info("Installing unix users");

                for (UnixUser unixUser : machineSetup.getUnixUsers()) {

                    String username = unixUser.getName();
                    logger.info("UnixUser: {}", username);
                    UnixUserDetail existingUser = unixUsersAndGroupsUtils.userGet(username);
                    if (existingUser == null) {
                        // Create
                        logger.info("UnixUser: {} does not exist. Create it", username);
                        if (unixUsersAndGroupsUtils.userCreate(username, unixUser.getId(), unixUser.getHomeFolder(), null, null)) {
                            dbService.unixUserAdd(username);
                        } else {
                            logger.error("UnixUser: {} was not created succesfully", username);
                        }
                    } else {
                        // Update if needed
                        if (!StringTools.safeEquals(unixUser.getHomeFolder(), existingUser.getHomeFolder())) {
                            unixUsersAndGroupsUtils.userHomeUpdate(username, unixUser.getHomeFolder());
                        }
                        if (!StringTools.safeEquals(unixUser.getHashedPassword(), existingUser.getHashedPassword())) {
                            unixUsersAndGroupsUtils.userPasswordUpdate(username, unixUser.getHashedPassword());
                        }
                        if (!StringTools.safeEquals(unixUser.getShell(), existingUser.getShell())) {
                            unixUsersAndGroupsUtils.userShellUpdate(username, unixUser.getShell());
                        }
                    }

                }

                // Install applications
                logger.info("Install application");
                List<Tuple2<DockerContainerOutputContext, IPApplicationDefinition>> outputContextAndApplicationDefinitions = machineSetup.getApplications().stream() //
                        .map(application -> {
                            String applicationName = application.getName();
                            String buildDirectory = imageBuildPath + "/" + applicationName + "/";
                            DirectoryTools.deleteFolder(buildDirectory);

                            return new Tuple2<>(new DockerContainerOutputContext(applicationName, applicationName, applicationName, buildDirectory), application.getApplicationDefinition());
                        }).collect(Collectors.toList());

                dockerUtils.containersManage(dockerState, outputContextAndApplicationDefinitions, notFailedCallback);
            });
            timeoutRunnableHandler.run();
        } catch (Exception e) {
            logger.error("There was an unexpected exception while applying the machine's setup", e);
        }

        threadNameStateTool.revert();
    }

    @Override
    public void run() {

        // Create image build directory if needed
        DirectoryTools.createPath(imageBuildPath);

        // Get the expected configuration from disk
        String machineSetupFile = persistedConfigPath + "/machineSetup.json";
        if (!FileTools.exists(machineSetupFile)) {
            logger.error("You need the file {} to be able to do anything", machineSetupFile);
            return;
        }
        MachineSetup machineSetup = JsonTools.readFromFile(machineSetupFile, MachineSetup.class);
        infraUiApiClientManagementService.updateClientDetailsIfNeeded(machineSetup);

        // Get the previous DockerState
        DockerState dockerState = dbService.dockerStateLoad(machineSetup);
        updateRedirectionDetails(machineSetup, dockerState);

        // Go to the expected state
        logger.info("First run from persisted setup");
        applyState(dockerState, machineSetup);
        dbService.dockerStateSave(dockerState);

        // If InfraUi details not set, quit as a single run
        if (!CollectionsTools.isAllItemNotNullOrEmpty(machineSetup.getUiApiBaseUrl(), machineSetup.getUiApiUserId(), machineSetup.getUiApiUserKey())) {
            logger.info("The UI API details are not set or fully set. Quitting as a single run");
            return;
        }

        while (true) {
            try {
                // Loop every minute
                logger.info("Sleep 1 minute");
                ThreadTools.sleep(60000);

                // Get the configuration from the InfraUi
                InfraApiService infraApiService = infraUiApiClientManagementService.getInfraApiService();
                if (infraApiService == null) {
                    continue;
                }
                machineSetup = infraApiService.getInfraMachineApiService().getMachineSetup(machineSetup.getMachineName());
                infraUiApiClientManagementService.updateClientDetailsIfNeeded(machineSetup);
                updateRedirectionDetails(machineSetup, dockerState);
                JsonTools.writeToFile(machineSetupFile + "-tmp", machineSetup);
                FileTools.deleteFile(machineSetupFile);
                new File(machineSetupFile + "-tmp").renameTo(new File(machineSetupFile));

                // Go to the expected state
                logger.info("Apply state");
                applyState(dockerState, machineSetup);
                dbService.dockerStateSave(dockerState);
            } catch (Exception e) {
                logger.error("There was an unexpected exception while looping", e);
            }
        }

    }

    @PostConstruct
    public void startThread() {
        new Thread(this).start();
    }

    private void updateRedirectionDetails(MachineSetup machineSetup, DockerState dockerState) {
        dockerState.setRedirectorBridgePort(machineSetup.getRedirectorBridgePort());
        dockerState.setRedirectorCaCerts(machineSetup.getRedirectorCaCerts());
        dockerState.setRedirectorNodeCert(machineSetup.getRedirectorNodeCert());
        dockerState.setRedirectorNodeKey(machineSetup.getRedirectorNodeKey());
    }

}
