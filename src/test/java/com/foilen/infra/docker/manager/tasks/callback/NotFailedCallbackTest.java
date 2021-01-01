/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks.callback;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.foilen.infra.docker.manager.db.dao.DockerStateDaoImpl;
import com.foilen.infra.docker.manager.db.services.DbServiceImpl;
import com.foilen.infra.plugin.system.utils.model.DockerState;
import com.foilen.infra.plugin.system.utils.model.DockerStateFailed;
import com.foilen.infra.plugin.system.utils.model.DockerStateIds;
import com.foilen.smalltools.tools.DateTools;

public class NotFailedCallbackTest {

    private static final String FAILURE_ID = "failure_id";
    private static final String OK_ID = "ok_id";

    private static final String containerName = "theContainer";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private NotFailedCallback callback = new NotFailedCallback();

    private DockerStateDaoImpl dockerStateDao;
    private DockerStateIds failedDockerStateIds;

    @Before
    public void before() throws Exception {

        dockerStateDao = new DockerStateDaoImpl();
        dockerStateDao.setFile(folder.newFile());
        dockerStateDao.setStagingFile(folder.newFile());
        dockerStateDao.save(new DockerState());

        DbServiceImpl dbService = new DbServiceImpl();
        dbService.setDockerStateDao(dockerStateDao);
        callback.setDbService(dbService);

        failedDockerStateIds = new DockerStateIds(FAILURE_ID, FAILURE_ID, FAILURE_ID);
    }

    @Test
    public void testProceedWithTransformedContainer_11_mins_ago_OK() {
        DockerState dockerState = new DockerState();
        DockerStateFailed dockerStateFailed = new DockerStateFailed(failedDockerStateIds, DateTools.addDate(Calendar.MINUTE, -11));
        dockerState.getFailedContainersByName().put(containerName, dockerStateFailed);
        dockerStateDao.save(dockerState);
        Assert.assertTrue(callback.proceedWithTransformedContainer(containerName, failedDockerStateIds));
    }

    @Test
    public void testProceedWithTransformedContainer_5_mins_ago_NO() {
        DockerState dockerState = new DockerState();
        DockerStateFailed dockerStateFailed = new DockerStateFailed(failedDockerStateIds, DateTools.addDate(Calendar.MINUTE, -5));
        dockerState.getFailedContainersByName().put(containerName, dockerStateFailed);
        dockerStateDao.save(dockerState);
        Assert.assertFalse(callback.proceedWithTransformedContainer(containerName, failedDockerStateIds));
    }

    @Test
    public void testProceedWithTransformedContainer_diff_id_11_mins_ago_OK() {
        DockerState dockerState = new DockerState();
        DockerStateFailed dockerStateFailed = new DockerStateFailed(failedDockerStateIds, DateTools.addDate(Calendar.MINUTE, -11));
        dockerState.getFailedContainersByName().put(containerName, dockerStateFailed);
        dockerStateDao.save(dockerState);
        Assert.assertTrue(callback.proceedWithTransformedContainer(containerName, new DockerStateIds(OK_ID, OK_ID, OK_ID)));
    }

    @Test
    public void testProceedWithTransformedContainer_diff_id_5_mins_ago_OK() {
        DockerState dockerState = new DockerState();
        DockerStateFailed dockerStateFailed = new DockerStateFailed(failedDockerStateIds, DateTools.addDate(Calendar.MINUTE, -5));
        dockerState.getFailedContainersByName().put(containerName, dockerStateFailed);
        dockerStateDao.save(dockerState);
        Assert.assertTrue(callback.proceedWithTransformedContainer(containerName, new DockerStateIds(OK_ID, OK_ID, OK_ID)));
    }

    @Test
    public void testProceedWithTransformedContainer_no_previous_OK() {
        Assert.assertTrue(callback.proceedWithTransformedContainer(containerName, failedDockerStateIds));
    }

}
