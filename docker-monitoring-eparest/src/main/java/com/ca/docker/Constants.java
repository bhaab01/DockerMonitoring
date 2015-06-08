package com.ca.docker;

import java.math.BigDecimal;
/**
 * This file contains all the constants used in the application
 * @author bhaab01
 *
 */
public class Constants
{
    public static final String DOCKER_HOST_PROP   = "docker.hostname";
    public static final String DOCKER_PORT_PROP   = "docker.port";
    public static final String APM_HOST_PROP = "apm.apihost";
    public static final String APM_PORT_PROP = "apm.apiport";
    public static final String DOCKER_POLLING_INTERVAL = "docker.interval.seconds";
    public static final String DOCKER_CONTAINER_INFO = "/containers/json?all=1";
    public static final String DOCKER_HOST_INFO = "/info";
    
    public static final int DOCKER_DEFAULT_POLLING_INTERVAL = 60;
    public static final String PIPE = "|";
    public static final String COLON = ":";
    public static final String UNDER_SCORE = "_";
    public static final String SEMI_COLON = ";";
    
   
}
