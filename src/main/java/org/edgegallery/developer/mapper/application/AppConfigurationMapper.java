package org.edgegallery.developer.mapper.application;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;

@Mapper
public interface AppConfigurationMapper {

    List<TrafficRule> getAllTrafficRules(String applicationId);

    int createTrafficRule(@Param("applicationId")String applicationId, @Param("trafficRule")TrafficRule trafficRule);

    int modifyTrafficRule(TrafficRule trafficRule);

    int deleteTrafficRule(String id);

    List<DnsRule> getAllDnsRules(String applicationId);

    int createDnsRule(@Param("applicationId")String applicationId,  @Param("dnsRule")DnsRule dnsRule);

    int modifyDnsRule(DnsRule dnsRule);

    int deleteDnsRule(String id);

    List<AppServiceProduced> getAllServiceProduced(String applicationId);

    int createServiceProduced(@Param("applicationId")String applicationId, @Param("appServiceProduced")AppServiceProduced appServiceProduced);

    int modifyServiceProduced(AppServiceProduced serviceProduced);

    int deleteServiceProduced(String id);

    List<AppServiceRequired> getAllServiceRequired(String applicationId);

    int createServiceRequired(@Param("applicationId")String applicationId, @Param("serviceRequired")AppServiceRequired serviceRequired);

    int modifyServiceRequired(AppServiceRequired serviceRequired);

    int deleteServiceRequired(String id);

    AppCertificate getAppCertificate(String applicationId);

    int createAppCertificate(@Param("applicationId")String applicationId, @Param("appCertificate")AppCertificate appCertificate);

    int modifyAppCertificate(AppCertificate appCertificate);

    int deleteAppCertificate(String id);

}
