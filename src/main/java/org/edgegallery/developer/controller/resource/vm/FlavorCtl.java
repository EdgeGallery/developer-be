/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.controller.resource.vm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.recource.vm.FlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RestSchema(schemaId = "flavors")
@RequestMapping("/mec/developer/v2/flavors")
@Api(tags = "flavors")
public class FlavorCtl {

    @Autowired
    FlavorService flavorService;

    /**
     * getALLFlavors.
     *
     * @return
     */
    @ApiOperation(value = "get all flavor", response = Flavor.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Flavor.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<Flavor>> getAllFavors()  {
        return ResponseEntity.ok(flavorService.getAllFavors());
    }

    /**
     * getFlavorById.
     *
     * @return
     */
    @ApiOperation(value = "get a flavor by id", response = Flavor.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Flavor.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{flavorId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Flavor> getFavorById(
        @ApiParam(value = "flavorId", required = true) @PathVariable("flavorId") String flavorId)  {
        return ResponseEntity.ok(flavorService.getFavorById(flavorId));
    }

    /**
     * createFlavor.
     *
     * @return
     */
    @ApiOperation(value = "create a flavor", response = Flavor.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Flavor.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Flavor> createFavor(
        @NotNull @ApiParam(value = "Flavor", required = true) @RequestBody Flavor flavor)  {
        return ResponseEntity.ok(flavorService.createFavor(flavor));
    }

    /**
     * deleteFlavorById.
     *
     * @return
     */
    @ApiOperation(value = "delete a flavor by id", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{flavorId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteFavorById(
        @ApiParam(value = "flavorId", required = true) @PathVariable("flavorId") String flavorId)  {
        return ResponseEntity.ok(flavorService.deleteFavorById(flavorId));
    }

}
