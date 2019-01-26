/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.Application;
import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.api.model.UnixUser;
import com.foilen.infra.api.response.ResponseMachineSetup;
import com.foilen.infra.api.service.InfraApiService;
import com.foilen.infra.docker.manager.db.services.DbService;
import com.foilen.infra.docker.manager.services.InfraUiApiClientManagementService;
import com.foilen.infra.docker.manager.tasks.callback.NotFailedCallback;
import com.foilen.infra.docker.manager.tasks.callback.SaveTransformedApplicationDefinitionCallback;
import com.foilen.infra.plugin.system.utils.DockerUtils;
import com.foilen.infra.plugin.system.utils.UnixUsersAndGroupsUtils;
import com.foilen.infra.plugin.system.utils.impl.DockerUtilsImpl;
import com.foilen.infra.plugin.system.utils.impl.UnixUsersAndGroupsUtilsImpl;
import com.foilen.infra.plugin.system.utils.model.ContainersManageContext;
import com.foilen.infra.plugin.system.utils.model.CronApplicationBuildDetails;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.UnixUserDetail;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionService;
import com.foilen.infra.plugin.v1.model.outputter.docker.DockerContainerOutputContext;
import com.foilen.smalltools.TimeoutRunnableHandler;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.SystemTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.tuple.Tuple3;

@Component
public class ApplyStateTask extends AbstractBasics implements Runnable {

    @Autowired
    private DbService dbService;
    @Autowired
    private InfraUiApiClientManagementService infraUiApiClientManagementService;
    @Autowired
    private NotFailedCallback notFailedCallback;
    @Autowired
    private SaveTransformedApplicationDefinitionCallback saveTransformedApplicationDefinitionCallback;

    @Value("${dockerManager.persistedConfigPath}")
    private String persistedConfigPath;
    @Value("${dockerManager.imageBuildPath}")
    private String imageBuildPath;

    private String hostFs = SystemTools.getPropertyOrEnvironment("HOSTFS", "/");

    private DockerUtils dockerUtils = new DockerUtilsImpl();
    private UnixUsersAndGroupsUtils unixUsersAndGroupsUtils = new UnixUsersAndGroupsUtilsImpl();

    private void addIfIdSet(Map<Long, List<String>> applicationNamesByUnixUserId, Long unixUserId, String applicationName) {
        if (unixUserId != null) {
            List<String> applicationNames = CollectionsTools.getOrCreateEmptyArrayList(applicationNamesByUnixUserId, unixUserId, String.class);
            if (!applicationNames.contains(applicationName)) {
                applicationNames.add(applicationName);
            }
        }
    }

