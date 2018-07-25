/*
    Foilen Infra Docker Manager
    https://github.com/foilen/foilen-infra-docker-manager
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.docker.manager.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.DiskStat;
import com.foilen.infra.api.model.SystemStats;
import com.foilen.smalltools.systemusage.FileSystemUsage;
import com.foilen.smalltools.systemusage.FileSystemUsage.FileSystemInfo;
import com.foilen.smalltools.systemusage.ProcUsage;
import com.foilen.smalltools.systemusage.results.CpuInfo;
import com.foilen.smalltools.systemusage.results.MemoryInfo;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.SystemTools;

@Component
class SystemStatisticsServiceImpl implements SystemStatisticsService {

    private String procNetDevPath = SystemTools.getPropertyOrEnvironment("HOSTFS", "/") + "/proc/net/dev";
    private String procStatPath = SystemTools.getPropertyOrEnvironment("HOSTFS", "/") + "/proc/stat";
    private String procMeminfoPath = SystemTools.getPropertyOrEnvironment("HOSTFS", "/") + "/proc/meminfo";

    private CpuInfo lastCpuInfo;
    private Map<String, NetworkInfo> networkInfoByInterfaceName = new HashMap<>();

    @Override
    public SystemStats retrieveSystemStats() {

        SystemStats systemStats = new SystemStats();

        // CPU
        CpuInfo currentCpuInfo = ProcUsage.getMainCpuInfo(procStatPath);
        if (lastCpuInfo != null) {
            long total = currentCpuInfo.calculateTotal() - lastCpuInfo.calculateTotal();
            long busy = currentCpuInfo.calculateBusy() - lastCpuInfo.calculateBusy();
            systemStats.setCpuUsed(busy);
            systemStats.setCpuTotal(total);
        }
        lastCpuInfo = currentCpuInfo;

        // Memory
        MemoryInfo memoryInfo = ProcUsage.getMemoryInfo(procMeminfoPath);
        systemStats.setMemoryUsed(memoryInfo.getPhysicalUsed());
        systemStats.setMemoryTotal(memoryInfo.getPhysicalTotal());

        // Memory Swap
        systemStats.setMemorySwapUsed(memoryInfo.getSwapUsed());
        systemStats.setMemorySwapTotal(memoryInfo.getSwapTotal());

        // Disks
        for (FileSystemInfo fileSystemInfo : FileSystemUsage.getRootFileSystemInfos()) {
            DiskStat diskStat = new DiskStat();
            systemStats.getDiskStats().add(diskStat);
            diskStat.setPath(fileSystemInfo.getFile().getAbsolutePath());
            diskStat.setRoot(true);
            diskStat.setFreeSpace(fileSystemInfo.getFreeSpace());
            diskStat.setFreeSpacePercent(fileSystemInfo.getFreeSpacePercent());
            diskStat.setTotalSpace(fileSystemInfo.getTotalSpace());
            diskStat.setUsedSpace(fileSystemInfo.getUsedSpace());
            diskStat.setUsedSpacePercent(fileSystemInfo.getUsedSpacePercent());
        }

        // Network
        for (NetworkInfo currentNetworkInfo : ProcUsage.getNetworkInfos(procNetDevPath)) {
            String interfaceName = currentNetworkInfo.getInterfaceName();
            NetworkInfo lastNetworkInfo = networkInfoByInterfaceName.get(interfaceName);
            if (lastNetworkInfo != null) {
                NetworkInfo deltaNetworkInfo = new NetworkInfo();
                deltaNetworkInfo.setInterfaceName(interfaceName);
                deltaNetworkInfo.setInBytes(currentNetworkInfo.getInBytes() - lastNetworkInfo.getInBytes());
                deltaNetworkInfo.setInBytes(currentNetworkInfo.getInPackets() - lastNetworkInfo.getInPackets());
                deltaNetworkInfo.setOutBytes(currentNetworkInfo.getOutBytes() - lastNetworkInfo.getOutBytes());
                deltaNetworkInfo.setOutBytes(currentNetworkInfo.getOutPackets() - lastNetworkInfo.getOutPackets());
                systemStats.getNetworkDeltas().add(deltaNetworkInfo);
            }
            networkInfoByInterfaceName.put(currentNetworkInfo.getInterfaceName(), currentNetworkInfo);
        }

        return systemStats;
    }
}
