/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.smalltools.db.AbstractSingleJsonFileDao;

@Component
public class DockerStateDaoImpl extends AbstractSingleJsonFileDao<DockerState> implements DockerStateDao {

    @Value("${dockerManager.internalDatabasePath}/dockerState.json")
    private File file;
    @Value("${dockerManager.internalDatabasePath}/dockerState.json.tmp")
    private File stagingFile;

    @Override
    protected File getFinalFile() {
        return file;
    }

    @Override
    protected File getStagingFile() {
        return stagingFile;
    }

    @Override
    protected Class<DockerState> getType() {
        return DockerState.class;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setStagingFile(File stagingFile) {
        this.stagingFile = stagingFile;
    }

}
