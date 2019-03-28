/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerManagerConfig {

    private String internalDatabasePath;
    private String persistedConfigPath;
    private String imageBuildPath;

    public String getImageBuildPath() {
        return imageBuildPath;
    }

    public String getInternalDatabasePath() {
        return internalDatabasePath;
    }

    public String getPersistedConfigPath() {
        return persistedConfigPath;
    }

    public void setImageBuildPath(String imageBuildPath) {
        this.imageBuildPath = imageBuildPath;
    }

    public void setInternalDatabasePath(String internalDatabasePath) {
        this.internalDatabasePath = internalDatabasePath;
    }

    public void setPersistedConfigPath(String persistedConfigPath) {
        this.persistedConfigPath = persistedConfigPath;
    }

}
