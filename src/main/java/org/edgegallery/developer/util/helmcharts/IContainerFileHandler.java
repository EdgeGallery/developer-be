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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.util.ImageConfig;

public interface IContainerFileHandler extends Closeable {

    // load tgz or yaml file, and parse it. it will auto-create charts.yaml and values.yaml when loading yaml file.
    // create a template dir to parse the input files, please using clean() to delete the temp files after using.
    void load(String... filePaths) throws IOException;

    // get catalog from helm-charts file.
    List<HelmChartFile> getCatalog();

    // export tgz package.
    String exportHelmChartsPackage();

    void setHasMep(boolean hasMep);

    void setImageConfig(ImageConfig imageConfig);

    // innerPath is the path of file in the tgz. Can get innerPath from the object of HelmChartFile.
    String getContentByInnerPath(String innerPath);

    // innerPath is the path of file in the tgz.
    boolean modifyFileByPath(String innerPath, String content);

    // innerPath is the path of file in the tgz.
    void addFile(String innerPath, String content);

    // parse the k8s file by kubernetes-client. If the file contains value-params, maybe need
    List<Object> getK8sTemplateObject(HelmChartFile innerFile);

    // clean the temp dir and data.
    void clean();

    List<HelmChartFile> getTemplatesFile();
}
