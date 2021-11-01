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

package org.edgegallery.developer.service.apppackage.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.apppackage.AppPackageMapper;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.VMPackageFileCreator;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.ApplicationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppPackageServiceImpl implements AppPackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPackageServiceImpl.class);

    @Autowired
    private AppPackageMapper appPackageMapper;

    @Override
    public AppPackage getAppPackage(String packageId) {
        return appPackageMapper.getAppPackage(packageId);
    }

    @Override
    public AppPkgStructure getAppPackageStructure(String packageId) {
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is empty.");
            throw new IllegalRequestException("packageId is empty!", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("query object(AppPackage) is null.");
            throw new DataBaseException("query object(AppPackage) is null!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String projectPath = getProjectPath(appPackage.getAppId());
        if (StringUtils.isEmpty(projectPath)) {
            LOGGER.error("project path not exist.");
            throw new FileFoundFailException("project path not exist!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        // get csar pkg structure
        AppPkgStructure structure;
        String fileName = appPackage.getPackageFileName();
        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("fileName of app pkg is empty.");
            throw new DataBaseException("fileName of app pkg is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        try {
            String pkgFolderName = fileName.substring(0, fileName.lastIndexOf("."));
            LOGGER.warn("pkgFolderName:{}", pkgFolderName);
            structure = getFiles(projectPath + pkgFolderName + File.separator, new AppPkgStructure());
        } catch (IOException e) {
            LOGGER.error("get app pkg occur {}", e.getMessage());
            return null;
        }
        return structure;
    }

    @Override
    public String getAppPackageFileContent(String packageId, String fileName) {
        if (StringUtils.isEmpty(packageId) || StringUtils.isEmpty(fileName)) {
            LOGGER.error("packageId or fileName is empty.");
            throw new IllegalRequestException("packageId or fileName is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("query object(AppPackage) is null.");
            throw new DataBaseException("query object(AppPackage) is null!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String projectPath = getProjectPath(appPackage.getAppId());
        if (StringUtils.isEmpty(projectPath)) {
            LOGGER.error("project path not exist.");
            throw new FileFoundFailException("project path not exist!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        String pkgName = appPackage.getPackageFileName();
        if (StringUtils.isEmpty(pkgName)) {
            LOGGER.error("fileName of app pkg is empty.");
            throw new DataBaseException("fileName of app pkg is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String pkgFolderName = pkgName.substring(0, pkgName.lastIndexOf("."));
        File file = new File(projectPath + pkgFolderName + File.separator);
        List<String> paths = FileUtil.getAllFilePath(file);
        if (paths.isEmpty()) {
            String errMsg = "can not find any file in app pkg folder!";
            throw new FileFoundFailException(errMsg, ResponseConsts.RET_FILE_NOT_FOUND);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = FileUtil.readFileContent(path);
            }
        }
        if (fileContent.equals("error")) {
            LOGGER.error("file is not readable!");
            throw new FileOperateException("file is not readable", ResponseConsts.RET_FILE_NOT_READABLE);
        }
        if (fileContent.equals("")) {
            LOGGER.error("can not find file {}!", fileName);
            throw new FileFoundFailException("the file you find cannot be found", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        return fileContent;
    }

    @Override
    public boolean updateAppPackageFileContent(String packageId, String fileName, String content) {
        if (StringUtils.isEmpty(packageId) || StringUtils.isEmpty(fileName) || StringUtils.isEmpty(content)) {
            String message = "packageId or fileName or content is empty";
            LOGGER.error(message);
            throw new IllegalRequestException(message, ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("query object(AppPackage) is null.");
            throw new DataBaseException("query object(AppPackage) is null!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String pkgName = appPackage.getPackageFileName();
        if (StringUtils.isEmpty(pkgName)) {
            LOGGER.error("fileName of app pkg is empty.");
            throw new DataBaseException("fileName of app pkg is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String projectPath = getProjectPath(appPackage.getAppId());
        String pkgFolderName = pkgName.substring(0, pkgName.lastIndexOf("."));
        File file = new File(projectPath + pkgFolderName + File.separator + fileName);
        if(!file.exists()){
            LOGGER.error("can not find file {}!", fileName);
            throw new FileFoundFailException("the file you update cannot be found!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        try {
            FileUtils.writeStringToFile(file,content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("write content to file occur {}!", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public AppPackage generateAppPackage(VMApplication application) {
        AppPackage appPackage = appPackageMapper.getAppPackageByAppId(application.getId());
        if(null == appPackage){
            appPackage = new AppPackage();
            appPackage.setId(UUID.randomUUID().toString());
            appPackage.setAppId(application.getId());
            appPackageMapper.createAppPackage(appPackage);
        }

        // generation appd
        VMPackageFileCreator vmPackageFileCreator = new VMPackageFileCreator(application, appPackage.getId());
        String fileName = vmPackageFileCreator.generateAppPackageFile();
        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("Generation app package error.");
            deletePackage(appPackage);
            throw new FileOperateException("Generation app package error.", ResponseConsts.RET_CREATE_FILE_FAIL);
        }
        appPackage.setPackageFileName(fileName);
        appPackageMapper.modifyAppPackage(appPackage);
        return appPackage;
    }

    @Override
    public AppPackage generateAppPackage(ContainerApplication application) {
        return null;
    }

    @Override
    public boolean deletePackage(String packageId) {
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        return deletePackage(appPackage);

    }

    public boolean deletePackage(AppPackage appPackage) {
        // delete package file
        String packagePath = ApplicationUtil.getApplicationBasePath(appPackage.getAppId());
        DeveloperFileUtils.deleteDir(packagePath);
        appPackageMapper.deleteAppPackage(appPackage.getId());
        return true;
    }

    private String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
            + File.separator;
    }

    private AppPkgStructure getFiles(String filePath, AppPkgStructure appPkgStructure) throws IOException {
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