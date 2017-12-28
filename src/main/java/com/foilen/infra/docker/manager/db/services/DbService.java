/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.services;

import java.util.List;
import java.util.Optional;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.docker.manager.db.model.FailedContainerState;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;

public interface DbService {

    DockerState dockerStateLoad(MachineSetup machineSetup);

    void dockerStateSave(DockerState dockerState);

    Optional<FailedContainerState> failedFindBy(String containerName, DockerStateIds dockerStateIds);

    void unixUserAdd(String username);

    void unixUserDelete(String username);

    List<String> unixUserFindAllNotNeeded(List<String> neededUnixUsers);

}
