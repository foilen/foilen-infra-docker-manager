/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.configspring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.foilen.infra.docker.manager.tasks.callback.MultipleDockerContainerManagementCallback;
import com.foilen.infra.docker.manager.tasks.callback.NotFailedCallback;
import com.foilen.infra.docker.manager.tasks.callback.NotSkippingCallback;
import com.foilen.smalltools.tools.SpringTools;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan({ "com.foilen.infra.docker.manager.db", "com.foilen.infra.docker.manager.services", "com.foilen.infra.docker.manager.tasks" })
public class DockerManagerSpringConfig {

    @Autowired
    private NotFailedCallback notFailedCallback;
    @Autowired
    private NotSkippingCallback notSkippingCallback;

    @Bean
    public MultipleDockerContainerManagementCallback multipleDockerContainerManagementCallback() {
        return new MultipleDockerContainerManagementCallback(notSkippingCallback, notFailedCallback);
    }

    @Bean
    public SpringTools springTools() {
        return new SpringTools();
    }

}