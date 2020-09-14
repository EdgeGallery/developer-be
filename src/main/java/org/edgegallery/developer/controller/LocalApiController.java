/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SwaggerService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestSchema(schemaId = "localapi")
@RequestMapping("/mec/developer/v1/localapi")
public class LocalApiController {

    //length of file name limit under 255
    private static final int MAX_FILE_NAME_LENGTH = 255;

    @Autowired
    private SwaggerService swaggerService;

    /**
     * get file from swagger.
     */
    @ApiOperation(value = "query all local api", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileName}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<byte[]> getFile(@ApiParam(value = "fileName", required = true) @PathVariable("fileName")
        @Length(max = MAX_FILE_NAME_LENGTH) String fileName) {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = swaggerService.getFile(fileName);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }
}
