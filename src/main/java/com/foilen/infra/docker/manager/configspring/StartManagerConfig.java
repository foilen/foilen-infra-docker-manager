/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.configspring;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.foilen.smalltools.tools.SpringTools;

@Configuration
@EnableAutoConfiguration
@ComponentScan({ "com.foilen.infra.docker.manager.db", "com.foilen.infra.docker.manager.services", "com.foilen.infra.docker.manager.tasks" })
@PropertySource({ "classpath:/com/foilen/infra/docker/manager/config/application.properties", "classpath:/com/foilen/infra/docker/manager/config/application-${MODE}.properties" })
public class StartManagerConfig {

    @Bean
    public SpringTools springTools() {
        return new SpringTools();
    }

}