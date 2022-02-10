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

package org.edgegallery.developer.service.releasedpackage.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.ForbiddenException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.mapper.releasedpackage.ReleasedPackageMapper;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.appstore.PublishAppErrResponse;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgReqDto;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.signature.EncryptedService;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ReleasedPackageServiceImpl implements ReleasedPackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasedPackageServiceImpl.class);

    private static final String DOCS_ICON_PATH = "/Artifacts/Docs";

    @Autowired
    private ReleasedPackageMapper releasedPackageMapper;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private AppPackageService appPackageService;

    @Autowired
    private EncryptedService encryptedService;

    @Override
    public boolean synchronizePackage(User user, List<ReleasedPkgReqDto> pkgReqDtos) {
        // check user param
        if (user == null) {
            LOGGER.error("no user info was found!");
            throw new UnauthorizedException("no user info was found", ResponseConsts.RET_REQUEST_UNAUTHORIZED);
        }
        boolean permissionRes = !StringUtils.isEmpty(user.getUserAuth()) && user.getUserAuth()
            .contains(Consts.ROLE_DEVELOPER_ADMIN);
        //check user permissions
        if (!permissionRes) {
            LOGGER.error("admin permission are required!");
            throw new ForbiddenException("admin permission are required!", ResponseConsts.RET_REQUEST_FORBIDDEN);
        }

        // check pkgReqDtos param
        if (CollectionUtils.isEmpty(pkgReqDtos)) {
            LOGGER.error("no request body info was found!");
            throw new IllegalRequestException("no request body info was found", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        for (ReleasedPkgReqDto reqDto : pkgReqDtos) {
            // call app store get pkg interface && insert released db
            String queryPkgRes = AppStoreUtil.getPkgInfo(reqDto.getAppId(), reqDto.getPackageId(), user.getToken());
            if (queryPkgRes == null) {
                String msg = "call app store query pkg interface failed!";
                LOGGER.error(msg);
                throw new DeveloperException(msg, ResponseConsts.RET_SYNCHRONIZE_APP_PKG_FAIL);
            }
            saveReleasedPkg(user, queryPkgRes);
            // download pkg
            byte[] downloadPkgRes = AppStoreUtil.downloadPkg(reqDto.getAppId(), reqDto.getPackageId(), user.getToken());
            boolean res = saveDownloadResToFile(reqDto.getPackageId(), downloadPkgRes);
            if (!res) {
                String msg = "save download res to file failed!!";
                LOGGER.error(msg);
                throw new FileOperateException(msg, ResponseConsts.RET_SAVE_FILE_FAIL);
            }
            // insert app package db
            saveDownloadRes(reqDto.getPackageId());
        }
        return true;
    }

    private void saveReleasedPkg(User user, String queryPkgRes) {
        JsonObject jsonObject = new JsonParser().parse(queryPkgRes).getAsJsonObject();
        JsonObject dataObj = jsonObject.getAsJsonObject("data");
        String appId = dataObj.get("appId").getAsString();
        String packageId = dataObj.get("packageId").getAsString();

        ReleasedPackage queryReleasedPackage = releasedPackageMapper.getReleasedPackageById(appId, packageId);
        if (queryReleasedPackage != null) {
            releasedPackageMapper.deleteReleasedPackageById(appId, packageId);
        }

        ReleasedPackage releasedPackage = new ReleasedPackage(dataObj, user);
        int res = releasedPackageMapper.createReleasedPackage(releasedPackage);
        if (res <= 0) {
            String msg = "save released pkg info failed!";
            LOGGER.error(msg);
            throw new DataBaseException(msg, ResponseConsts.RET_CREATE_DATA_FAIL);
        }
    }

    private boolean saveDownloadResToFile(String packageId, byte[] downloadPkgRes) {
        try {
            if (downloadPkgRes.length == 0) {
                LOGGER.error("download pkg failed!");
                throw new DeveloperException("download pkg failed!", ResponseConsts.RET_SYNCHRONIZE_APP_PKG_FAIL);
            }

            String fileName = packageId + ".zip";
            String outPath = getPackagePath(packageId);
            LOGGER.info("output package path:{}", outPath);
            File pkgDir = new File(outPath);
            if (!pkgDir.exists()) {
                boolean isMk = pkgDir.mkdirs();
                if (!isMk) {
                    LOGGER.error("create pkg out path failed");
                    throw new FileOperateException("create pkg out path failed!", ResponseConsts.RET_CREATE_FILE_FAIL);
                }
            }

            File pkgFile = new File(outPath + fileName);
            if (!pkgFile.exists() && !pkgFile.createNewFile()) {
                LOGGER.error("create pkg file failed");
                throw new FileOperateException("create pkg file failed!", ResponseConsts.RET_CREATE_FILE_FAIL);
            }

            FileUtils.writeByteArrayToFile(pkgFile, downloadPkgRes);
        } catch (IOException e) {
            LOGGER.error("save file occur {}", e.getMessage());
            return false;
        }
        return true;
    }

    private void saveDownloadRes(String packageId) {
        AppPackage queriedAppPackage = appPackageService.getAppPackage(packageId);
        if (queriedAppPackage != null) {
            boolean ret = appPackageService.deletePackageRecord(packageId);
            if (!ret) {
                LOGGER.error("delete app pkg failed!");
                throw new DataBaseException("delete app pkg failed!", ResponseConsts.RET_DELETE_DATA_FAIL);
            }
        }
        AppPackage appPackage = new AppPackage();
        appPackage.setId(packageId);
        appPackage.setPackageFileName(packageId.concat(".zip"));
        appPackage.setPackageFilePath(
            BusinessConfigUtil.getReleasedPackagesPath() + packageId + File.separator + packageId + ".zip");
        boolean res = appPackageService.createPackage(appPackage);
        if (!res) {
            String msg = "save released pkg info to table app package failed!";
            LOGGER.error(msg);
            throw new DataBaseException(msg, ResponseConsts.RET_CREATE_DATA_FAIL);
        }
    }

    private String getPackagePath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getReleasedPackagesPath() + packageId
            + File.separator;
    }

    @Override
    public Page<ReleasedPackage> getAllPackages(String name, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<ReleasedPackage> pageInfo = new PageInfo<>(releasedPackageMapper.getAllReleasedPackages(name));
        LOGGER.info("Get all released pkg success.");
        return new Page<>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    @Override
    public List<AppPkgFile> getAppPkgStructure(String packageId) {
        return appPackageService.getAppPackageStructure(packageId);
    }

    @Override
    public ReleasedPkgFileContent getAppPkgFileContent(ReleasedPkgFileContentReqDto structureReqDto, String packageId) {
        return appPackageService.getAppPackageFileContent(structureReqDto, packageId);
    }

    @Override
    public ReleasedPkgFileContent editAppPkgFileContent(ReleasedPkgFileContent releasedPkgFileContent,
        String packageId) {
        return appPackageService.updateAppPackageFileContent(releasedPkgFileContent, packageId);
    }

    @Override
    public boolean deleteAppPkg(String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("delete pkg failed,packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (releasedPackage == null) {
            LOGGER.warn("packageId is error");
            return true;
        }

        int deletePkgRet = releasedPackageMapper.deleteReleasedPackageByPkgId(packageId);
        if (deletePkgRet <= 0) {
            LOGGER.error("delete released pkg data {} failed!", packageId);
            throw new DataBaseException("delete released pkg data failed", ResponseConsts.RET_DELETE_DATA_FAIL);
        }

        return appPackageService.deletePackage(packageId);
    }

    @Override
    public boolean releaseAppPkg(User user, PublishAppReqDto publishAppReqDto, String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("release pkg failed,packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        // query appPackage and releasedPackage
        AppPackage appPackage = appPackageService.getAppPackage(packageId);
        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (appPackage == null || releasedPackage == null) {
            LOGGER.warn("packageId is error");
            throw new DataBaseException("can not found app or released Package", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        String appPKgPath = getAppPkgPath(packageId) + packageId + ".csar";
        File appPkg = new File(appPKgPath);
        if (!appPkg.exists()) {
            LOGGER.warn("The synchronized pkg has not been packaged(.csar) yet");
            throw new FileFoundFailException("can not found app package(.csar)", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        List<File> list = getIconList(packageId);
        if (CollectionUtils.isEmpty(list)) {
            LOGGER.warn("can not found icon under art/docs dir");
            throw new FileFoundFailException("can not found icon under docs dir", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("file", new FileSystemResource(appPkg));
        map.put("icon", new FileSystemResource(list.get(0)));
        map.put("type", releasedPackage.getType());
        map.put("shortDesc", releasedPackage.getShortDesc());
        map.put("affinity", releasedPackage.getArchitecture());
        map.put("industry", releasedPackage.getIndustry());
        map.put("testTaskId", releasedPackage.getTestTaskId());
        PublishAppErrResponse errResponse = new PublishAppErrResponse();
        String uploadResult = AppStoreUtil.storeToAppStore(map, user, errResponse);
        LOGGER.info("uploadResult:{}", uploadResult);
        checkResultLength(uploadResult, "upload app to appstore fail!", errResponse);

        LOGGER.info("upload appstore result:{}", uploadResult);
        JsonObject jsonObject = new JsonParser().parse(uploadResult).getAsJsonObject();
        JsonElement appStoreAppId = jsonObject.get("appId");
        JsonElement appStorePackageId = jsonObject.get("packageId");

        checkInnerParamNull(appStoreAppId, "response from upload to appstore does not contain appId");
        checkInnerParamNull(appStorePackageId, "response from upload to appstore does not contain packageId");

        String publishRes = AppStoreUtil
            .publishToAppStore(appStoreAppId.getAsString(), appStorePackageId.getAsString(), user.getToken(),
                publishAppReqDto, errResponse);
        LOGGER.info("publishRes:{}", publishRes);
        checkResultLength(publishRes, "publish app to appstore fail!", errResponse);
        return true;
    }

    @Override
    public ReleasedPackage getReleasedPackageByPkgId(String packageId) {
        return releasedPackageMapper.getReleasedPackageByPkgId(packageId);
    }

    private String getAppPkgDecompressPath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getReleasedPackagesPath() + packageId
            + File.separator + packageId + File.separator;
    }

    private String getAppPkgPath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getReleasedPackagesPath() + packageId
            + File.separator;
    }

    private List<File> getIconList(String packageId) {
        String iconPath = getAppPkgDecompressPath(packageId) + DOCS_ICON_PATH;
        File icon = new File(iconPath);
        return Arrays.stream(icon.listFiles())
            .filter(item -> item.getName().endsWith("jpg") || item.getName().endsWith("png"))
            .collect(Collectors.toList());
    }

    private <T> void checkInnerParamNull(T innerParam, String msg) {
        if (null == innerParam) {
            LOGGER.error(msg);
            throw new DeveloperException(msg, ResponseConsts.RET_PUBLISH_APP_PKG_FAIL);
        }
    }

    private void checkResultLength(String innerParam, String msg, PublishAppErrResponse errResponse) {
        if (StringUtils.isEmpty(innerParam) || errResponse.getErrCode() != 0) {
            LOGGER.error(msg);
            throw new DeveloperException(msg, errResponse.getErrCode());
        }
    }
}

