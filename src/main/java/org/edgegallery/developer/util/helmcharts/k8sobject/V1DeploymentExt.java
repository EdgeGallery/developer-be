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

package org.edgegallery.developer.util.helmcharts.k8sobject;

import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V1DeploymentExt extends V1Deployment implements IContainerImage {
    private static final Logger LOGGER = LoggerFactory.getLogger(V1DeploymentExt.class);

    @Override
    public List<String> getImages() {
        List<String> images = new ArrayList<>();
        try {
            for (V1Container container : this.getSpec().getTemplate().getSpec().getContainers()) {
                if (container.getImage() != null) {
                    images.add(container.getImage());
                }
            }
        } catch (NullPointerException e) {
            LOGGER.info("No images in this template.");
        }
        return images;
    }
}
