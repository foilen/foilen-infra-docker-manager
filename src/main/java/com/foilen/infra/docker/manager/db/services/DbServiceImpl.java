/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.docker.manager.db.dao.FailedContainerStateDao;
import com.foilen.infra.docker.manager.db.dao.InstalledUnixUserDao;
import com.foilen.infra.docker.manager.db.dao.RedirectEndpointDao;
import com.foilen.infra.docker.manager.db.dao.RunningContainerStateDao;
import com.foilen.infra.docker.manager.db.model.FailedContainerState;
import com.foilen.infra.docker.manager.db.model.InstalledUnixUser;
import com.foilen.infra.docker.manager.db.model.RedirectEndpoint;
import com.foilen.infra.docker.manager.db.model.RunningContainerState;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional
public class DbServiceImpl extends AbstractBasics implements DbService {

    @Autowired
    private FailedContainerStateDao failedContainerStateDao;
    @Autowired
    private InstalledUnixUserDao installedUnixUserDao;
    @Autowired
    private RedirectEndpointDao redirectEndpointDao;
    @Autowired
    private RunningContainerStateDao runningContainerStateDao;

    @Override
    public DockerState dockerStateLoad(MachineSetup machineSetup) {

        DockerState dockerState = new DockerState();

        Sort sortByContainerName = new Sort("containerName");

        runningContainerStateDao.findAll(sortByContainerName).forEach(it -> {

            // runningContainersByName
            dockerState.getRunningContainersByName().put( //
                    it.getContainerName(), //
                    new DockerStateIds(it.getImageUniqueId(), it.getContainerRunUniqueId(), it.getContainerStartedUniqueId()));

            // ipByName
            dockerState.getIpByName().put(it.getContainerName(), it.getIp());
        });

        // failedContainerNames
        failedContainerStateDao.findAll(sortByContainerName).forEach(it -> {
            DockerStateIds dockerStateIds = new DockerStateIds(it.getImageUniqueId(), it.getContainerRunUniqueId(), it.getContainerStartedUniqueId());
            dockerStateIds.setLastState(it.getLastState());

            dockerState.getFailedContainersByName().put(it.getContainerName(), dockerStateIds);
        });

        redirectEndpointDao.findAll(new Sort("machineContainerEndpoint")).forEach(it -> {
            // redirectPortByMachineContainerEndpoint
            dockerState.getRedirectPortByMachineContainerEndpoint().put(it.getMachineContainerEndpoint(), it.getPort());

            // redirectIpByMachineContainerEndpoint
            dockerState.getRedirectIpByMachineContainerEndpoint().put(it.getMachineContainerEndpoint(), it.getIp());
        });

        return dockerState;
    }

    @Override
    public void dockerStateSave(DockerState dockerState) {

        // failedContainerStates
        List<FailedContainerState> failedContainerStates = new ArrayList<>();
        dockerState.getFailedContainersByName().entrySet().forEach(it -> {
            String containerName = it.getKey();
            DockerStateIds dockerStateIds = it.getValue();

            FailedContainerState failedContainerState = new FailedContainerState();
            failedContainerState.setContainerName(containerName);
            failedContainerState.setImageUniqueId(dockerStateIds.getImageUniqueId());
            failedContainerState.setContainerRunUniqueId(dockerStateIds.getContainerRunUniqueId());
            failedContainerState.setContainerStartedUniqueId(dockerStateIds.getContainerStartedUniqueId());
            failedContainerState.setLastState(dockerStateIds.getLastState());

            failedContainerStates.add(failedContainerState);
        });
        failedContainerStateDao.deleteAll();
        failedContainerStateDao.save(failedContainerStates);

        // redirectEndpoints
        Set<String> machineContainerEndpoints = new HashSet<>();
        machineContainerEndpoints.addAll(dockerState.getRedirectPortByMachineContainerEndpoint().keySet());
        machineContainerEndpoints.addAll(dockerState.getRedirectIpByMachineContainerEndpoint().keySet());

        List<RedirectEndpoint> redirectEndpoints = machineContainerEndpoints.stream() //
                .sorted() //
                .map(it -> {
                    RedirectEndpoint redirectEndpoint = new RedirectEndpoint();
                    redirectEndpoint.setMachineContainerEndpoint(it);
                    redirectEndpoint.setIp(dockerState.getRedirectIpByMachineContainerEndpoint().get(it));
                    redirectEndpoint.setPort(dockerState.getRedirectPortByMachineContainerEndpoint().get(it));
                    return redirectEndpoint;
                }) //
                .collect(Collectors.toList());
        redirectEndpointDao.deleteAll();
        redirectEndpointDao.save(redirectEndpoints);

        // runningContainerStates
        List<RunningContainerState> runningContainerStates = new ArrayList<>();
        dockerState.getRunningContainersByName().entrySet().forEach(it -> {
            String containerName = it.getKey();
            DockerStateIds dockerStateIds = it.getValue();

            RunningContainerState runningContainerState = new RunningContainerState();
            runningContainerState.setContainerName(containerName);
            runningContainerState.setImageUniqueId(dockerStateIds.getImageUniqueId());
            runningContainerState.setContainerRunUniqueId(dockerStateIds.getContainerRunUniqueId());
            runningContainerState.setContainerStartedUniqueId(dockerStateIds.getContainerStartedUniqueId());
            runningContainerState.setIp(dockerState.getIpByName().get(containerName));

            runningContainerStates.add(runningContainerState);
        });
        runningContainerStateDao.deleteAll();
        runningContainerStateDao.save(runningContainerStates);

    }

    @Override
    public Optional<FailedContainerState> failedFindBy(String containerName, DockerStateIds dockerStateIds) {
        return failedContainerStateDao.findByContainerNameAndImageUniqueIdAndContainerRunUniqueIdAndContainerStartedUniqueId( //
                containerName, //
                dockerStateIds.getImageUniqueId(), //
                dockerStateIds.getContainerRunUniqueId(), //
                dockerStateIds.getContainerStartedUniqueId());
    }

    @Override
    public void unixUserAdd(String username) {
        InstalledUnixUser installedUnixUser = installedUnixUserDao.findOne(username);
        if (installedUnixUser == null) {
            installedUnixUserDao.save(new InstalledUnixUser(username));
        }
    }

    @Override
    public void unixUserDelete(String username) {
        installedUnixUserDao.delete(username);
    }

    @Override
    public List<String> unixUserFindAllNotNeeded(List<String> neededUnixUsers) {
        return installedUnixUserDao.findAll(new Sort("username")).stream() //
                .map(InstalledUnixUser::getUsername) //
                .filter(it -> !neededUnixUsers.contains(it)) //
                .collect(Collectors.toList());
    }

}
