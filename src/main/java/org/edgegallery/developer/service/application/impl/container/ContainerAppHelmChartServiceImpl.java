/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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
package org.edgegallery.developer.service.application.impl.container;

import java.util.List;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service("containerAppHelmChartService")
public class ContainerAppHelmChartServiceImpl implements ContainerAppHelmChartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppHelmChartServiceImpl.class);

    @Override
    public Boolean uploadHelmChartYaml(MultipartFile helmTemplateYaml, String applicationId) {
        // upload helm chart yaml
        return null;
    }

    @Override
    public List<HelmChart> getHelmChartList(String applicationId) {
        return null;
    }

    @Override
    public HelmChart getHelmChartById(String applicationId, String id) {
        return null;
    }

    @Override
    public Boolean deleteHelmChartById(String applicationId, String id) {
        return null;
    }
}
