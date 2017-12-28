/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager;

import org.hibernate.dialect.H2Dialect;

import com.foilen.smalltools.tools.Hibernate50Tools;

public class DockerManagerGenSql {

    public static void main(String[] args) {
        Hibernate50Tools.generateSqlSchema(H2Dialect.class, "sql/h2.sql", true, "com.foilen.infra.docker.manager.db.model");
    }

}
