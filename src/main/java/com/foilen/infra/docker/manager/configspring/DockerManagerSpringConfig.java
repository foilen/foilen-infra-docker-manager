/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.configspring;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.foilen.smalltools.tools.SpringTools;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan({ "com.foilen.infra.docker.manager.db", "com.foilen.infra.docker.manager.services", "com.foilen.infra.docker.manager.tasks" })
public class DockerManagerSpringConfig {

    @Bean
    public SpringTools springTools() {
        return new SpringTools();
    }

}