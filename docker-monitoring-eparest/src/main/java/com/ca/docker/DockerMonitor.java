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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class DockerMonitor {
    private static Logger  logger;

    private Properties     props;

    private String         dockerHost;

    private int            dockerPort;

    private String         dockerUrl;

    private URL            apmUrl;

    private int            collectionInterval;

    private String         caCertificate;

    private String         clientCertificate;

    private String         clientKey;

    private String         keystorePassword;

    public static boolean sslEnabled = false;

    public static DockerCertificates certificate;

    public DockerMonitor(final Properties inProps) throws Exception {
        props = inProps;
        processProperties();
        try {
            /**
             * Check connection now with docker host & Port. If this fails,
             * there is no point on continuing the program
             */
            if (!checkDockerConnectionThroughRemoteAPI(dockerHost, dockerPort)) {
                String msg = "Docker daemon Unix port is not binding to "
                        + dockerHost
                        + ":"
                        + dockerPort
                        + ". \nIt is possible to make the Docker daemon to listen on "
                        + "a specific IP and port by running -H option. \nFor more "
                        + "information, go to https://docs.docker.com/articles/basics/"
                        + "#bind-docker-to-another-hostport-or-a-unix-socket";

                if (logger != null) {
                    logger.log(Level.SEVERE, msg);
                } else {
                    System.err.println(msg);
                }
            }
            if (collectionInterval > 0) {
                new DataPoller(this);
            }

        } catch (Exception e2) {
            // for standalone server discovery, there's not really
            // anything to fail...
            e2.printStackTrace();
        }

    }

    private void processProperties() throws Exception {
        dockerHost = getStringProp(Constants.DOCKER_HOST_PROP);
        dockerPort = getIntProp(Constants.DOCKER_PORT_PROP);
        setInterval();
        setApmUrl();
        readCertificateEntry();
        if (sslEnabled) {
            DockerCertificates dockerCerificate =
                    new DockerCertificates(
                                           caCertificate,
                                           clientCertificate,
                                           clientKey,
                                           keystorePassword);
            setDockerCertificates(dockerCerificate);


        }
        setDockerUrl();

    }

    public void setDockerCertificates(DockerCertificates dockerCerificate) {
        // TODO Auto-generated method stub
        certificate = dockerCerificate;

    }



    private void readCertificateEntry() {
        // TODO Auto-generated method stub
        caCertificate = getStringProp1(Constants.DOCKER_CA_KEY, props);
        clientCertificate = getStringProp1(Constants.DOCKER_CLIENT_CERTIFIACTE, props);
        clientKey = getStringProp1(Constants.DOCKER_CLIENT_KEY, props);
        if (caCertificate != null && !caCertificate.equals("")
                && clientCertificate != null && !clientCertificate.equals("")
                && clientKey != null && !clientKey.equals("")) {
            sslEnabled = true;
        }
        keystorePassword = getStringProp1(Constants.DOCKER_KEYSTORE_PASSWORD, props);
    }

    private void setDockerUrl() {
        try {
            String protocol;
            if (sslEnabled) {
                protocol = Constants.HTTPS;
            } else {
                protocol = Constants.HTTP;
            }
            dockerUrl = String.format("%s%s:%d", protocol, dockerHost,
                                      dockerPort);
        } catch (Exception ex) {
            throw new RuntimeException("Can't initialize Docker API URL", ex);
        }
    }

    public static Registry<ConnectionSocketFactory> getSchemeRegistry(final DockerCertificates dc) {
        final SSLConnectionSocketFactory https;
        if (dc == null) {
            https = SSLConnectionSocketFactory.getSocketFactory();
        } else {
            https = new SSLConnectionSocketFactory(dc.sslContext(),
                                                   dc.hostnameVerifier());
        }

        final RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", https)
                .register("http",
                          PlainConnectionSocketFactory.getSocketFactory());

        return registryBuilder.build();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: DockerMonitor your-propfile");
            System.exit(1);
        }

        try {
            logger = (new LoggerManager(new FileInputStream(args[0])))
                    .getLogger();
            Properties props = new Properties();
            props.load(new FileReader(args[0]));
            logger.log(Level.INFO, "APM Docker Collector version: {0}",
                       DockerMonitor.class.getPackage()
                       .getImplementationVersion());
            new DockerMonitor(props);

            for (Handler h : logger.getHandlers()) {
                h.close();
            }
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(Level.WARNING, "Exception: {0}", ex);
            } else {
                System.err.println("Exception: " + ex);
            }
            System.exit(1);
        }

    }

    private String getStringProp(final String pname) {
        return getStringProp(pname, props);
    }

    public static String getStringProp(final String pname, final Properties p) {
        final String ret = p.getProperty(pname);
        if (isEmpty(ret)) {
            throw new IllegalArgumentException(
                                               String.format("missing or invalid property: %s%n",
                                                             pname));
        }
        return ret;
    }

    public static String getStringProp1(final String pname, final Properties p) {
        final String ret = p.getProperty(pname);

        return ret;
    }


    private static boolean isEmpty(final String s) {
        return (s == null || "".equals(s.trim()));
    }

    /**
     * 
     * @param pname
     * @return
     */
    private int getIntProp(final String pname) {
        int ret = 0;
        try {
            ret = Integer.parseInt(getStringProp(pname));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                String.format("missing or invalid integer property: %s%n",
                                                             pname));
        }
        return ret;
    }

    private void setInterval() {
        collectionInterval = getIntProp(Constants.DOCKER_POLLING_INTERVAL);
        if (collectionInterval < 5) {
            collectionInterval = Constants.DOCKER_DEFAULT_POLLING_INTERVAL;
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getDockerUrl() {
        return dockerUrl;
    }

    public String getDockerHost() {
        return dockerHost;
    }

    public URL getApmUrl() {
        return apmUrl;
    }

    public int getCollectionInterval() {
        return collectionInterval;
    }

    private void setApmUrl() {
        final String apiHost = getStringProp(Constants.APM_HOST_PROP);
        final int apiPort = getIntProp(Constants.APM_PORT_PROP);
        try {
            apmUrl = new URL(String.format("http://%s:%d/apm/metricFeed",
                                           apiHost, apiPort));
        } catch (Exception ex) {
            throw new RuntimeException("Can't initialize APM API URL", ex);
        }
    }

    /**
     * Connect to http://docker_host:port/containers/json This would ensure
     * that docker resource information is accessible through Remote API
     * @param host
     * @param port
     * @return true, in case the connection is successful through Remote API and
     *         false in case this is not successful
     * 
     */
    private boolean checkDockerConnectionThroughRemoteAPI(String docker_host,
                                                          int port) {
        StringBuffer sb = new StringBuffer(dockerUrl);
        sb.append(Constants.DOCKER_CONTAINER_INFO);
        PoolingHttpClientConnectionManager cManager = new PoolingHttpClientConnectionManager(
                                                                                             getSchemeRegistry(certificate));
        if (sb.toString().startsWith("https")) {
            return checkHttpsConnection(cManager, sb.toString());
        }
        try {
            URL url = new URL(sb.toString());
            URLConnection connection = url.openConnection();
            connection.connect();
            // Cast to a HttpURLConnection
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                // if the response code is anything other than 200, this means
                // its a bad connection
                if (httpConnection.getResponseCode() != 200) {
                    return false;
                }

            } else {
                System.err.println("error - not a http request!");
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    private boolean checkHttpsConnection(PoolingHttpClientConnectionManager cManager, String uri)  {
        // TODO Auto-generated method stub
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cManager)
                .build();

        HttpGet request = new HttpGet(uri);
        try {
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                return false;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
