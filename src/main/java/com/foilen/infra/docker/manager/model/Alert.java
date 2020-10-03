/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.model;

import com.foilen.smalltools.tools.AbstractBasics;

public class Alert extends AbstractBasics {

    private String subjet;
    private String content;

    public Alert() {
    }

    public Alert(String subjet, String content) {
        this.subjet = subjet;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getSubjet() {
        return subjet;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSubjet(String subjet) {
        this.subjet = subjet;
    }

}
