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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.model.AppPkgStructure;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("appReleaseService")
public class AppReleaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppReleaseService.class);

    private List<String> listLocal = new ArrayList<>();

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
        List<String> paths = getFilesPath(file);
        if (paths == null || paths.isEmpty()) {
            LOGGER.error("can not find any file!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any file!");
            return Either.left(error);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = readFileIntoString(path);
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

    /**
     * getFilesPath.
     *
     * @param dir file dir
     * @return
     */
    public List<String> getFilesPath(File dir) {
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFilesPath(file);
                }
                if (file.isFile()) {
                    try {
                        listLocal.add(file.getCanonicalPath());
                    } catch (IOException e) {
                        LOGGER.error("get unzip dir occur exception {}", e.getMessage());
                        return new ArrayList<>();
                    }
                }
            }
        }
        return listLocal;
    }

    /**
     * readFileIntoString.
     *
     * @param filePath filepath
     * @return
     */
    public String readFileIntoString(String filePath) {
        String msg = "error";
        StringBuffer sb = new StringBuffer();
        String encoding = "UTF-8";
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                 BufferedReader bufferedReader = new BufferedReader(read)) {
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    sb.append(lineTxt + "\r\n");
                }
            } catch (IOException e) {
                LOGGER.error("read file occur exception {}", e.getMessage());
                return msg;
            }
        } else {
            LOGGER.error("There are no files in this directory!");
            return msg;
        }
        return sb.toString();
    }
}
