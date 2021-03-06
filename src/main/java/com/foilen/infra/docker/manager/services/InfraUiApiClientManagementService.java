/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import com.foilen.infra.api.model.machine.MachineSetup;
import com.foilen.infra.api.service.InfraApiService;

public interface InfraUiApiClientManagementService {

    /**
     * Get the Infra UI API client if available.
     *
     * @return the client or null if not all the details are available
     */
    InfraApiService getInfraApiService();

    /**
     * Updates the client if there is any change.
     *
     * @param machineSetup
     *            the machine setup
     */
    void updateClientDetailsIfNeeded(MachineSetup machineSetup);

}
