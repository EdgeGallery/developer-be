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

package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("swaggerService")
public class SwaggerService {

    private static final String API_PATH = "./configs/api";

    private static final Logger log = LoggerFactory.getLogger(SwaggerService.class);

    /**
     * getFile.
     *
     * @return
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> getFile(String fileName) {
        if (!isInclude(fileName)) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "fileName is invalid.");
            return Either.left(error);
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/octet-stream");
            headers.add("Content-Disposition", "attachment; filename=" + fileName + ".yaml");
            byte[] bytes = FileUtils.readFileToByteArray(new File(API_PATH + File.separator + fileName + ".yaml"));
            log.info("get file success {}.yaml", fileName);
            return Either.right(ResponseEntity.ok().headers(headers).body(bytes));
        } catch (IOException e) {
            log.error("get file failed, {}", e.getMessage());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "get file failed.");
            return Either.left(error);
        }
    }

    private static  boolean isInclude(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        String[] fileNames = {"plugin", "testapp", "hosts", "files", "capability-groups", "projects"};
        for (String name : fileNames) {
            if (name.equals(fileName)) {
                return true;
            }
        }
        return false;
    }
}
