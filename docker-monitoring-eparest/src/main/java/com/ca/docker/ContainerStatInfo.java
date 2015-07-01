package com.ca.docker;

import java.util.HashMap;

public class ContainerStatInfo
    implements DockerInfo
{
    private Double cpupercentage;
    private Double memorypercentage;
    private Long memoryusage;
    private Long totalmemory;
    private NetworkStatInfo nsi;
    
    private HashMap<String, Comparable<?>>  containerStatInfo;
    
    @Override
    public HashMap<String, ?> getDockerInfo()
    {
        // TODO Auto-generated method stub
        return containerStatInfo;
    }

    public void populateMetricData(ContainerStatInfo statInfo)
    {
        // TODO Auto-generated method stub
        containerStatInfo = new HashMap<String, Comparable<?>>();
        
        
        containerStatInfo.put("CPU %", statInfo.cpupercentage);
        containerStatInfo.put("Memory %", statInfo.memorypercentage);
        containerStatInfo.put("Memory (in MB)", statInfo.memoryusage);
        
        containerStatInfo.put("Bytes Received", statInfo.nsi.getBytesreceived());
        containerStatInfo.put("Packets Received", statInfo.nsi.getPacketsreceived());
        containerStatInfo.put("Errors Received", statInfo.nsi.getErrorsreceived());
        containerStatInfo.put("Dropped Packets during Receive", statInfo.nsi.getDropsreceived());
        
        
        containerStatInfo.put("Bytes Sent", statInfo.nsi.getBytestransmitted());
        containerStatInfo.put("Packets Sent", statInfo.nsi.getPacketstransmitted());
        containerStatInfo.put("Errors Sent", statInfo.nsi.getErrorstransmitted());
        containerStatInfo.put("Dropped Packets during Sent", statInfo.nsi.getDropstransmitted());
        
        
        
    }

    public void setCpupercentage(Double cpupercentage)
    {
        this.cpupercentage = cpupercentage;
    }

    public void setMemorypercentage(Double memorypercentage)
    {
        this.memorypercentage = memorypercentage;
    }

    public void setMemporyUsage(Long memoryUsage)
    {
        // TODO Auto-generated method stub
        this.memoryusage = memoryUsage;
    }

    public void setTotalMemory(Long totalMemory)
    {
        // TODO Auto-generated method stub
        this.totalmemory  = totalMemory;
    }

    public void setNetworkData(NetworkStatInfo netWorkData)
    {
        // TODO Auto-generated method stub
        this.nsi = netWorkData;
    }

}
