/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import com.foilen.infra.plugin.system.utils.model.DockerState;

public interface DockerStateDao {

    DockerState load();

    void save(DockerState dockerState);

}
