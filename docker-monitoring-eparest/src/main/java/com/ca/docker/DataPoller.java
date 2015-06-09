/**
 * 
 */
package com.ca.docker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.BooleanNode;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author bhaab01
 *
 */
public class DataPoller
    implements Runnable
{

    private final DockerMonitor   dockerMonitor;

    private MetricFeedBundle      mfb;

    private Cache<String, Double> previousMetricsMap;

    private static int            upContainer;

    private static int            downContainer;

    private HostInfo              hostInfo;

    private final Logger          logger;

    public DockerMonitor getDockerMonitor()
    {
        return dockerMonitor;
    }

    public DataPoller(DockerMonitor dm)
    {
        // TODO Auto-generated constructor stub
        dockerMonitor = dm;
        logger = DockerMonitor.getLogger();

        previousMetricsMap = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES).build();
        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(1);
        ses.scheduleAtFixedRate(this, 0, dm.getCollectionInterval(),
                                TimeUnit.SECONDS);

        // run forever
        synchronized (ses)
        {
            try
            {
                ses.wait();
            } catch (InterruptedException ie)
            {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        // TODO Auto-generated method stub
        logger.log(Level.INFO, "harvesting metrics...");
        try
        {
            hostInfo = readHostInfoJsonFromUrl(Constants.DOCKER_HOST_INFO);
            mfb = new MetricFeedBundle();
            // makeMetrics(hostInfo, null);

            ArrayList<Container> containers = readContainerInfoJsonFromUrl(Constants.DOCKER_CONTAINER_INFO);
            for (Container container : containers)
            {
                makeMetrics(container,
                            (String) container.getDockerInfo().get("Names"));

            }
            makeMetrics(hostInfo, null);
            deliverMetrics(mfb);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Make sure we replace the Introscope reserved characters with proper
     * character Replace '|' character with '_' & similarly, replace ':' to ';'
     * 
     * @param in
     * @return
     */

    private String translate(final String in)
    {
        return in.replace(Constants.PIPE, Constants.UNDER_SCORE)
                .replace(Constants.COLON, Constants.SEMI_COLON);
    }

    /**
     * Deliver the metrics
     * 
     * @param mfb
     * @throws Exception
     */

    public void deliverMetrics(final MetricFeedBundle mfb) throws Exception
    {
        final String json = mfb.toString();
        final HttpURLConnection conn = (HttpURLConnection) dockerMonitor
                .getApmUrl().openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.getOutputStream().write(json.getBytes());
        final int rc = conn.getResponseCode();
        if (rc != 200)
        {
            logger.log(Level.SEVERE, "Error code: {0}, payload: {1}",
                       new Object[] { rc, getPayload(conn.getErrorStream()) });
        } else
        {
            logger.log(Level.INFO, "Successful metric delivery");
        }
    }

    /**
     * 
     * @param is
     * @return
     * @throws Exception
     */
    private String getPayload(final InputStream is) throws Exception
    {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = rdr.readLine()) != null)
        {
            sb.append(String.format("%s%n", line));
        }
        rdr.close();
        return sb.toString();
    }

    /**
     * 
     * @param hci
     * @param additionalPath
     * @return
     * @throws Exception
     */
    public MetricFeedBundle makeMetrics(final Object hci, String additionalPath)
        throws Exception
    {
        String basePath = null;
        if (additionalPath != null)
            basePath = "Docker" + Constants.PIPE
                       + dockerMonitor.getDockerHost() + Constants.PIPE
                       + translate(additionalPath);
        else
            basePath = "Docker" + "|" + dockerMonitor.getDockerHost();
        // Add a "mongo segment" to the metric path to insure that
        // mongo metrics are grouped/segregated in the metric browser.
        // Note that we can't use ":" in that segment though

        makeMetrics(mfb, basePath, hci);
        return mfb;
    }

    /**
     * 
     * @param mfb
     * @param basePath
     * @param hci
     */
    private void makeMetrics(MetricFeedBundle mfb, String basePath, Object hci)
    {
        Iterator<?> it = ((DockerInfo) hci).getDockerInfo().entrySet()
                .iterator();
        while (it.hasNext())
        {
            final MetricPath metricPath = new MetricPath(basePath);
            Map.Entry pair = (Map.Entry) it.next();
            metricPath.addMetric((String) pair.getKey());
            makeMetric(metricPath.toString(), pair.getValue(), mfb);
            it.remove();
        }

    }

    private void makeMetric(final String metricPath,
                            final Object dataObj,
                            final MetricFeedBundle mfb)
    {
        if (dataObj instanceof String)
        {
            mfb.addMetric("StringEvent", metricPath, (String) dataObj);
        } else if (dataObj instanceof Number)
        {
            String type;
            if (dataObj instanceof Double)
            {
                // API doesn't support floating-point metric values
                // so we round the value to a long, and also create a string
                // metric to display the value (just for debugging etc.)
                type = "LongCounter";
                long val = Math.round((Double) dataObj);
                mfb.addMetric("LongCounter", metricPath + " (rounded)",
                              String.valueOf(val));
                mfb.addMetric("StringEvent", metricPath + " (string)",
                              dataObj.toString());
            } else
            {
                if (dataObj instanceof Long)
                {
                    type = "LongCounter";
                } else
                {
                    // treat as Integer
                    type = "IntCounter";
                }
                mfb.addMetric(type, metricPath, dataObj.toString());
            }
        } else if (dataObj instanceof Date)
        {
            mfb.addMetric("TimeStamp", metricPath,
                          String.valueOf(((Date) dataObj).getTime()));
        } else if (dataObj instanceof Boolean)
        {
            mfb.addMetric("StringEvent", metricPath, dataObj.toString());
        }
    }

    private HostInfo readHostInfoJsonFromUrl(String dockerHostInfo)
    {
        // TODO Auto-generated method stub

        try
        {
            String json = readUrl(dockerHostInfo, true);

            Gson gson = new Gson();
            HostInfo hostInfo = gson.fromJson(json, HostInfo.class);
            hostInfo.populateMetricData(hostInfo);
            return hostInfo;
        } catch (JsonSyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param relativePath
     * @return an ArrayList of Containers
     */

    private ArrayList<Container> readContainerInfoJsonFromUrl(String relativePath)

    {
        String json;
        try
        {
            json = readUrl(relativePath, true);
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray jArray = parser.parse(json).getAsJsonArray();
            upContainer = downContainer = 0;
            ArrayList<Container> lcs = new ArrayList<Container>();

            for (JsonElement obj : jArray)
            {
                Container cse = gson.fromJson(obj, Container.class);
                cse.populateMetricData(cse);
                // Get the resource statistics of the container
                if (cse.getStatus().startsWith("Up"))
                {
                    upContainer++;
                    getContainerResourceStats(cse.getId(), cse.getNames());
                } else
                {
                    downContainer++;
                    resetContainerResourceStats(cse.getId(), cse.getNames());
                }
                lcs.add(cse);
            }
            // Containers page = gson.fromJson(json, Containers.class);
            hostInfo.updateContainerInfo(upContainer, downContainer);
            return lcs;
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    private void resetContainerResourceStats(String id, String names)
    {
        // TODO Auto-generated method stub

        ContainerStatInfo csi = new ContainerStatInfo();
        csi.setCpupercentage((double) 0);
        csi.setMemorypercentage((double) 0);
        csi.setMemporyUsage((long) 0);
        NetworkStatInfo nsi = new NetworkStatInfo();
        nsi.setBytesreceived((long) 0);
        nsi.setPacketsreceived((long) 0);
        nsi.setErrorsreceived((long) 0);
        nsi.setDropsreceived((long) 0);
        nsi.setBytestransmitted((long) 0);
        nsi.setPacketstransmitted((long) 0);
        nsi.setErrorstransmitted((long) 0);
        nsi.setDropstransmitted((long) 0);
        csi.setNetworkData(nsi);
        csi.setTotalMemory((long) 0);
        csi.populateMetricData(csi);
        try
        {
            makeMetrics(csi, names);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getContainerResourceStats(String id, String names)
    {
        // TODO Auto-generated method stub
        String resourcePath = "/containers/" + id + "/stats";
        readStatInfoJsonFromUrl(resourcePath, names);
    }

    private void readStatInfoJsonFromUrl(String resourcePath, String names)
    {
        try
        {
            String json = readUrl(resourcePath, false);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            ContainerStatInfo csi = new ContainerStatInfo();
            csi.setCpupercentage(this.getCPUPercentage(node, names));
            csi.setMemorypercentage(this.getMemoryPercentage(node, names));
            csi.setMemporyUsage(this.getMemoryUsage(node));
            csi.setTotalMemory(this.getTotalMemory(node));
            csi.setNetworkData(getNetWorkData(node));

            csi.populateMetricData(csi);
            makeMetrics(csi, names);
            return;
        } catch (JsonSyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

    private NetworkStatInfo getNetWorkData(JsonNode node)
    {
        // TODO Auto-generated method stub
        JsonNode networkstats = node.get("network");
        if (networkstats != null)
        {
            Long bytesreceived = ((Double) getValue("rx_bytes", networkstats))
                    .longValue();
            Long packetsreceived = ((Double) getValue("rx_packets",
                                                      networkstats))
                    .longValue();
            Long errorsreceived = ((Double) getValue("rx_errors", networkstats))
                    .longValue();
            Long dropsreceived = ((Double) getValue("rx_dropped", networkstats))
                    .longValue();
            Long bytestransmitted = ((Double) getValue("tx_bytes", networkstats))
                    .longValue();
            Long packetstransmitted = ((Double) getValue("tx_packets",
                                                         networkstats))
                    .longValue();
            Long errorstransmitted = ((Double) getValue("tx_errors",
                                                        networkstats))
                    .longValue();
            Long dropstransmitted = ((Double) getValue("tx_dropped",
                                                       networkstats))
                    .longValue();
            NetworkStatInfo nsi = new NetworkStatInfo();
            nsi.setBytesreceived(bytesreceived);
            nsi.setPacketsreceived(packetsreceived);
            nsi.setErrorsreceived(errorsreceived);
            nsi.setDropsreceived(dropsreceived);
            nsi.setBytestransmitted(bytestransmitted);
            nsi.setPacketstransmitted(packetstransmitted);
            nsi.setErrorstransmitted(errorstransmitted);
            nsi.setDropstransmitted(dropstransmitted);
            return nsi;
        }

        return null;
    }

    private Long getTotalMemory(JsonNode node)
    {
        JsonNode stats = node.get("memory_stats");
        if (stats != null)
        {
            Long limit = ((Double) getValue("limit", stats)).longValue();

            return limit / 1000000;
        }
        return null;
    }

    private Long getMemoryUsage(JsonNode node)
    {

        JsonNode stats = node.get("memory_stats");
        if (stats != null)
        {
            Long usage = ((Double) getValue("usage", stats)).longValue();
            return usage / 1000000;
        }
        return null;
    }

    private Double getCPUPercentage(JsonNode node, String containerName)
    {

        JsonNode stats = node.get("cpu_stats");
        JsonNode cpuUsage;
        if (stats != null && (cpuUsage = stats.get("cpu_usage")) != null)
        {
            Double totalUsage = (Double) getValue("total_usage", cpuUsage);
            Double systemUsage = (Double) getValue("system_cpu_usage", stats);
            String totalUsageCacheKey = containerName + "|total_usage";
            String systemUsageCacheKey = containerName + "|system_cpu_usage";
            Double prevTotalUsage = previousMetricsMap
                    .getIfPresent(totalUsageCacheKey);
            Double prevSystemUsage = previousMetricsMap
                    .getIfPresent(systemUsageCacheKey);
            if (prevSystemUsage != null && prevTotalUsage != null
                && totalUsage != null && systemUsage != null)
            {
                Double totalCpuDiff = new Double(totalUsage.doubleValue()
                                                 - prevTotalUsage.doubleValue());
                Double sysUsageDiff = new Double(
                                                 systemUsage.doubleValue()
                                                         - prevSystemUsage
                                                                 .doubleValue());
                return percentage(totalCpuDiff, sysUsageDiff);
            } else
            {
                ;
            }
            if (totalUsage != null && systemUsage != null)
            {
                previousMetricsMap.put(totalUsageCacheKey, totalUsage);
                previousMetricsMap.put(systemUsageCacheKey, systemUsage);
            }
        }

        return null;
    }

    /**
     * This returns the percentage of 2 numbers upto 3 decimals
     * 
     * @param d1
     * @param d2
     * @return
     */
    private static Double percentage(Double d1, Double d2)
    {
        DecimalFormat df = new DecimalFormat();
        Double numerator = new Double(d1) * 100;
        Double result = numerator / d2;
        df.applyPattern(".000");
        return Double.parseDouble(df.format(result));

    }

    /**
     * Identify the memory utilization percentage
     * 
     * @param node
     * @param names
     * @return
     */
    private Double getMemoryPercentage(JsonNode node, String names)
    {
        // TODO Auto-generated method stub
        JsonNode stats = node.get("memory_stats");
        if (stats != null)
        {
            Double usage = (Double) getValue("usage", stats);
            Double limit = (Double) getValue("limit", stats);
            if (usage != null && limit != null)
            {
                // printCollectiveObservedAverage(containerName +
// "|Memory|Current %", percentage(usage, limit));
                return percentage(usage, limit);
            } else
            {
                logger.log(Level.WARNING,
                           "Cannot calculate Memory %, usage={}", usage);
                logger.log(Level.WARNING, "and limit={}", limit);
            }
        } else
        {
            logger.log(Level.WARNING,
                       "The memory stats for container {} is not reported",
                       names);
        }
        return null;
    }

    private Object getValue(String propName, JsonNode node)
    {
        JsonNode jsonNode = node.get(propName);
        if (jsonNode != null)
        {
            if (jsonNode instanceof BooleanNode)
            {
                BooleanNode boolNode = (BooleanNode) jsonNode;
                return boolNode.getBooleanValue() ? true : false;
            } else
            {
                return jsonNode.getBigIntegerValue().doubleValue();
            }
        }
        return null;
    }

    private String readUrl(String urlString, Boolean readfully)
        throws Exception
    {
        BufferedReader reader = null;
        try
        {
            StringBuffer sb = new StringBuffer(this.getDockerMonitor()
                    .getDockerUrl());
            sb.append(urlString);
            URL url = new URL(sb.toString());
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String strLine;
            if (!readfully)
            {
                return reader.readLine();
            }
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();

        } finally
        {
            if (reader != null) reader.close();
        }
    }
}
