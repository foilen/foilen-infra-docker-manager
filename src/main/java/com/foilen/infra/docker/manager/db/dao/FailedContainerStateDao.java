/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foilen.infra.docker.manager.db.model.FailedContainerState;

public interface FailedContainerStateDao extends JpaRepository<FailedContainerState, String> {

    Optional<FailedContainerState> findByContainerNameAndImageUniqueIdAndContainerRunUniqueIdAndContainerStartedUniqueId( //
            String containerName, //
            String imageUniqueId, String containerRunUniqueId, String containerStartedUniqueId);

}
