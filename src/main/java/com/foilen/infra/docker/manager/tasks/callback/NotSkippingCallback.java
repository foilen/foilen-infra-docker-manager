/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.db.services.DbService;
import com.foilen.infra.plugin.system.utils.callback.DockerContainerManagementCallback;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class NotSkippingCallback extends AbstractBasics implements DockerContainerManagementCallback {

    @Autowired
    private DbService dbService;

    @Override
    public boolean proceedWithTransformedContainer(String containerName, DockerStateIds dockerStateIds) {
        boolean explicitlySkipped = dbService.isExplicitlySkipped(containerName);
        if (explicitlySkipped) {
            logger.info("Explicitly skipping container {}", containerName);
        }
        return !explicitlySkipped;
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

}