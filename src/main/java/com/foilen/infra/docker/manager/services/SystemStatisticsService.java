/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import com.foilen.infra.api.model.SystemStats;

/**
 * To retrieve all the statistics from the system.
 */
public interface SystemStatisticsService {

    /**
     * Get the global system statistics. (they are relative to the last call for some values)
     *
     * @return the system statistics
     */
    SystemStats retrieveSystemStats();

}
