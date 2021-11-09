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

package org.edgegallery.developer.service.application;

import java.util.List;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.springframework.transaction.annotation.Transactional;

public interface AppConfigurationService {

    /**
     * get AppConfiguration
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    public AppConfiguration getAppConfiguration(String applicationId);

    /**
     * modify AppConfiguration
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyAppConfiguration(String applicationId, AppConfiguration appConfiguration);

    /**
     * modify AppConfiguration
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    void deleteAppConfiguration(String applicationId);

    /**
     * get AppConfiguration TrafficRules
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    List<TrafficRule> getAllTrafficRules(String applicationId);

    /**
     * create AppConfiguration TrafficRules
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    TrafficRule createTrafficRules(String applicationId, TrafficRule trafficRule);

    /**
     * modify a AppConfiguration TrafficRule
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyTrafficRule(String applicationId, TrafficRule trafficRule);

    /**
     * delete a  AppConfiguration TrafficRule
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean deleteTrafficRule(String applicationId, String id);

    /**
     * create a  AppConfiguration DnsRule
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    DnsRule createDnsRule(String applicationId, DnsRule dnsRule);

    /**
     * get all  AppConfiguration DnsRules
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    List<DnsRule> getAllDnsRules(String applicationId);

    /**
     * delete a  AppConfiguration DnsRule
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean deleteDnsRule(String applicationId, String ruleId);

    /**
     * modify a  AppConfiguration DnsRule
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyDnsRule(String applicationId, DnsRule dnsRule);

    /**
     * get all  AppConfiguration ServiceProduced
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    List<AppServiceProduced> getAllServiceProduced(String applicationId);

    /**
     * create a   AppConfiguration ServiceProduced
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    AppServiceProduced createServiceProduced(String applicationId, AppServiceProduced serviceProduced);

    /**
     * delete a  AppConfiguration ServiceProduced
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean deleteServiceProduced(String applicationId, String appServiceProducedId);

    /**
     * modify a  AppConfiguration ServiceProduced
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyServiceProduced(String applicationId, String appServiceProducedId,
        AppServiceProduced serviceProduced);

    /**
     * get all  AppConfiguration ServiceRequired
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    List<AppServiceRequired> getAllServiceRequired(String applicationId);

    /**
     * create  AppConfiguration ServiceRequired
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    AppServiceRequired createServiceRequired(String applicationId, AppServiceRequired serviceRequired);

    /**
     * modify a  AppConfiguration ServiceRequired
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyServiceRequired(String applicationId, AppServiceRequired serviceRequired);

    /**
     * delete a   AppConfiguration ServiceRequired
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean deleteServiceRequired(String applicationId, String serviceRequiredId);

    AppCertificate getAppCertificate(String applicationId);

    AppCertificate createAppCertificate(String applicationId, AppCertificate appCertificate);

    Boolean modifyAppCertificate(String applicationId, AppCertificate appCertificate);

    Boolean deleteAppCertificate(String applicationId);
}
