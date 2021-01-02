/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager;

public class DockerManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DockerManagerException(String message) {
        super(message);
    }

    public DockerManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
