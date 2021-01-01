/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.DockerStateFailed;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;

public interface DbService {

    DockerState dockerStateLoad();

    void dockerStateSave(DockerState dockerState);

    Optional<DockerStateFailed> failedFindByContainerNameAndDockerStateIdsAndLastFailBefore(String containerName, DockerStateIds dockerStateIds, Date lastFailBefore);

    boolean isExplicitlySkipped(String containerName);

    void unixUserAdd(String username);

    void unixUserDelete(String username);

    List<String> unixUserFindAllNotNeeded(List<String> neededUnixUsers);

}
