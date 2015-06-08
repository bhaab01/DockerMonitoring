/*
 * Copyright (c) 2014 CA.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * IN NO EVENT WILL CA BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS
 * OR DAMAGE, DIRECT OR INDIRECT, FROM THE USE OF THIS MATERIAL,
 * INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION, GOODWILL,
 * OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

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