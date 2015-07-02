package com.ca.docker;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class MetricFeedBundle {
    private List<MetricInfo> metrics;

    public MetricFeedBundle() {
    }

    public List<MetricInfo> getMetrics() {
        return metrics;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public void addMetric(
        final String mtype,
        final String mname,
        final String mvalue
    ) {
        if (metrics == null) {
            metrics = new ArrayList<MetricInfo>();
        }
        metrics.add(new MetricInfo(mtype, mname, mvalue));
    }

    public static class MetricInfo {
        private String type;
        private String name;
        private String value;

        public MetricInfo() {
        }

        public MetricInfo(
            final String intype,
            final String inname,
            final String invalue
        ) {
            type = intype;
            name = inname;
            value = invalue;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}