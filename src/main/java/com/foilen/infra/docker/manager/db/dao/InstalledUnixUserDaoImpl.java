/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.smalltools.db.AbstractListSingleJsonFileDao;
import com.foilen.smalltools.tools.StringTools;

@Component
public class InstalledUnixUserDaoImpl extends AbstractListSingleJsonFileDao<String, String> implements InstalledUnixUserDao {

    @Value("${dockerManager.internalDatabasePath}/unixUsers.json")
    private File file;
    @Value("${dockerManager.internalDatabasePath}/unixUsers.json.tmp")
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
    protected Class<String> getType() {
        return String.class;
    }

    @Override
    protected boolean isEntity(String key, String entity) {
        return StringTools.safeEquals(key, entity);
    }

}
