/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.db.dao.model.Skipping;
import com.foilen.smalltools.db.AbstractSingleJsonFileDao;

@Component
public class SkippingDaoImpl extends AbstractSingleJsonFileDao<Skipping> implements SkippingDao {

    @Value("${dockerManager.internalDatabasePath}/skipping.json")
    private File file;
    @Value("${dockerManager.internalDatabasePath}/skipping.json.tmp")
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
    protected Class<Skipping> getType() {
        return Skipping.class;
    }

}
