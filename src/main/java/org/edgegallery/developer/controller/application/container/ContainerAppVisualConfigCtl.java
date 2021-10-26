package org.edgegallery.developer.controller.application.container;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.application.container.ContainerAppVisualConfigService;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "containerAppVisualConfig")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "containerAppVisualConfig")
@Validated
public class ContainerAppVisualConfigCtl {

    @Autowired
    private ContainerAppVisualConfigService appVisualConfigService;

    /**
     * Visual configuration, generating deployment yaml.
     */
    @ApiOperation(value = "genarate deployment yaml", response = HelmChart.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmChart.class)
    })
    @RequestMapping(value = "/{applicationId}/visualconfiguration", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmChart> saveDeploy(@RequestBody String configJsonData,
        @ApiParam(value = "applicationId", required = true) @PathVariable String applicationId,
        @ApiParam(value = "configType", required = true) @RequestParam String configType) {
        return ResponseEntity.ok(appVisualConfigService.saveDeployYaml(configJsonData, applicationId, configType));
    }

    /**
     * Modify the generated deployment yaml.
     */
    @ApiOperation(value = "modify the generated deployment yaml", response = HelmTemplateYamlPo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class)
    })
    @RequestMapping(value = "/visualconfiguration/{yamlFileId}", method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> updateDeployYaml(
        @ApiParam(value = "yamlFileId", required = true) @PathVariable String yamlFileId,
        @ApiParam(value = "yamlContent", required = true) @RequestBody String yamlContent) {
        return ResponseEntity.ok(appVisualConfigService.updateDeployYaml(yamlFileId, yamlContent));
    }

    /**
     * Get the generated deployment yaml,Edit file use.
     */
    @ApiOperation(value = "get the generated deployment yaml", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class)
    })
    @RequestMapping(value = "/visualconfiguration/{yamlFileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> getDeployYaml(
        @ApiParam(value = "yamlFileId", required = true) @PathVariable String yamlFileId) {
        return ResponseEntity.ok(appVisualConfigService.getDeployYaml(yamlFileId));
    }

    /**
     * Get the generated deployment yaml,Echo use in configured tables.
     */
    @ApiOperation(value = "get the generated deployment yaml as list", response = List.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/visualconfiguration/{yamlFileId}/list", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<String>> queryDeployYaml(
        @ApiParam(value = "yamlFileId", required = true) @PathVariable String yamlFileId) {
        return ResponseEntity.ok(appVisualConfigService.getDeployYamlAsList(yamlFileId));
    }

}
