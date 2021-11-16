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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.util.CompressFileUtils;

public class LoadHelmChartsFileHandler extends ContainerFileHandlerImp {

    @Setter
    private boolean hasMep;

    private String filePath;

    @Override
    public void load(String filePath) throws IOException {
        try {
            // create helm-charts temp dir
            Path tempDir = Files.createTempDirectory("eg-helmcharts-");
            workspace = tempDir.toString();

            File orgFile = new File(filePath);
            String fileName = orgFile.getName();
            Path targetFilePath = Files.createFile(Paths.get(workspace, fileName));
            FileUtils.copyFile(orgFile, targetFilePath.toFile());

            Path helmChartPath = Files.createDirectory(Paths.get(workspace,
                fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName));

            // unzip file
            if (!CompressFileUtils.decompress(targetFilePath.toString(), workspace)) {
                clean();
                return;
            }
            helmChartsDir = helmChartPath.toString();
        } catch (IOException e) {
            FileUtils.deleteDirectory(new File(workspace));
            workspace = null;
            throw new DeveloperException("Failed to read k8s config. config:" + filePath);
        }
    }

}
