/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foilen.infra.docker.manager.db.model.RunningContainerState;

public interface RunningContainerStateDao extends JpaRepository<RunningContainerState, String> {

}
