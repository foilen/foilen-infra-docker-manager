/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.dao.model;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.restapi.model.AbstractApiBase;

public class Skipping extends AbstractApiBase {

    private List<String> containerNames = new ArrayList<>();

    public List<String> getContainerNames() {
        return containerNames;
    }

    public void setContainerNames(List<String> containerNames) {
        this.containerNames = containerNames;
    }

}
