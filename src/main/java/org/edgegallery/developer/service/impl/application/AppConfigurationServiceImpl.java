package org.edgegallery.developer.service.impl.application;

import java.util.List;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("appConfigurationService")
public class AppConfigurationServiceImpl implements AppConfigurationService {

    @Override
    public Either<FormatRespDto, AppConfiguration> getAppConfiguration(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyAppConfiguration(String applicationId,
        AppConfiguration appConfiguration) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<TrafficRule>> getAllTrafficRules(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, TrafficRule> createTrafficRules(String applicationId, TrafficRule trafficRule) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyTrafficRules(String applicationId, String ruleId,
        TrafficRule trafficRule) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteTrafficRule(String applicationId, String ruleId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, DnsRule> createDnsRule(String applicationId, DnsRule dnsRule) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<DnsRule>> getAllDnsRules(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteDnsRule(String applicationId, String ruleId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyDnsRule(String applicationId, String ruleId, DnsRule dnsRule) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<AppServiceProduced>> getAllServiceProduced(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, AppServiceProduced> createServiceProduced(String applicationId,
        AppServiceProduced serviceProduced) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteServiceProduced(String applicationId, String serName) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyServiceProduced(String applicationId, String serName,
        AppServiceProduced serviceProduced) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<AppServiceRequired>> getAllServiceRequired(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, AppServiceRequired> createServiceRequired(String applicationId,
        AppServiceRequired serviceRequired) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyServiceRequired(String applicationId, String serName,
        AppServiceRequired serviceRequired) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteServiceRequired(String applicationId, String serName) {
        return null;
    }
}
