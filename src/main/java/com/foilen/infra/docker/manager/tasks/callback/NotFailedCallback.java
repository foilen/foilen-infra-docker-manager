/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks.callback;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.db.services.DbService;
import com.foilen.infra.plugin.system.utils.callback.DockerContainerManagementCallback;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;
import com.foilen.smalltools.tools.DateTools;

@Component
public class NotFailedCallback implements DockerContainerManagementCallback {

    @Autowired
    private DbService dbService;

    @Override
    public boolean proceedWithTransformedContainer(String containerName, DockerStateIds dockerStateIds) {
        Date lastFail = DateTools.addDate(Calendar.MINUTE, -10);
        return !dbService.failedFindByContainerNameAndDockerStateIdsAndLastFailBefore(containerName, dockerStateIds, lastFail).isPresent();
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

}