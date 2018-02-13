/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.foilen.smalltools.tools.AbstractBasics;

@Entity
public class RunningContainerState extends AbstractBasics {

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

    // Details
    @Column(length = 100)
    private String ip;

    public RunningContainerState() {
    }

    public RunningContainerState(String containerName, String imageUniqueId, String containerRunUniqueId, String containerStartedUniqueId, String ip) {
        this.containerName = containerName;
        this.imageUniqueId = imageUniqueId;
        this.containerRunUniqueId = containerRunUniqueId;
        this.containerStartedUniqueId = containerStartedUniqueId;
        this.ip = ip;
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

    public String getIp() {
        return ip;
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

    public void setIp(String ip) {
        this.ip = ip;
    }

}
