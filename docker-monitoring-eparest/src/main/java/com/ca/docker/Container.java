package com.ca.docker;

import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Container implements DockerInfo
{
    /**
     * This is sample json data when queries with /containers/json
     * {
                 "Id": "8dfafdbc3a40",
                 "Image": "ubuntu:latest",
                 "Command": "echo 1",
                 "Created": 1367854155,
                 "Status": "Exit 0",
                 "Ports": [
                     {
                     "IP": 0.0.0.0
                     "PrivatePort": 2222, 
                     "PublicPort": 3333, 
                     "Type": "tcp"
                     }
                 ],
                 "SizeRw": 12288,
                 "SizeRootFs": 0
         }
     */
    
    @SerializedName("Id") private String id;
    @SerializedName("Image") private String image;
    @SerializedName("Command") private String command;
    @SerializedName("Created") private Double created;
    @SerializedName("Status") private String status;
    @SerializedName("Names") private List names;
    // This is not working currently 
    //private Port Ports;
    @SerializedName("SizeRw") private Long sizeRw;
    @SerializedName("SizeRootFs") private Long sizeRootFs;
    
    private HashMap<String, Comparable<?>> containerInfo;
    
    public void populateMetricData(Container c)
    {
        containerInfo = new HashMap<String, Comparable<?>>();
        containerInfo.put("Id", c.id);
        containerInfo.put("Image", c.image);
        containerInfo.put("Command", c.command);
        containerInfo.put("Created", c.created);
        containerInfo.put("Status", c.status);
        containerInfo.put("SizeRw", c.sizeRw);
        containerInfo.put("SizeRootFs", c.sizeRootFs);
        containerInfo.put("Names", (String)c.names.get(0));
    }

    @Override
    public HashMap<String, Comparable<?>> getDockerInfo()
    {
        return containerInfo;
    }

    public String getId()
    {
        return id;
    }

    public String getNames()
    {
        return (String)names.get(0);
    }

    public String getStatus()
    {
        return status;
    }
    


}
