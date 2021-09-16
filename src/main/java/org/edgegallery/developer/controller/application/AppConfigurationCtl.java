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
package org.edgegallery.developer.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RestSchema(schemaId = "appConfiguration")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "appConfiguration")
@Validated
public class AppConfigurationCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private AppConfigurationService appConfigurationService;
    /**
     * get app configuration.
     */
    @ApiOperation(value = "get one application.", response = AppConfiguration.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppConfiguration.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppConfiguration> getAppConfiguration(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAppConfiguration(applicationId));
    }

    /**
     * modify app configuration.
     */
    @ApiOperation(value = "put one application.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyAppConfiguration(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "AppConfiguration", required = true) @RequestBody AppConfiguration appConfiguration)
    {
        return ResponseEntity.ok(appConfigurationService.modifyAppConfiguration(applicationId, appConfiguration));
    }

    /**
     * get all app traffic rules.
     */
    @ApiOperation(value = "get all app traffic rules.", response = TrafficRule.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TrafficRule.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/trafficrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<TrafficRule>> getTrafficRules(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAllTrafficRules(applicationId));
    }

    /**
     * create app traffic rule.
     */
    @ApiOperation(value = "create app traffic rule.", response = TrafficRule.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TrafficRule.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/trafficrules", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<TrafficRule> createTrafficRules(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "TrafficRule", required = true) @RequestBody TrafficRule trafficRule){
        TrafficRule result = appConfigurationService.createTrafficRules(applicationId, trafficRule);
        return ResponseEntity.ok(result);
    }

    /**
     * modify app traffic rule.
     */
    @ApiOperation(value = "modify app traffic rule.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/trafficrules/{ruleId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyTrafficRule(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "ruleId", required = true) @PathVariable("ruleId") String ruleId,
        @NotNull @ApiParam(value = "TrafficRule", required = true) @RequestBody TrafficRule trafficRule){
        Boolean result = appConfigurationService.modifyTrafficRule(applicationId, trafficRule);
        return ResponseEntity.ok(result);
    }

    /**
     * delete a app traffic rule.
     */
    @ApiOperation(value = "delete a app traffic rule.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/trafficrules/{ruleId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteTrafficRule(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "ruleId", required = true) @PathVariable("ruleId") String ruleId){
        Boolean result = appConfigurationService.deleteTrafficRule(applicationId, ruleId);
        return ResponseEntity.ok(result);
    }

    /**
     * get all app dns rule.
     */
    @ApiOperation(value = "get all app dns rule.", response = DnsRule.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = DnsRule.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/dnsrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<DnsRule>> getAllDnsRules(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAllDnsRules(applicationId));
    }

    /**
     * create a app dns rule.
     */
    @ApiOperation(value = "create app dns rule.", response = DnsRule.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = DnsRule.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/dnsrules", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<DnsRule> createDnsRule(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "DnsRule", required = true) @RequestBody DnsRule dnsRule){
        DnsRule result = appConfigurationService.createDnsRule(applicationId, dnsRule);
        return ResponseEntity.ok(result);
    }

    /**
     * modify a app dns rule.
     */
    @ApiOperation(value = "modify a app dns rule.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/dnsrules/{ruleId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyDnsRule(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "ruleId", required = true) @PathVariable("ruleId") String ruleId,
        @NotNull @ApiParam(value = "DnsRule", required = true) @RequestBody DnsRule dnsRule){
        Boolean result = appConfigurationService.modifyDnsRule(applicationId, dnsRule);
        return ResponseEntity.ok(result);
    }

    /**
     * delete a app dns rule.
     */
    @ApiOperation(value = "delete  a app dns rule.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/dnsrules/{ruleId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteDnsRule(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "ruleId", required = true) @PathVariable("ruleId") String ruleId){
        Boolean result = appConfigurationService.deleteDnsRule(applicationId, ruleId);
        return ResponseEntity.ok(result);
    }

    /**
     * get all app service produced.
     */
    @ApiOperation(value = "get all app service produced.", response = AppServiceProduced.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppServiceProduced.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/serviceproduceds", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<AppServiceProduced>> getAllServiceProduced(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAllServiceProduced(applicationId));
    }

    /**
     * create a app service produced.
     */
    @ApiOperation(value = "create a app service produced.", response = AppServiceProduced.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppServiceProduced.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/serviceproduceds", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppServiceProduced> createServiceProduced(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "ServiceProduced", required = true) @RequestBody AppServiceProduced serviceProduced){
        AppServiceProduced result = appConfigurationService.createServiceProduced(applicationId, serviceProduced);
        return ResponseEntity.ok(result);
    }

    /**
     * modify a app service produced.
     */
    @ApiOperation(value = "modify a app service produced.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/serviceproduceds/{serName}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyServiceProduced(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "serName", required = true) @PathVariable("serName") String serName,
        @NotNull @ApiParam(value = "ServiceProduced", required = true) @RequestBody AppServiceProduced serviceProduced){
        Boolean result = appConfigurationService.modifyServiceProduced(applicationId, serviceProduced);
        return ResponseEntity.ok(result);
    }

    /**
     * delete a app service produced.
     */
    @ApiOperation(value = "delete  app service produced.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/serviceproduceds/{serName}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteServiceProduced(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "serName", required = true) @PathVariable("serName") String serName){
        Boolean result = appConfigurationService.deleteServiceProduced(applicationId, serName);
        return ResponseEntity.ok(result);
    }

    /**
     * get all app service required.
     */
    @ApiOperation(value = "get all app service required.", response = AppServiceRequired.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppServiceRequired.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/servicerequireds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<AppServiceRequired>> getAllServiceRequired(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAllServiceRequired(applicationId));
    }

    /**
     * create a app service required.
     */
    @ApiOperation(value = "create a app service required.", response = AppServiceRequired.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppServiceRequired.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/servicerequireds", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppServiceRequired> createServiceRequired(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "ServiceRequired", required = true) @RequestBody AppServiceRequired serviceRequired){
        AppServiceRequired result = appConfigurationService.createServiceRequired(applicationId, serviceRequired);
        return ResponseEntity.ok(result);
    }

    /**
     * modify a app service required.
     */
    @ApiOperation(value = "modify a app service required.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/servicerequireds/{serName}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyServiceRequired(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "serName", required = true) @PathVariable("serName") String serName,
        @NotNull @ApiParam(value = "ServiceRequired", required = true) @RequestBody AppServiceRequired serviceRequired){
        Boolean result = appConfigurationService.modifyServiceRequired(applicationId, serviceRequired);
        return ResponseEntity.ok(result);
    }

    /**
     * delete a app service produced.
     */
    @ApiOperation(value = "delete  app service produced.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/servicerequireds/{serName}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteServiceRequired(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "serName", required = true) @PathVariable("serName") String serName){
        Boolean result = appConfigurationService.deleteServiceRequired(applicationId, serName);
        return ResponseEntity.ok(result);
    }

    /**
     * get App Certificate.
     */
    @ApiOperation(value = "get App Certificate.", response = AppCertificate.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppCertificate.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/appcertificate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppCertificate> getAppCertificate(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        return ResponseEntity.ok(appConfigurationService.getAppCertificate(applicationId));
    }

    /**
     * create a App Certificate.
     */
    @ApiOperation(value = "create a App Certificate.", response = AppCertificate.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppCertificate.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/appcertificate", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppCertificate> createAppCertificate(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "AppCertificate", required = true) @RequestBody AppCertificate appCertificate){
        AppCertificate result = appConfigurationService.createAppCertificate(applicationId, appCertificate);
        return ResponseEntity.ok(result);
    }

    /**
     * modify a App Certificate.
     */
    @ApiOperation(value = "modify a App Certificate.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/appcertificate", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyAppCertificate(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "AppCertificate", required = true) @RequestBody AppCertificate appCertificate){
        Boolean result = appConfigurationService.modifyAppCertificate(applicationId, appCertificate);
        return ResponseEntity.ok(result);
    }

    /**
     * delete a App Certificate.
     */
    @ApiOperation(value = "delete a App Certificate.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/appconfiguration/appcertificate", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteAppCertificate(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        Boolean result = appConfigurationService.deleteAppCertificate(applicationId);
        return ResponseEntity.ok(result);
    }

}
