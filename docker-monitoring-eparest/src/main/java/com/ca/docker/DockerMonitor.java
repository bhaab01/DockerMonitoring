package com.ca.docker;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DockerMonitor
{
    private static Logger logger;

    private Properties    props;

    private String        dockerHost;

    private int           dockerPort;

    private String        dockerUrl;

    private URL           apmUrl;

    private int           collectionInterval;

    public DockerMonitor(final Properties inProps)
    {
        props = inProps;
        processProperties();
        try
        {
            /**
             * Check connection now with docker host & Port. If this fails,
             * there is no point on continuing the program
             */
            if (!checkDockerConnectionThroughRemoteAPI(dockerHost, dockerPort))
            {
                if (logger != null)
                {
                    logger.log(Level.SEVERE,
                               "Docker daemon Unix port is not binding to "
                                       + dockerHost
                                       + ":"
                                       + dockerPort
                                       + ". \nIt is possible to make the Docker daemon to listen on a specific IP and port by running -H option. \nFor more information, go to https://docs.docker.com/articles/basics/#bind-docker-to-another-hostport-or-a-unix-socket");
                } else
                {
                    System.err
                            .println("Docker daemon Unix port is not binding to "
                                     + dockerHost
                                     + ":"
                                     + dockerPort
                                     + ". \nIt is possible to make the Docker daemon to listen on a specific IP and port by running -H option. \nFor more information, go to https://docs.docker.com/articles/basics/#bind-docker-to-another-hostport-or-a-unix-socket");
                }
            }
            if ( collectionInterval > 0)
            {
                new DataPoller(this);
            }
            
            /*
             * topology = new StandaloneMongod(props, host, port, logger);
             * topology.discoverServers("Standalone");
             */
        } catch (Exception e2)
        {
            // for standalone server discovery, there's not really
            // anything to fail...
            e2.printStackTrace();
        }

    }

    private void processProperties()
    {
        dockerHost = getStringProp(Constants.DOCKER_HOST_PROP);
        dockerPort = getIntProp(Constants.DOCKER_PORT_PROP);
        setDockerUrl();
        setInterval();
        setApmUrl();
    }

    private void setDockerUrl()
    {
        try
        {
            dockerUrl = String.format("http://%s:%d", dockerHost, dockerPort);
        } catch (Exception ex)
        {
            throw new RuntimeException("Can't initialize Docker API URL", ex);
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Usage: DockerMonitor your-propfile");
            System.exit(1);
        }
        try
        {
            logger = (new LoggerManager(new FileInputStream(args[0])))
                    .getLogger();
            Properties p = new Properties();
            p.load(new FileReader(args[0]));
            logger.log(Level.INFO, "APM Docker Collector version: {0}",
                       DockerMonitor.class.getPackage()
                               .getImplementationVersion());
            new DockerMonitor(p);

            for (Handler h : logger.getHandlers())
            {
                h.close();
            }
        }

        catch (Exception ex)
        {
            if (logger != null)
            {
                logger.log(Level.WARNING, "Exception: {0}", ex);
            } else
            {
                System.err.println("Exception: " + ex);
            }
            System.exit(1);
        }

    }



    private String getStringProp(final String pname)
    {
        return getStringProp(pname, props);
    }

    public static String getStringProp(final String pname, final Properties p)
    {
        final String ret = p.getProperty(pname);
        if (isEmpty(ret))
        {
            throw new IllegalArgumentException(
                                               String.format("missing or invalid property: %s%n",
                                                             pname));
        }
        return ret;
    }

    private static boolean isEmpty(final String s)
    {
        return (s == null || "".equals(s.trim()));
    }

    /**
     * 
     * @param pname
     * @return
     */
    private int getIntProp(final String pname)
    {
        int ret = 0;
        try
        {
            ret = Integer.parseInt(getStringProp(pname));
        } catch (NumberFormatException nfe)
        {
            throw new IllegalArgumentException(
                                               String.format("missing or invalid integer property: %s%n",
                                                             pname));
        }
        return ret;
    }

    private void setInterval()
    {
        collectionInterval = getIntProp(Constants.DOCKER_POLLING_INTERVAL);
        if (collectionInterval < 5)
            collectionInterval = Constants.DOCKER_DEFAULT_POLLING_INTERVAL;
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public String getDockerUrl()
    {
        return dockerUrl;
    }

    public String getDockerHost()
    {
        return dockerHost;
    }

    public URL getApmUrl()
    {
        return apmUrl;
    }

    public int getCollectionInterval()
    {
        return collectionInterval;
    }

    private void setApmUrl()
    {
        final String apiHost = getStringProp(Constants.APM_HOST_PROP);
        final int apiPort = getIntProp(Constants.APM_PORT_PROP);
        try
        {
            apmUrl = new URL(String.format("http://%s:%d/apm/metricFeed",
                                           apiHost, apiPort));
        } catch (Exception ex)
        {
            throw new RuntimeException("Can't initialize APM API URL", ex);
        }
    }

    /**
     * 
     * @param host
     * @param port
     * @return true, in case the connection is successful through Remote API and
     *         false in case this is not successful
     * 
     */
    private boolean checkDockerConnectionThroughRemoteAPI(String docker_host,
                                                          int port)
    {
        /**
         * Connect to http://docker_host:port/containers/json This would ensure
         * that docker resource information is accessible through Remote API
         */

        StringBuffer sb = new StringBuffer(dockerUrl);
        sb.append(Constants.DOCKER_CONTAINER_INFO);

        try
        {
            URL url = new URL(sb.toString());
            URLConnection connection = url.openConnection();
            connection.connect();
            // Cast to a HttpURLConnection
            if (connection instanceof HttpURLConnection)
            {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                // if the response code is anything other than 200, this means
                // its a bad connection
                if (httpConnection.getResponseCode() != 200) return false;

            } else
            {
                System.err.println("error - not a http request!");
                return false;
            }
        } catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;

    }

}
