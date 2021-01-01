/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao;

import java.util.List;

public interface InstalledUnixUserDao {

    void add(String username);

    boolean delete(String username);

    List<String> findAllAsList();

}
