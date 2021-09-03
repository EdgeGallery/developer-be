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

package org.edgegallery.developer.model.application.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;

@Getter
@Setter
@ToString
public class AppConfiguration {

    private AppCertificate appCertificate;

    private List<AppServiceProduced> appServiceProducedList;

    private List<AppServiceRequired> appServiceRequiredList;

    private List<TrafficRule> trafficRuleList;

    private List<DnsRule> dnsRuleList;
}
