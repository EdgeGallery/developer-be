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
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.application.AppConfigurationMapper;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumApplicationType;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("appConfigurationService")
public class AppConfigurationServiceImpl implements AppConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationServiceImpl.class);

    @Autowired
    private AppConfigurationMapper appConfigurationMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

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
    public Boolean modifyAppConfiguration(String applicationId, AppConfiguration appConfiguration) {
        try {
            appConfigurationMapper.modifyAppCertificate(applicationId, appConfiguration.getAppCertificate());
            for (AppServiceProduced appServiceProduced : appConfiguration.getAppServiceProducedList()) {
                appConfigurationMapper.modifyServiceProduced(applicationId, appServiceProduced);
            }
            for (AppServiceRequired appServiceRequired : appConfiguration.getAppServiceRequiredList()) {
                appConfigurationMapper.modifyServiceRequired(applicationId, appServiceRequired);
            }
            for (TrafficRule trafficRule : appConfiguration.getTrafficRuleList()) {
                appConfigurationMapper.modifyTrafficRule(applicationId, trafficRule);
            }
            for (DnsRule dnsRule : appConfiguration.getDnsRuleList()) {
                appConfigurationMapper.modifyDnsRule(applicationId, dnsRule);
            }
        } catch (Exception e) {
            LOGGER.error("modify appConfiguration failed, appId: {}", applicationId);
            throw new DataBaseException("modify appConfiguration failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }

        return true;
    }

    @Override
    public List<TrafficRule> getAllTrafficRules(String applicationId) {
        return appConfigurationMapper.getAllTrafficRules(applicationId);
    }

    @Override
    public TrafficRule createTrafficRules(String applicationId, TrafficRule trafficRule) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("get application fail by applicationId:{}", applicationId);
            throw new EntityNotFoundException("application is not exit, create failed",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        TrafficRule result = appConfigurationMapper.getTrafficRule(applicationId, trafficRule.getTrafficRuleId());
        if (result != null) {
            LOGGER.error("create trafficRule failed: ruleId have exit");
            throw new DeveloperException("create trafficRule failed: ruleId have exit",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        int res = appConfigurationMapper.createTrafficRule(applicationId, trafficRule);
        if (res < 1) {
            LOGGER.error("create TrafficRule failed");
            throw new DataBaseException("create TrafficRule failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return trafficRule;
    }

    @Override
    public Boolean modifyTrafficRule(String applicationId, TrafficRule trafficRule) {
        int res = appConfigurationMapper.modifyTrafficRule(applicationId, trafficRule);
        if (res < 1) {
            LOGGER.error("modify TrafficRule failed");
            throw new DataBaseException("modify TrafficRule failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
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
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("get application fail by applicationId:{}", applicationId);
            throw new EntityNotFoundException("application is not exit, create failed",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        DnsRule result = appConfigurationMapper.getDnsRule(applicationId, dnsRule.getDnsRuleId());
        if (result != null) {
            LOGGER.error("create DnsRule failed: ruleId have exit");
            throw new EntityNotFoundException("create DnsRule failed: ruleId have exit",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        int res = appConfigurationMapper.createDnsRule(applicationId, dnsRule);
        if (res < 1) {
            LOGGER.error("create DnsRule failed");
            throw new DataBaseException("create DnsRule failed", ResponseConsts.RET_CERATE_DATA_FAIL);
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
            throw new DataBaseException("modify DnsRule failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public List<AppServiceProduced> getAllServiceProduced(String applicationId) {
        return appConfigurationMapper.getAllServiceProduced(applicationId);
    }

    @Override
    public AppServiceProduced createServiceProduced(String applicationId, AppServiceProduced serviceProduced) {
        checkApplicationById(applicationId);
        AppServiceProduced appServiceProduced = appConfigurationMapper
            .getServiceProducedBySerName(applicationId, serviceProduced.getServiceName());
        if (appServiceProduced != null) {
            LOGGER.error("create serviceProduced failed: serName have exit");
            throw new EntityNotFoundException("create serviceProduced failed,serviceName have exit",
                ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        serviceProduced.setAppServiceProducedId(UUID.randomUUID().toString());
        int res = appConfigurationMapper.createServiceProduced(applicationId, serviceProduced);
        if (res < 1) {
            LOGGER.error("create serviceProduced failed");
            throw new DataBaseException("create serviceProduced failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return serviceProduced;
    }

    @Override
    public Boolean deleteServiceProduced(String applicationId, String appServiceProducedId) {
        appConfigurationMapper.deleteServiceProduced(applicationId, appServiceProducedId);
        return true;
    }

    @Override
    public Boolean modifyServiceProduced(String applicationId, String appServiceProducedId,
        AppServiceProduced serviceProduced) {
        AppServiceProduced appServiceProduced = appConfigurationMapper
            .getServiceProduced(applicationId, appServiceProducedId);
        if (appServiceProduced == null) {
            LOGGER.error("param appServiceProducedId is incorrect!");
            throw new IllegalRequestException("appServiceProducedId is incorrect!",
                ResponseConsts.RET_REQUEST_PARAM_ERROR);
        }
        String serviceProducedId = serviceProduced.getAppServiceProducedId();
        if (StringUtils.isEmpty(serviceProducedId) || !serviceProducedId.equals(appServiceProducedId)) {
            serviceProduced.setAppServiceProducedId(appServiceProducedId);
        }
        int res = appConfigurationMapper.modifyServiceProduced(applicationId, serviceProduced);
        if (res < 1) {
            LOGGER.error("modify AppServiceProduced failed");
            throw new DataBaseException("modify AppServiceProduced failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public List<AppServiceRequired> getAllServiceRequired(String applicationId) {
        return appConfigurationMapper.getAllServiceRequired(applicationId);
    }

    @Override
    public AppServiceRequired createServiceRequired(String applicationId, AppServiceRequired serviceRequired) {
        checkApplicationById(applicationId);
        AppServiceRequired appServiceRequired = appConfigurationMapper
            .getServiceRequired(applicationId, serviceRequired.getSerName());
        if (appServiceRequired != null) {
            LOGGER.error("create serviceRequired failed: serName have exit");
            throw new DeveloperException("create serviceRequired failed: serName have exit",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        int res = appConfigurationMapper.createServiceRequired(applicationId, serviceRequired);
        if (res < 1) {
            LOGGER.error("create serviceRequired failed");
            throw new DataBaseException("create serviceRequired failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return serviceRequired;
    }

    @Override
    public Boolean modifyServiceRequired(String applicationId, AppServiceRequired serviceRequired) {
        int res = appConfigurationMapper.modifyServiceRequired(applicationId, serviceRequired);
        if (res < 1) {
            LOGGER.error("modify serviceRequired failed");
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
    public AppCertificate createAppCertificate(String applicationId, AppCertificate appCertificate) {
        checkApplicationById(applicationId);
        int res = appConfigurationMapper.createAppCertificate(applicationId, appCertificate);
        if (res < 1) {
            LOGGER.error("create appCertificate failed");
            throw new DataBaseException("create appCertificate failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return appCertificate;
    }

    @Override
    public Boolean modifyAppCertificate(String applicationId, AppCertificate appCertificate) {
        int res = appConfigurationMapper.modifyAppCertificate(applicationId, appCertificate);
        if (res < 1) {
            LOGGER.error("modify appCertificate failed");
            throw new DataBaseException("modify appCertificate failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean deleteAppCertificate(String applicationId) {
        appConfigurationMapper.deleteAppCertificate(applicationId);
        return true;
    }

    private void checkApplicationById(String applicationId) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("get application fail by applicationId:{}", applicationId);
            throw new EntityNotFoundException("application is not exit, create failed",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        if (application.getAppCreateType() == EnumApplicationType.INTEGRATED) {
            LOGGER.error("application integrated not need app certificate");
            throw new DeveloperException("application integrated not need app certificate",
                ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

}
