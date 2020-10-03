/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.tasks;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.foilen.infra.plugin.system.utils.model.DockerStateIp;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.DateTools;

public class ApplyStateTaskTest {

    @Test
    public void testCleanupIps() {
        Map<String, DockerStateIp> ipStateByName = new TreeMap<>();
        ipStateByName.put("keep_single1", new DockerStateIp().setIp("127.0.0.1").setLastUsed(DateTools.addDate(Calendar.HOUR, -1)));
        ipStateByName.put("keep_single2", new DockerStateIp().setIp("127.0.0.60").setLastUsed(DateTools.addDate(Calendar.HOUR, -2)));
        ipStateByName.put("keep_single3", new DockerStateIp().setIp("127.0.0.61").setLastUsed(DateTools.addDate(Calendar.HOUR, -26)));

        ipStateByName.put("a_remove_sameip_old1", new DockerStateIp().setIp("127.0.0.2").setLastUsed(DateTools.addDate(Calendar.HOUR, -3)));
        ipStateByName.put("keep_sameip_recent", new DockerStateIp().setIp("127.0.0.2").setLastUsed(DateTools.addDate(Calendar.HOUR, -1)));
        ipStateByName.put("remove_sameip_old2", new DockerStateIp().setIp("127.0.0.2").setLastUsed(DateTools.addDate(Calendar.HOUR, -4)));

        ipStateByName.put("a_remove_sameip_old1_v2", new DockerStateIp().setIp("127.0.0.20").setLastUsed(DateTools.addDate(Calendar.HOUR, -30)));
        ipStateByName.put("keep_sameip_recent_v2", new DockerStateIp().setIp("127.0.0.20").setLastUsed(DateTools.addDate(Calendar.HOUR, -15)));
        ipStateByName.put("remove_sameip_old2_v2", new DockerStateIp().setIp("127.0.0.20").setLastUsed(DateTools.addDate(Calendar.HOUR, -40)));

        ipStateByName.put("remove_too_old1", new DockerStateIp().setIp("127.0.0.3").setLastUsed(DateTools.addDate(Calendar.HOUR, -50)));
        ipStateByName.put("remove_too_old2", new DockerStateIp().setIp("127.0.0.4").setLastUsed(DateTools.addDate(Calendar.HOUR, -100)));

        // Execute
        ApplyStateTask.cleanupIps(ipStateByName);

        // Assert
        Map<String, DockerStateIp> expectedIpStateByName = new TreeMap<>();
        ipStateByName.forEach((containerName, ip) -> {
            if (containerName.startsWith("keep_")) {
                expectedIpStateByName.put(containerName, ip);
            }
        });
        AssertTools.assertJsonComparisonWithoutNulls(expectedIpStateByName, ipStateByName);
    }

}
