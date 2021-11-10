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

import lombok.Setter;

public class LoadHelmChartsFileHandler implements IContainerFileHandler {

    private final String filePath;

    @Setter
    private boolean hasMep;

    LoadHelmChartsFileHandler(String filePath) {
        this.filePath = filePath;
    }

    public void loadHelmCharts(String filePath) {

    }

    @Override
    public void load(String filePath) {

    }

    @Override
    public void getCatalog() {

    }

    @Override
    public String exportHelmCharts(String outPath) {
        return null;
    }

    @Override
    public void modifyFileByPath(String filePath, String content) {

    }

    @Override
    public void addFile(String filePath, String content) {

    }
}
