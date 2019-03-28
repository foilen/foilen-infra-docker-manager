/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foilen.infra.docker.manager.db.dao.DockerStateDao;
import com.foilen.infra.docker.manager.db.dao.InstalledUnixUserDao;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.DockerStateFailed;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
public class DbServiceImpl extends AbstractBasics implements DbService {

    @Autowired
    private DockerStateDao dockerStateDao;
    @Autowired
    private InstalledUnixUserDao installedUnixUserDao;

    @Override
    public DockerState dockerStateLoad() {
        return dockerStateDao.load();
    }

    @Override
    public void dockerStateSave(DockerState dockerState) {
        dockerStateDao.save(dockerState);
    }

    @Override
    public Optional<DockerStateFailed> failedFindByContainerNameAndDockerStateIdsAndLastFailBefore(String containerName, DockerStateIds dockerStateIds, Date lastFailBefore) {
        DockerStateFailed dockerStateFailed = dockerStateDao.load().getFailedContainersByName().get(containerName);
        logger.info("Failed container state for [{}] : {}", containerName, dockerStateFailed);
        if (dockerStateFailed == null || //
                !dockerStateIds.idsEquals(dockerStateFailed.getDockerStateIds()) || //
                dockerStateFailed.getLastFail().getTime() < lastFailBefore.getTime()) {
            return Optional.empty();
        }
        return Optional.of(dockerStateFailed);
    }

    @Override
    public void unixUserAdd(String username) {
        installedUnixUserDao.add(username);
    }

    @Override
    public void unixUserDelete(String username) {
        installedUnixUserDao.delete(username);
    }

    @Override
    public List<String> unixUserFindAllNotNeeded(List<String> neededUnixUsers) {
        List<String> notNeeded = new ArrayList<>();
        notNeeded.addAll(installedUnixUserDao.findAllAsList());
        notNeeded.removeAll(neededUnixUsers);
        return notNeeded;
    }

}
