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
package org.edgegallery.developer.service.application.impl;

import java.util.List;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.AppConfigurationMapper;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.service.SystemImageMgmtService;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("appConfigurationService")
public class AppConfigurationServiceImpl implements AppConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    @Autowired
    private AppConfigurationMapper appConfigurationMapper;

    @Override
    public AppConfiguration getAppConfiguration(String applicationId) {
        AppConfiguration appConfiguration = new AppConfiguration();
        appConfiguration.setAppCertificate(appConfigurationMapper.getAppCertificate(applicationId));
        appConfiguration.setAppServiceProducedList(appConfigurationMapper.getAllServiceProduced(applicationId));
        appConfiguration.setAppServiceRequiredList(appConfigurationMapper.getAllServiceRequired(applicationId));
        appConfiguration.setTrafficRuleList(appConfigurationMapper.getAllTrafficRules(applicationId));
        appConfiguration.setDnsRuleList(appConfigurationMapper.getAllDnsRules(applicationId));
        return appConfiguration;
    }

    @Override
    public Boolean modifyAppConfiguration(String applicationId,
        AppConfiguration appConfiguration) {
        try {
            appConfigurationMapper.modifyAppCertificate(applicationId, appConfiguration.getAppCertificate());
            for (AppServiceProduced appServiceProduced:appConfiguration.getAppServiceProducedList()) {
                appConfigurationMapper.modifyServiceProduced(applicationId, appServiceProduced);
            }
            for (AppServiceRequired appServiceRequired:appConfiguration.getAppServiceRequiredList()) {
                appConfigurationMapper.modifyServiceRequired(applicationId, appServiceRequired);
            }
            for (TrafficRule trafficRule: appConfiguration.getTrafficRuleList()) {
                appConfigurationMapper.modifyTrafficRule(applicationId, trafficRule);
            }
            for (DnsRule dnsRule: appConfiguration.getDnsRuleList()) {
                appConfigurationMapper.modifyDnsRule(applicationId, dnsRule);
            }
        } catch (Exception e) {
            LOGGER.error("modify appConfiguration failed");
            throw new DeveloperException("modify appConfiguration failed", ResponseConsts.MODIFY_DATA_FAILED);
        }

        return true;
    }

    @Override
    public List<TrafficRule> getAllTrafficRules(String applicationId) {
        return appConfigurationMapper.getAllTrafficRules(applicationId);
    }

    @Override
    public TrafficRule createTrafficRules(String applicationId, TrafficRule trafficRule) {
        TrafficRule result = appConfigurationMapper.getTrafficRule(applicationId, trafficRule.getTrafficRuleId());
        if (result != null) {
            LOGGER.error("create trafficRule failed: ruleId have exit");
            throw new DeveloperException("create trafficRule failed: ruleId have exit", ResponseConsts.INSERT_DATA_FAILED);
        }
        int res = appConfigurationMapper.createTrafficRule(applicationId,trafficRule);
        if (res < 1) {
            LOGGER.error("create TrafficRule failed");
            throw new DeveloperException("create TrafficRule failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        return trafficRule;
    }

    @Override
    public Boolean modifyTrafficRule(String applicationId, TrafficRule trafficRule) {
        int res = appConfigurationMapper.modifyTrafficRule(applicationId, trafficRule);
        if (res < 1) {
            LOGGER.error("modify TrafficRule failed");
            throw new DeveloperException("modify TrafficRule failed", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public Boolean deleteTrafficRule(String applicationId, String ruleId) {
        appConfigurationMapper.deleteTrafficRule(applicationId, ruleId);
        return true;
    }

    @Override
    public DnsRule createDnsRule(String applicationId, DnsRule dnsRule) {
        DnsRule result = appConfigurationMapper.getDnsRule(applicationId, dnsRule.getDnsRuleId());
        if (result != null) {
            LOGGER.error("create DnsRule failed: ruleId have exit");
            throw new DeveloperException("create DnsRule failed: ruleId have exit", ResponseConsts.INSERT_DATA_FAILED);
        }
        int res = appConfigurationMapper.createDnsRule(applicationId,dnsRule);
        if (res < 1) {
            LOGGER.error("create DnsRule failed");
            throw new DeveloperException("create DnsRule failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        return dnsRule;
    }

    @Override
    public List<DnsRule> getAllDnsRules(String applicationId) {
        return appConfigurationMapper.getAllDnsRules(applicationId);
    }

    @Override
    public Boolean deleteDnsRule(String applicationId, String ruleId) {
        appConfigurationMapper.deleteDnsRule(applicationId, ruleId);
        return true;
    }

    @Override
    public Boolean modifyDnsRule(String applicationId, DnsRule dnsRule) {
        int res = appConfigurationMapper.modifyDnsRule(applicationId, dnsRule);
        if (res < 1) {
            LOGGER.error("modify DnsRule failed");
            throw new DeveloperException("modify DnsRule failed", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public List<AppServiceProduced> getAllServiceProduced(String applicationId) {
        return appConfigurationMapper.getAllServiceProduced(applicationId);
    }

    @Override
    public AppServiceProduced createServiceProduced(String applicationId,
        AppServiceProduced serviceProduced) {
        AppServiceProduced appServiceProduced = appConfigurationMapper.getServiceProduced(applicationId, serviceProduced.getSerName());
        if (appServiceProduced != null) {
            LOGGER.error("create serviceProduced failed: serName have exit");
            throw new DeveloperException("create serviceProduced failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        int res = appConfigurationMapper.createServiceProduced(applicationId,serviceProduced);
        if (res < 1) {
            LOGGER.error("create serviceProduced failed");
            throw new DeveloperException("create serviceProduced failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        return serviceProduced;
    }

    @Override
    public Boolean deleteServiceProduced(String applicationId, String serName) {
        appConfigurationMapper.deleteServiceProduced(applicationId, serName);
        return true;
    }

    @Override
    public Boolean modifyServiceProduced(String applicationId, AppServiceProduced serviceProduced) {
        int res = appConfigurationMapper.modifyServiceProduced(applicationId, serviceProduced);
        if (res < 1) {
            LOGGER.error("modify AppServiceProduced failed");
            throw new DeveloperException("modify AppServiceProduced failed", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public List<AppServiceRequired> getAllServiceRequired(String applicationId) {
        return appConfigurationMapper.getAllServiceRequired(applicationId);
    }

    @Override
    public AppServiceRequired createServiceRequired(String applicationId,
        AppServiceRequired serviceRequired) {
        AppServiceRequired appServiceRequired = appConfigurationMapper.getServiceRequired(applicationId, serviceRequired.getSerName());
        if (appServiceRequired != null) {
            LOGGER.error("create serviceRequired failed: serName have exit");
            throw new DeveloperException("create serviceRequired failed: serName have exit", ResponseConsts.INSERT_DATA_FAILED);
        }
        int res = appConfigurationMapper.createServiceRequired(applicationId,serviceRequired);
        if (res < 1) {
            LOGGER.error("create serviceRequired failed");
            throw new DeveloperException("create serviceRequired failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        return serviceRequired;
    }

    @Override
    public Boolean modifyServiceRequired(String applicationId, AppServiceRequired serviceRequired) {
        int res = appConfigurationMapper.modifyServiceRequired(applicationId, serviceRequired);
        if (res < 1) {
            LOGGER.error("modify serviceRequired failed");
            throw new DeveloperException("modify serviceRequired failed", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public Boolean deleteServiceRequired(String applicationId, String serName) {
        appConfigurationMapper.deleteServiceRequired(applicationId, serName);
        return true;
    }

    @Override
    public AppCertificate getAppCertificate(String applicationId) {
        return appConfigurationMapper.getAppCertificate(applicationId);
    }

    @Override
    public AppCertificate createAppCertificate(String applicationId,
        AppCertificate appCertificate) {
        int res = appConfigurationMapper.createAppCertificate(applicationId,appCertificate);
        if (res < 1) {
            LOGGER.error("create appCertificate failed");
            throw new DeveloperException("create appCertificate failed", ResponseConsts.INSERT_DATA_FAILED);
        }
        return appCertificate;
    }

    @Override
    public Boolean modifyAppCertificate(String applicationId, AppCertificate appCertificate) {
        int res = appConfigurationMapper.modifyAppCertificate(applicationId, appCertificate);
        if (res < 1) {
            LOGGER.error("modify appCertificate failed");
            throw new DeveloperException("modify appCertificate failed", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public Boolean deleteAppCertificate(String applicationId) {
        appConfigurationMapper.deleteAppCertificate(applicationId);
        return true;
    }


}
