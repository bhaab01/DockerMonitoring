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