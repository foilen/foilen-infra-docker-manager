/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.foilen.infra.plugin.system.utils.model.DockerStep;
import com.foilen.smalltools.tools.AbstractBasics;

@Entity
public class FailedContainerState extends AbstractBasics {

    @Id
    @Column(nullable = false, unique = true)
    private String containerName;

    // Ids
    @Column(length = 100)
    private String imageUniqueId;
    @Column(length = 100)
    private String containerRunUniqueId;
    @Column(length = 100)
    private String containerStartedUniqueId;

    // State
    @Enumerated(EnumType.STRING)
    private DockerStep lastState = DockerStep.BUILD_IMAGE;

    public FailedContainerState() {
    }

    public String getContainerName() {
        return containerName;
    }

    public String getContainerRunUniqueId() {
        return containerRunUniqueId;
    }

    public String getContainerStartedUniqueId() {
        return containerStartedUniqueId;
    }

    public String getImageUniqueId() {
        return imageUniqueId;
    }

    public DockerStep getLastState() {
        return lastState;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setContainerRunUniqueId(String containerRunUniqueId) {
        this.containerRunUniqueId = containerRunUniqueId;
    }

    public void setContainerStartedUniqueId(String containerStartedUniqueId) {
        this.containerStartedUniqueId = containerStartedUniqueId;
    }

    public void setImageUniqueId(String imageUniqueId) {
        this.imageUniqueId = imageUniqueId;
    }

    public void setLastState(DockerStep lastState) {
        this.lastState = lastState;
    }

}
