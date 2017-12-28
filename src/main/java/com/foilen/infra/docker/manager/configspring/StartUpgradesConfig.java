/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.configspring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.DatabaseUpgraderTracker;

@Configuration
@ComponentScan({ "com.foilen.infra.docker.manager.upgrades" })
@EnableAutoConfiguration(exclude = { JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@PropertySource({ "classpath:/com/foilen/infra/docker/manager/config/application.properties", "classpath:/com/foilen/infra/docker/manager/config/application-${MODE}.properties" })
public class StartUpgradesConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public UpgraderTools upgraderTools(List<UpgradeTask> tasks) {
        UpgraderTools upgraderTools = new UpgraderTools(tasks);
        upgraderTools.addUpgraderTracker("db", new DatabaseUpgraderTracker(jdbcTemplate));
        return upgraderTools;
    }

}
