/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.foilen.smalltools.tools.AbstractBasics;

@Entity
@Table(indexes = { @Index(name = "redirect_endpoint_port", columnList = "port") })
public class RedirectEndpoint extends AbstractBasics {

    @Id
    @Column(nullable = false, unique = true)
    private String machineContainerEndpoint;

    @Column(length = 100)
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public String getMachineContainerEndpoint() {
        return machineContainerEndpoint;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setMachineContainerEndpoint(String machineContainerEndpoint) {
        this.machineContainerEndpoint = machineContainerEndpoint;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
