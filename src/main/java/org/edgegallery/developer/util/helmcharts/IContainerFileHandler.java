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

public interface IContainerFileHandler {

    // load tgz or yaml file, and parse it. it will auto-create charts.yaml and values.yaml when loading yaml file.
    void load(String filePath);

    // get catalog from helm-charts file
    void getCatalog();

    String exportHelmCharts(String outPath);

    void SetHashMep(boolean hasMep);

    void modifyFileByPath(String filePath, String content);

    void addFile(String filePath, String content);
}
