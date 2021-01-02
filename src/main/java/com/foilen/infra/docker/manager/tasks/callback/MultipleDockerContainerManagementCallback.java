/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.infra.plugin.system.utils.callback.DockerContainerManagementCallback;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;

public class MultipleDockerContainerManagementCallback implements DockerContainerManagementCallback {

    private List<DockerContainerManagementCallback> callbacks = new ArrayList<>();

    public MultipleDockerContainerManagementCallback(DockerContainerManagementCallback... callbacks) {
        this.callbacks = Arrays.asList(callbacks);
    }

    @Override
    public boolean proceedWithTransformedContainer(String containerName, DockerStateIds dockerStateIds) {
        return callbacks.stream().allMatch(it -> it.proceedWithTransformedContainer(containerName, dockerStateIds));
    }

}