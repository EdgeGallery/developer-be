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

import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.edgegallery.developer.util.ImageConfig;
import org.springframework.util.CollectionUtils;

public abstract class AbstractDefaultFileHandler implements IContainerFileHandler {

    @Setter
    private boolean hasMep;

    @Setter
    private ImageConfig imageConfig;

    @Setter
    private List<EgValuesYaml.ServiceConfig> serviceConfig;

    EgValuesYaml getDefaultValues() {
        EgValuesYaml defaultValues = EgValuesYaml.createDefaultEgValues(hasMep);
        if (imageConfig != null) {
            defaultValues.getImageLocation().setDomainName(imageConfig.getDomainname());
            defaultValues.getImageLocation().setProject(imageConfig.getProject());
        }

        if (!CollectionUtils.isEmpty(serviceConfig)) {
            defaultValues.setServiceConfig(serviceConfig);
        }
        return defaultValues;
    }

    EgChartsYaml getDefaultChart(String helmChartsName) {
        EgChartsYaml defaultCharts = EgChartsYaml.createDefaultCharts();
        defaultCharts.setName(helmChartsName.replaceAll("_", "-") + "-" + RandomStringUtils.randomNumeric(8));
        return defaultCharts;
    }
}
