/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;

public class ChartFileCreator implements BaseFileCreator {

    private static final String CHART_TEMPLATE_PATH = "./configs/chart_template";

    private static final String TEMPORARY_BASE_PATH = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil
        .getWorkspacePath();

    private static final String CHART_COMPRESS_DEST_RELATIVE_PATH = "chart";

    private String dirName;

    private String temporaryPath;

    private String chartName;

    private Map<String, String> yamlNameToContentMap = new HashMap<>();

    public ChartFileCreator() {
        this.dirName = UUID.randomUUID().toString();
        this.temporaryPath = TEMPORARY_BASE_PATH + dirName;
    }

    @Override
    public void createFileWithTemplate() throws IOException {
        FileUtils.copyDirectory(new File(CHART_TEMPLATE_PATH), new File(TEMPORARY_BASE_PATH + dirName));
    }

    @Override
    public void config() throws IOException {
        generateTemplateYaml();
        replaceChartYaml();
    }

    @Override
    public String compressFile() throws IOException {
        return CompressFileUtils
            .compressToTgzAndDeleteSrc(temporaryPath, TEMPORARY_BASE_PATH + CHART_COMPRESS_DEST_RELATIVE_PATH, dirName)
            .getCanonicalPath();
    }

    private void generateTemplateYaml() throws IOException {
        for (Map.Entry<String, String> entry : yamlNameToContentMap.entrySet()) {
            String name = entry.getKey();
            String content = entry.getValue();
            File file = new File(temporaryPath + File.separator + "templates" + File.separator + name);
            FileUtils.writeStringToFile(file, content, Consts.FILE_ENCODING);
        }
    }

    private void replaceChartYaml() throws IOException {
        File chartYamlFile = new File(temporaryPath + File.separator + "Chart.yaml");
        FileUtils.writeStringToFile(chartYamlFile,
            FileUtils.readFileToString(chartYamlFile, Consts.FILE_ENCODING).replace("{CHART_NAME}", chartName),
            Consts.FILE_ENCODING);
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public void addTemplateYaml(String name, String content) {
        yamlNameToContentMap.put(name, content);
    }
}
