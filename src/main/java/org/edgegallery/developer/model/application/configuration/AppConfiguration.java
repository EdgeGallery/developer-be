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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import com.spencerwi.either.Either;

@Getter
@Setter
@ToString
public class AppConfiguration {

    private AppCertificate appCertificate = new AppCertificate();

    private List<AppServiceProduced> appServiceProducedList = new ArrayList<AppServiceProduced>(0);

    private List<AppServiceRequired> appServiceRequiredList = new ArrayList<AppServiceRequired>(0);

    private List<TrafficRule> trafficRuleList = new ArrayList<TrafficRule>(0);

    private List<DnsRule> dnsRuleList = new ArrayList<DnsRule>(0);

}
