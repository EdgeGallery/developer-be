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

package org.edgegallery.developer.model.apppackage.appd.appconfiguration;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;

@Setter
@Getter
public class ConfigurationProperty {

    private AppCertificate appCertificate = new AppCertificate();

    private List<AppServiceRequiredDef> appServiceRequired = new ArrayList<AppServiceRequiredDef>(0);

    private List<AppServiceProducedDef> appServiceProduced = new ArrayList<AppServiceProducedDef>(0);

    private boolean appSupportMp1 = true;

    private String appName;

    private List<TrafficRule> appTrafficRule = new ArrayList<TrafficRule>(0);

    private List<DnsRule> appDNSRule = new ArrayList<DnsRule>(0);
}
