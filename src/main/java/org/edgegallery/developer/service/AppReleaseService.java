/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("appReleaseService")
public class AppReleaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppReleaseService.class);

    /**
     * getPkgStruById.
     */
    public Either<FormatRespDto, AppPkgStructure> getPkgStruById(String projectId, String csarId) {
        if (projectId == null || projectId.equals("")) {
            LOGGER.error("project id can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!");
            return Either.left(error);
        }
        String csarPath = getProjectPath(projectId);
        if (csarPath.equals("")) {
            LOGGER.error("can not find this project!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find this project!");
            return Either.left(error);
        }
        // get csar pkg structure
        AppPkgStructure structure;
        try {
            structure = getFiles(getProjectPath(projectId) + csarId + File.separator, new AppPkgStructure());
        } catch (IOException ex) {
            LOGGER.error("get csar pkg occur io exception: {}", ex.getMessage());
            String message = "get csar pkg occur io exception!";
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, message);
            return Either.left(error);
        }
        return Either.right(structure);
    }

    /**
     * getPkgContentByFileName.
     */
    public Either<FormatRespDto, String> getPkgContentByFileName(String projectId, String fileName) {
        if (projectId == null || projectId.equals("")) {
            LOGGER.error("project id can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!");
            return Either.left(error);
        }
        if (fileName == null || fileName.equals("")) {
            LOGGER.error("fileName can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file name can not be empty!");
            return Either.left(error);
        }
        File file = new File(getProjectPath(projectId));
        List<String> paths = FileUtil.getAllFilePath(file);
        if (paths == null || paths.isEmpty()) {
            LOGGER.error("can not find any file!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any file!");
            return Either.left(error);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = FileUtil.readFileContent(path);
            }
        }
        if (fileContent.equals("error")) {
            LOGGER.warn("file is not readable!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is not readable!");
            return Either.left(error);
        }
        return Either.right(fileContent);
    }

    private String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
            + File.separator;
    }

    /**
     * getFiles.
     */
    public AppPkgStructure getFiles(String filePath, AppPkgStructure appPkgStructure) throws IOException {
        File root = new File(filePath);
        File[] files = root.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<AppPkgStructure> fileList = new ArrayList<>();
        for (File file : files) {
            AppPkgStructure dto = new AppPkgStructure();
            if (file.isDirectory()) {
                String str = file.getName();
                dto.setId(str);
                dto.setName(str);
                fileList.add(dto);
                //Recursive call
                File[] fileArr = file.listFiles();
                if (fileArr != null && fileArr.length != 0) {
                    getFiles(file.getCanonicalPath(), dto);
                }
            } else {
                AppPkgStructure valueDto = new AppPkgStructure();
                valueDto.setId(file.getName());
                valueDto.setName(file.getName());
                valueDto.setParent(false);
                fileList.add(valueDto);
            }
        }
        appPkgStructure.setChildren(fileList);
        return appPkgStructure;
    }
}
