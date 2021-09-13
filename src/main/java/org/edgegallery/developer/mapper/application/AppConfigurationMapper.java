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

    TrafficRule getTrafficRule(@Param("applicationId")String applicationId, @Param("ruleId")String ruleId);

    int createTrafficRule(@Param("applicationId")String applicationId, @Param("trafficRule")TrafficRule trafficRule);

    int modifyTrafficRule(@Param("applicationId")String applicationId, @Param("trafficRule")TrafficRule trafficRule);

    int deleteTrafficRule(@Param("applicationId")String applicationId, @Param("ruleId")String ruleId);

    List<DnsRule> getAllDnsRules(String applicationId);

    DnsRule getDnsRule(@Param("applicationId")String applicationId, @Param("ruleId")String ruleId);

    int createDnsRule(@Param("applicationId")String applicationId,  @Param("dnsRule")DnsRule dnsRule);

    int modifyDnsRule(@Param("applicationId")String applicationId,  @Param("dnsRule")DnsRule dnsRule);

    int deleteDnsRule(@Param("applicationId")String applicationId, @Param("ruleId")String ruleId);

    List<AppServiceProduced> getAllServiceProduced(String applicationId);

    AppServiceProduced getServiceProduced(@Param("applicationId")String applicationId, @Param("serName")String serName);

    int createServiceProduced(@Param("applicationId")String applicationId, @Param("serviceProduced")AppServiceProduced serviceProduced);

    int modifyServiceProduced(@Param("applicationId")String applicationId, @Param("serviceProduced")AppServiceProduced serviceProduced);

    int deleteServiceProduced(@Param("applicationId")String applicationId, @Param("serName")String serName);

    List<AppServiceRequired> getAllServiceRequired(String applicationId);

    AppServiceRequired getServiceRequired(@Param("applicationId")String applicationId, @Param("serName")String serName);

    int createServiceRequired(@Param("applicationId")String applicationId, @Param("serviceRequired")AppServiceRequired serviceRequired);

    int modifyServiceRequired(@Param("applicationId")String applicationId, @Param("serviceRequired")AppServiceRequired serviceRequired);

    int deleteServiceRequired(@Param("applicationId")String applicationId, @Param("serName")String serName);

    AppCertificate getAppCertificate(String applicationId);

    int createAppCertificate(@Param("applicationId")String applicationId, @Param("appCertificate")AppCertificate appCertificate);

    int modifyAppCertificate(@Param("applicationId")String applicationId, @Param("appCertificate")AppCertificate appCertificate);

    int deleteAppCertificate(String applicationId);

}
