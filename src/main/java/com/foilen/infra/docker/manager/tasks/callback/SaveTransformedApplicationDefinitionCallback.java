/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks.callback;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.system.utils.callback.TransformedApplicationDefinitionCallback;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ThreadTools;

@Component
public class SaveTransformedApplicationDefinitionCallback extends AbstractBasics implements TransformedApplicationDefinitionCallback {

    @Value("${dockerManager.internalDatabasePath}/debug-applicationDefinition/")
    private File folder;

    @Override
    public void handler(String applicationName, IPApplicationDefinition applicationDefinition) {
        try {
            JsonTools.writeToFile(folder.getAbsolutePath() + File.separatorChar + applicationName + "+" + System.currentTimeMillis() + ".json", applicationDefinition);
        } catch (Throwable t) {
            logger.error("Problem saving the debug application definition", t);
        }
    }

    @PostConstruct
    public void init() {
        DirectoryTools.createPath(folder);

        ExecutorsTools.getCachedThreadPool().submit(() -> {
            for (;;) {
                try {
                    ThreadTools.sleep(15 * 60000);
                    Date expiredBefore = DateTools.addDate(Calendar.HOUR, -1);
                    DirectoryTools.deleteOlderFilesInDirectory(folder, expiredBefore);
                } catch (Throwable e) {
                    logger.error("Problem cleaning up the debug application definition", e);
                }
            }
        });

    }

}