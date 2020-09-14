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
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;

public class CsarFileCreator implements BaseFileCreator {

    private static final String CSAR_TEMPLATE_PATH = "./configs/csar_template";

    private static final String TEMPORARY_BASE_PATH = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil
        .getWorkspacePath();

    private static final String CSAR_COMPRESS_DEST_RELATIVE_PATH = "csar";

    private String dirName;

    private String temporaryPath;

    private String appName;

    private String chartFilePath;

    public CsarFileCreator() {
        this.dirName = UUID.randomUUID().toString();
        this.temporaryPath = TEMPORARY_BASE_PATH + dirName;
    }

    @Override
    public void createFileWithTemplate() throws IOException {
        FileUtils.copyDirectory(new File(CSAR_TEMPLATE_PATH), new File(temporaryPath));
    }

    @Override
    public void config() throws IOException {
        // replace chart file name
        replaceAppName();
        // move tgz file to charts dir
        String buf = temporaryPath + File.separator + "Artifacts" + File.separator + "Deployment" + File.separator
            + "Charts";
        FileUtils.moveFileToDirectory(new File(chartFilePath), new File(buf), false);
    }

    @Override
    public String compressFile() throws IOException {
        return CompressFileUtils
            .compressToCsarAndDeleteSrc(temporaryPath, TEMPORARY_BASE_PATH + CSAR_COMPRESS_DEST_RELATIVE_PATH, dirName)
            .getCanonicalPath();
    }

    private void replaceAppName() throws IOException {
        File mainServiceTemplateFile = new File(temporaryPath + File.separator + "MainServiceTemplate.mf");
        FileUtils.writeStringToFile(mainServiceTemplateFile,
            FileUtils.readFileToString(mainServiceTemplateFile, Consts.FILE_ENCODING).replace("{APP_NAME}", appName),
            Consts.FILE_ENCODING);
    }

    public void setChartFilePath(String chartFilePath) {
        this.chartFilePath = chartFilePath;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
