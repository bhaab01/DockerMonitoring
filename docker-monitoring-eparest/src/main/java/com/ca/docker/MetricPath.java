package com.ca.docker;


public class MetricPath {

    private StringBuilder sb = new StringBuilder();

    public MetricPath(final String base) {
        sb.append(base);
    }

    public void addElement(final String nm) {
        sb.append(Constants.PIPE);
        sb.append(translate(nm));
    }

    public void addMetric(final String nm) {
        sb.append(Constants.COLON);
        sb.append(translate(nm));
    }

    private String translate(final String in) {
        return in.replace(Constants.PIPE, Constants.UNDER_SCORE).replace(Constants.COLON, Constants.SEMI_COLON);
    }

    public String toString() {
        return sb.toString();
    }
}