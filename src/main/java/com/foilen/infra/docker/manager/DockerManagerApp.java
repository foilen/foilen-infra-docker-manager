/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.foilen.infra.api.InfraApiUiConfigDetails;
import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.docker.manager.configspring.StartManagerConfig;
import com.foilen.infra.docker.manager.configspring.StartUpgradesConfig;
import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.LogbackTools;
import com.google.common.base.Strings;

public class DockerManagerApp {

    private final static Logger logger = LoggerFactory.getLogger(DockerManagerApp.class);

    public static void main(String[] args) throws Exception {

        try {

            // Get the parameters
            DockerManagerOptions options = new DockerManagerOptions();
            CmdLineParser cmdLineParser = new CmdLineParser(options);
            try {
                cmdLineParser.parseArgument(args);
            } catch (CmdLineException e) {
                e.printStackTrace();
                showUsage();
                return;
            }

            List<String> springBootArgs = new ArrayList<String>();
            if (options.debug) {
                springBootArgs.add("--debug");
            }

            // Set the environment
            String mode = options.mode;
            ConfigurableEnvironment environment = new StandardServletEnvironment();
            environment.addActiveProfile(mode);
            System.setProperty("MODE", mode);

            // Get the configuration from options or environment
            String configFile = options.configFile;
            if (Strings.isNullOrEmpty(configFile)) {
                configFile = environment.getProperty("CONFIG_FILE");
            }
            DockerManagerConfig config;
            if (Strings.isNullOrEmpty(configFile)) {
                config = new DockerManagerConfig();
            } else {
                config = JsonTools.readFromFile(configFile, DockerManagerConfig.class);
            }

            // Local -> Add some values
            if ("LOCAL".equals(options.mode)) {
                logger.info("Setting some values for LOCAL mode");

                InfraApiUiConfigDetails infraApiUiConfigDetails = new InfraApiUiConfigDetails();
                config.setInfraApiUiConfigDetails(infraApiUiConfigDetails);

                config.setInternalDatabasePath(JavaEnvironmentValues.getWorkingDirectory() + "/_dockerManager");
                config.setPersistedConfigPath(JavaEnvironmentValues.getWorkingDirectory() + "/_persistedConfig");
                config.setImageBuildPath(JavaEnvironmentValues.getWorkingDirectory() + "/_imageBuild");

                // Create MachineSetup file
                DirectoryTools.createPath(config.getPersistedConfigPath());
                JsonTools.writeToFile(config.getPersistedConfigPath() + "/machineSetup.json", new MachineSetup());
            }

            // Check needed config and add it to the known properties
            BeanWrapper loginConfigBeanWrapper = new BeanWrapperImpl(config);
            for (PropertyDescriptor propertyDescriptor : loginConfigBeanWrapper.getPropertyDescriptors()) {
                String propertyName = propertyDescriptor.getName();
                Object propertyValue = loginConfigBeanWrapper.getPropertyValue(propertyName);
                if (propertyValue == null || propertyValue.toString().isEmpty()) {
                    System.err.println(propertyName + " in the config cannot be null or empty");
                    System.exit(1);
                } else {
                    System.setProperty("dockerManager." + propertyName, propertyValue.toString());
                }
            }

            // Set logging
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            } else {
                LogbackTools.changeConfig("/logback.xml");
            }

            String[] springBootArgsArray = springBootArgs.toArray(new String[springBootArgs.size()]);

            startUpgrader(springBootArgsArray);

            startManager(springBootArgsArray);

            // Check if debug
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            }
        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        }
    }

    private static void showUsage() {
        System.out.println("Usage:");
        CmdLineParser cmdLineParser = new CmdLineParser(new DockerManagerOptions());
        cmdLineParser.printUsage(System.out);
    }

    private static void startManager(String args[]) {

        logger.info("[MANAGER] Begin");

        SpringApplication application = new SpringApplication(StartManagerConfig.class);
        application.setBannerMode(Mode.OFF);
        List<String> profiles = new ArrayList<>();
        profiles.add(System.getProperty("MODE"));
        application.setAdditionalProfiles(profiles.toArray(new String[profiles.size()]));
        application.run(args);

        logger.info("[MANAGER] Started");
    }

    private static void startUpgrader(String args[]) {
        logger.info("[UPGRADER] Begin");

        // Prepare Spring
        AnnotationConfigApplicationContext upgraderAppCtx = new AnnotationConfigApplicationContext();
        upgraderAppCtx.getEnvironment().setActiveProfiles(System.getProperty("MODE"));
        upgraderAppCtx.register(StartUpgradesConfig.class);
        upgraderAppCtx.refresh();
        upgraderAppCtx.close();

        logger.info("[UPGRADER] End");
    }

}
