/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.machine.MachineSetup;
import com.foilen.infra.api.service.InfraApiService;
import com.foilen.infra.api.service.InfraApiServiceImpl;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.StringTools;

@Component
public class InfraUiApiClientManagementServiceImpl extends AbstractBasics implements InfraUiApiClientManagementService {

    private String infraBaseUrl;
    private String apiUser;
    private String apiKey;
    private String cert;

    private InfraApiService infraApiService;

    @Override
    public InfraApiService getInfraApiService() {
        return infraApiService;
    }

    @Override
    public void updateClientDetailsIfNeeded(MachineSetup machineSetup) {

        boolean changed = //
                !StringTools.safeEquals(infraBaseUrl, machineSetup.getUiApiBaseUrl()) || //
                        !StringTools.safeEquals(apiUser, machineSetup.getUiApiUserId()) || //
                        (machineSetup.getUiApiUserKey() != null && !StringTools.safeEquals(apiKey, machineSetup.getUiApiUserKey())) || //
                        !StringTools.safeEquals(cert, machineSetup.getUiApiCert()) //
        ;
        if (changed) {
            infraBaseUrl = machineSetup.getUiApiBaseUrl();
            apiUser = machineSetup.getUiApiUserId();
            if (machineSetup.getUiApiUserKey() != null) {
                apiKey = machineSetup.getUiApiUserKey();
            }
            cert = machineSetup.getUiApiCert();

            if (CollectionsTools.isAllItemNotNullOrEmpty(infraBaseUrl, apiUser, apiKey)) {
                infraApiService = new InfraApiServiceImpl(infraBaseUrl, apiUser, apiKey, cert);
            } else {
                infraApiService = null;
            }
        }

    }

}