    private void applyState(DockerState dockerState, MachineSetup machineSetup) {

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .clear() //
                .setSeparator(" - ") //
                .appendDate() //
                .appendText("Apply State") //
                .change();

        logger.info("Starting to apply the desired setup. Has {} unix users and {} applications", machineSetup.getUnixUsers().size(), machineSetup.getApplications().size());

        // Create the network if needed
        dockerUtils.networkCreateIfNotExists(DockerUtilsImpl.NETWORK_NAME, "172.20.0.0/16");

        long hardTimeoutMs = 10000; // 10s
        hardTimeoutMs += machineSetup.getUnixUsers().size() * 30_000; // 30s per unix user
        hardTimeoutMs += machineSetup.getApplications().size() * 600_000; // 10m per application
        logger.info("Hard timeout set to {} ms. Max {}", hardTimeoutMs, DateTools.formatFull(DateTools.addDate(Calendar.MILLISECOND, (int) hardTimeoutMs)));
        ThreadNameStateTool currentThreadNameStateTool = ThreadTools.nameThread();
        try {
            TimeoutRunnableHandler timeoutRunnableHandler = new TimeoutRunnableHandler(hardTimeoutMs, () -> {

                currentThreadNameStateTool.change();

                // Remove unused unix users
                logger.info("Removing unneeded unix users");
                List<String> neededUnixUsers = machineSetup.getUnixUsers().stream() //
                        .map(UnixUser::getName) //
                        .sorted() //
                        .collect(Collectors.toList());
                List<String> toDeleteUnixUsers = dbService.unixUserFindAllNotNeeded(neededUnixUsers);
                for (String toDeleteUnixUser : toDeleteUnixUsers) {
                    logger.info("UnixUser to delete: {}", toDeleteUnixUser);
                    UnixUserDetail toDeleteUnixUserDetails = unixUsersAndGroupsUtils.userGet(toDeleteUnixUser);
                    if (toDeleteUnixUserDetails == null || unixUsersAndGroupsUtils.userRemove(toDeleteUnixUserDetails.getName(), toDeleteUnixUserDetails.getHomeFolder())) {
                        dbService.unixUserDelete(toDeleteUnixUser);
                    } else {
                        logger.error("UnixUser: {} was not deleted succesfully", toDeleteUnixUser);
                    }
                }

                // Install unix users
                logger.info("Installing unix users");
                Map<Long, String> unixUserNameById = new HashMap<>();
                for (UnixUser unixUser : machineSetup.getUnixUsers()) {

                    String username = unixUser.getName();
                    logger.info("UnixUser: {}", username);
                    unixUserNameById.put(unixUser.getId(), username);
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
                logger.info("Installing applications");
                ContainersManageContext containersManageContext = new ContainersManageContext();
                containersManageContext.setDockerState(dockerState);
                containersManageContext.setContainerManagementCallback(notFailedCallback);
                containersManageContext.setTransformedApplicationDefinitionCallback(saveTransformedApplicationDefinitionCallback);
                Map<Long, List<String>> applicationNamesByUnixUserId = new HashMap<>();
                List<Tuple3<String, String, Integer>> appNameEndpointNameAndPorts = new ArrayList<>();
                for (Application application : machineSetup.getApplications()) {
                    String applicationName = application.getName();
                    String buildDirectory = imageBuildPath + "/" + applicationName + "/";
                    DirectoryTools.deleteFolder(buildDirectory);

                    // Associate the application name to all the running users (for docker-sudo)
                    addIfIdSet(applicationNamesByUnixUserId, application.getApplicationDefinition().getRunAs(), applicationName);
                    for (IPApplicationDefinitionService service : application.getApplicationDefinition().getServices()) {
                        addIfIdSet(applicationNamesByUnixUserId, service.getRunAs(), applicationName);
                    }

                    // Add applications details
                    CronApplicationBuildDetails applicationBuildDetails = new CronApplicationBuildDetails();
                    applicationBuildDetails.setApplicationDefinition(application.getApplicationDefinition());
                    DockerContainerOutputContext outputContext = new DockerContainerOutputContext(applicationName, applicationName, applicationName, buildDirectory);
                    applicationBuildDetails.setOutputContext(outputContext);
                    outputContext.setDockerLogsMaxSizeMB(100);
                    switch (application.getExecutionPolicy()) {
                    case ALWAYS_ON:
                        containersManageContext.getAlwaysRunningApplications().add(applicationBuildDetails);
                        break;
                    case CRON:
                        applicationBuildDetails.setCronTime(application.getExecutionCronDetails());
                        containersManageContext.getCronApplications().add(applicationBuildDetails);
                        break;
                    }

                    // Keep the exposed ports
                    application.getApplicationDefinition().getPortsEndpoint().forEach((port, endpoint) -> {
                        appNameEndpointNameAndPorts.add(new Tuple3<>(applicationName, endpoint, port));
                    });
                }

                dockerUtils.containersManage(containersManageContext);

                // Install docker-sudo configuration
                logger.info("Install docker-sudo configuration");
                String dockerSudoConfPath = hostFs + "/etc/docker-sudo/";
                DirectoryTools.createPath(dockerSudoConfPath, "root", "root", "755");
                List<String> filesToRemove = new ArrayList<>();
                for (String file : new File(dockerSudoConfPath).list()) {
                    filesToRemove.add(file);
                    filesToRemove.remove("images");
                }

                for (Long unixUserId : applicationNamesByUnixUserId.keySet()) {
                    String unixUserName = unixUserNameById.get(unixUserId);
                    if (unixUserName == null) {
                        continue;
                    }

                    // Get the config file
                    String configFileName = "containers-" + unixUserName + ".conf";
                    String fullConfigPath = dockerSudoConfPath + configFileName;
                    filesToRemove.remove(configFileName);

                    // Write information in it
                    List<String> applicationNames = applicationNamesByUnixUserId.get(unixUserId);
                    Collections.sort(applicationNames);
                    logger.info("Saving {} with {} entries", fullConfigPath, applicationNames.size());
                    FileTools.writeFileWithContentCheck(fullConfigPath, applicationNames);
                    FileTools.changePermissions(fullConfigPath, false, "644");
                }

                for (String fileToRemove : filesToRemove) {
                    String fullConfigPath = dockerSudoConfPath + fileToRemove;
                    logger.info("Deleting extra file {}", fullConfigPath);
                    FileTools.deleteFile(fullConfigPath);
                }

                // Install docker-sudo images
                logger.info("Install docker-sudo images");
                String dockerSudoImagesPath = hostFs + "/etc/docker-sudo/images/";
                DirectoryTools.createPath(dockerSudoImagesPath, "root", "root", "755");
                List<Tuple2<String, String>> dockerSudoImages = Arrays.asList( //
                        new Tuple2<>("mariadb", "mariadb:10.3") //
                );
                filesToRemove = new ArrayList<>();
                for (String file : new File(dockerSudoImagesPath).list()) {
                    filesToRemove.add(file);
                }
                for (Tuple2<String, String> image : dockerSudoImages) {
                    logger.info("Installing image {} using {}", image.getA(), image.getB());
                    filesToRemove.remove(image.getA());

                    String fullImagePath = dockerSudoImagesPath + image.getA();
                    DirectoryTools.createPath(fullImagePath, "root", "root", "755");
                    FileTools.writeFile("FROM " + image.getB() + "\n", new File(fullImagePath + "/Dockerfile"), "root", "root", "644");
                }

                for (String fileToRemove : filesToRemove) {
                    String fullImagePath = dockerSudoImagesPath + fileToRemove;
                    logger.info("Deleting extra directory {}", fullImagePath);
                    DirectoryTools.deleteFolder(fullImagePath);
                }

                // Registry of all exposed services
                logger.info("Keep a listing of the applications endpoints. Amount: {}", appNameEndpointNameAndPorts.size());
                filesToRemove.clear();
                String applicationEndpointsPath = hostFs + "/var/infra-endpoints/";
                DirectoryTools.createPath(applicationEndpointsPath, "root", "root", "755");
                for (String file : new File(applicationEndpointsPath).list()) {
                    filesToRemove.add(file);
                }

                for (Tuple3<String, String, Integer> appNameEndpointNameAndPort : appNameEndpointNameAndPorts) {
                    String applicationName = appNameEndpointNameAndPort.getA();
                    String endpoint = appNameEndpointNameAndPort.getB();
                    int port = appNameEndpointNameAndPort.getC();
                    logger.info("Processing {}/{}/{}", applicationName, endpoint, port);
                    String ip = dockerState.getIpStateByName().get(applicationName).getIp();
                    if (ip == null) {
                        logger.info("Skipping {}/{}/{} because we do not know the IP of the app (most likely not running)");
                        continue;
                    }

                    // Get the config file
                    String configFileName = applicationName + "_" + endpoint;
                    String fullConfigPath = applicationEndpointsPath + configFileName;
                    filesToRemove.remove(configFileName);

                    // Write information in it
                    String hostPort = ip + ":" + port;
                    logger.info("Saving {} with content {}", fullConfigPath, hostPort);
                    FileTools.writeFileWithContentCheck(fullConfigPath, hostPort);
                    FileTools.changePermissions(fullConfigPath, false, "644");
                }

                for (String fileToRemove : filesToRemove) {
                    String fullConfigPath = applicationEndpointsPath + fileToRemove;
                    logger.info("Deleting extra file {}", fullConfigPath);
                    FileTools.deleteFile(fullConfigPath);
                }

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
        DockerState dockerState = dbService.dockerStateLoad();
        updateRedirectionDetails(machineSetup, dockerState);

        // Go to the expected state
        logger.info("First run from persisted setup");
        applyState(dockerState, machineSetup);
        dbService.dockerStateSave(dockerState);

        // If InfraUi details not set, quit as a single run
        if (!CollectionsTools.isAllItemNotNullOrEmpty(machineSetup.getUiApiBaseUrl(), machineSetup.getUiApiUserId(), machineSetup.getUiApiUserKey())) {
            logger.info("The UI API details are not set or are not fully set. Quitting as a single run");
            System.exit(0);
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

                try {
                    ResponseMachineSetup responseMachineSetup = infraApiService.getInfraMachineApiService().getMachineSetup(machineSetup.getMachineName());
                    if (responseMachineSetup.isSuccess()) {
                        machineSetup = responseMachineSetup.getItem();
                    } else {
                        logger.error("Could not retrieve the machine setup. Will use persisted one. Error: {}", responseMachineSetup.getError());
                    }
                } catch (Exception e) {
                    logger.error("Could not retrieve the machine setup. Will use persisted one.", e);
                }

                infraUiApiClientManagementService.updateClientDetailsIfNeeded(machineSetup);
                updateRedirectionDetails(machineSetup, dockerState);
                if (machineSetup != null) {
                    JsonTools.writeToFile(machineSetupFile + "-tmp", machineSetup);
                    FileTools.deleteFile(machineSetupFile);
                    new File(machineSetupFile + "-tmp").renameTo(new File(machineSetupFile));
                }

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
