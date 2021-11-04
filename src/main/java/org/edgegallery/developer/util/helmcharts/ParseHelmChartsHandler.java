/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.util.helmcharts;

import hapi.chart.ChartOuterClass.ChartOrBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.microbean.helm.chart.DirectoryChartLoader;

public class ParseHelmChartsHandler {

    public void loadCharts(String filePath) throws IOException {
        Path path = new File(filePath).toPath();
        URI uri = URI.create("");
        ChartOrBuilder chart = null;
        DirectoryChartLoader chartLoader = new DirectoryChartLoader();
        chart = chartLoader.load(path);
        System.out.println(chart.getMetadata().getName());
        // chart.getDependencies(0).getTemplates(0);
        chart.getTemplates(0).getAllFields();
    }

    public static void main(String[] args) throws IOException {
        ParseHelmChartsHandler handler = new ParseHelmChartsHandler();
        handler.loadCharts("D:\\test\\face2");

    }
}
