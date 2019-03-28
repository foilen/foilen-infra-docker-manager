/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.docker.manager.model.Alert;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;

@Component
public class AlertingServiceImpl extends AbstractBasics implements AlertingService {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

    @Value("${dockerManager.internalDatabasePath}/alerts/")
    private File alertDirectory;

    @Override
    public void saveAlert(String subject, String content) {
        String alertFileName = alertDirectory.getAbsolutePath() + File.separator + sdf.format(new Date()) + ".json";
        OutputStream alertOutputStream = FileTools.createStagingFile(alertFileName + ".tmp", alertFileName);
        JsonTools.writeToStream(alertOutputStream, new Alert(subject, content));
        CloseableTools.close(alertOutputStream);
    }

}
