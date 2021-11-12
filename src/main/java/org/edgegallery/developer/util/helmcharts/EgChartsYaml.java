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

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.service.apppackage.converter.CustomRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

@Getter
@Setter
public class EgChartsYaml {
    private String apiVersion;

    private String appVersion;

    private String description;

    private String name;

    private String version;

    public static EgChartsYaml createDefaultCharts() {
        EgChartsYaml defaultCharts = new EgChartsYaml();
        defaultCharts.setApiVersion("v1");
        defaultCharts.setAppVersion("1.0");
        defaultCharts.setVersion("1.0");
        defaultCharts.setName(UUID.randomUUID().toString());
        defaultCharts.setDescription("A Helm chart for Kubernetes");

        return defaultCharts;
    }

    public String getContent() {
        Yaml yaml = new Yaml(new SafeConstructor(), new CustomRepresenter());
        return yaml.dumpAs(this, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
    }
}
