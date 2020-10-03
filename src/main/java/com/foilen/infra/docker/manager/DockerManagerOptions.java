/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the application.
 */
public class DockerManagerOptions {

    @Option(name = "--debug", usage = "To log everything (default: false)")
    public boolean debug;

    @Option(name = "--configFile", usage = "The config file path (default: none since using the CONFIG_FILE environment variable)")
    public String configFile;

    @Option(name = "--mode", usage = "The mode: LOCAL or PROD (default: PROD)")
    public String mode = "PROD";

}
