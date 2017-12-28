/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.foilen.smalltools.tools.AbstractBasics;

@Entity
public class InstalledUnixUser extends AbstractBasics {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    public InstalledUnixUser() {
    }

    public InstalledUnixUser(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
