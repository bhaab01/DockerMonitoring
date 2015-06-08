package com.ca.docker;

import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HostInfo
    implements DockerInfo
{

    /**
     * 
     "Containers":10, "Debug":0, "DockerRootDir":"/var/lib/docker",
     * "Driver":"devicemapper", "DriverStatus":[ [
     * "Pool Name","docker-253:0-2624099-pool" ], [ "Pool Blocksize","65.54 kB"
     * ], [ "Backing Filesystem","extfs" ], [ "Data file", "/dev/loop0" ], [
     * "Metadata file", "/dev/loop1" ], [ "Data Space Used", "6.74 GB" ], [
     * "Data Space Total","107.4 GB" ], [ "Metadata Space Used","6.484 MB" ], [
     * "Metadata Space Total","2.147 GB" ], [ "Udev Sync Supported", "true" ], [
     * "Data loop file", "/var/lib/docker/devicemapper/devicemapper/data" ], [
     * "Metadata loop file","/var/lib/docker/devicemapper/devicemapper/metadata"
     * ], [ "Library Version", "1.02.89-RHEL6 (2014-09-01)" ] ],
     * "ExecutionDriver":"native-0.2",
     * "ID":"CUVK:XWE7:H7QR:6I2Y:R6PU:ZD3V:6XT7:FKJQ:UE4F:SNIA:X5KS:ZHZA",
     * "IPv4Forwarding":1, "Images":63,
     * "IndexServerAddress":"https://index.docker.io/v1/",
     * "InitPath":"/usr/libexec/docker/dockerinit",
     * "InitSha1":"bd0359e86e0e97527aa6298afba8df863db179c8",
     * "KernelVersion":"2.6.32-431.el6.x86_64", "Labels":null,
     * "MemTotal":4011843584, "MemoryLimit":1, "NCPU":1, "NEventsListener":0,
     * "NFd":100, "NGoroutines":74, "Name":"bhaab01-U148604",
     * "OperatingSystem":"\u003cunknown\u003e", "RegistryConfig":{
     * "IndexConfigs":{ "docker.io":{ "Mirrors":null, "Name":"docker.io",
     * "Official":true, "Secure":true } }, "InsecureRegistryCIDRs":[
     * "127.0.0.0/8" ] }, "SwapLimit":1 }
     */
    @SerializedName("Containers")
    private Integer                        totalContainer;

    @SerializedName("Images")
    private Integer                        totalImages;

    @SerializedName("Name")
    private String                         name;

    @SerializedName("DockerRootDir")
    private String                         dockerRootDir;

    @SerializedName("OperatingSystem")
    private String                         os;

    @SerializedName("IndexServerAddress")
    private String                         indexServerAddress;

    @SerializedName("KernelVersion")
    private String                         kernelVersion;

    @SerializedName("DriverStatus")
    private List<List>                     driverStatus;

    @SerializedName("NCPU")
    private Integer                        noofcpu;

    @SerializedName("MemTotal")
    private Long                           totalMemory;

    private HashMap<String, Comparable<?>> hostInfo;
    
    private int upContainer;
    private int downContainer;
    
    public void updateContainerInfo(int a, int b)
    {
        hostInfo.put("Running Container", a);
        hostInfo.put("Stopped Container", b);  
    }

    public void populateMetricData(HostInfo c)
    {
        hostInfo = new HashMap<String, Comparable<?>>();
        hostInfo.put("Total Container", c.totalContainer);
        hostInfo.put("Total Images", c.totalImages);
        hostInfo.put("Name", c.name);
        hostInfo.put("DockerRootDir", c.dockerRootDir);
        hostInfo.put("OperatingSystem", c.os);
        hostInfo.put("IndexServerAddress", c.indexServerAddress);
        hostInfo.put("KernelVersion", c.kernelVersion);
        hostInfo.put("No of CPU", c.noofcpu);
        hostInfo.put("Total Memory", c.totalMemory/(1024*1024));
        
        
        for (int i = 0; i < driverStatus.size(); i++)
        {
           if (((String) driverStatus.get(i).get(0)).equals("Pool Blocksize")
                || ((String) driverStatus.get(i).get(0))
                        .equals("Data Space Used")
                || ((String) driverStatus.get(i).get(0))
                        .equals("Data Space Total")
                || ((String) driverStatus.get(i).get(0))
                        .equals("Metadata Space Used")
                || ((String) driverStatus.get(i).get(0))
                        .equals("Metadata Space Total")

            )
            {
                String value = (String) (driverStatus.get(i).get(1));
                String[] result = value.split("\\s");
                Integer metricVal = Math.round(Float.valueOf(result[0]));
                String metricName = (String) driverStatus.get(i).get(0)
                                    + " (in " + result[1] + ")";
                hostInfo.put(metricName, metricVal);
            }
        }
    }

    @Override
    public HashMap<String, Comparable<?>> getDockerInfo()
    {
        // TODO Auto-generated method stub
        return hostInfo;
    }

}
