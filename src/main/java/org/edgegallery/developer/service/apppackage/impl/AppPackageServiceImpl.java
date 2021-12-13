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
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.application.AppScriptMapper;
import org.edgegallery.developer.mapper.apppackage.AppPackageMapper;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.creater.ContainerPackageFileCreator;
import org.edgegallery.developer.service.apppackage.csar.creater.PackageFileCreator;
import org.edgegallery.developer.service.apppackage.csar.creater.VMPackageFileCreator;
import org.edgegallery.developer.service.apppackage.csar.signature.EncryptedService;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.releasedpackage.ReleasedPackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppPackageServiceImpl implements AppPackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPackageServiceImpl.class);

    private static final String APPD_ZIP_PATH = "/APPD/";

    private static final String CHARTS_TGZ_PATH = "/Artifacts/Deployment/Charts/";

    private static final String TEMPLATE_PATH = "temp";

    @Autowired
    private AppPackageMapper appPackageMapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AppScriptMapper appScriptMapper;

    @Autowired
    private ReleasedPackageService releasedPackageService;

    @Autowired
    private EncryptedService encryptedService;

    @Override
    public AppPackage getAppPackage(String packageId) {
        return appPackageMapper.getAppPackage(packageId);
    }

    @Override
    public AppPackage getAppPackageByAppId(String applicationId) {
        return appPackageMapper.getAppPackageByAppId(applicationId);
    }

    @Override
    public List<AppPkgFile> getAppPackageStructure(String packageId) {
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is empty.");
            throw new IllegalRequestException("packageId is empty!", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("packageId is error!");
            throw new DataBaseException("packageId is error", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String pkgDir = InitConfigUtil.getWorkSpaceBaseDir() + appPackage.getPackageFilePath();
        File pkgFile = new File(pkgDir);
        if (!pkgFile.exists()) {
            LOGGER.error("pkg {} not found", pkgFile.getName());
            throw new FileFoundFailException("pkg(.zip) not found!", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        //decompress zip
        String zipDecompressDir = ReleasedPackageUtil.decompressAppPkg(appPackage, pkgDir, packageId);
        LOGGER.info("zipDecompressDir:{}", zipDecompressDir);

        //get zip catalog
        return ReleasedPackageUtil.getCatalogue(zipDecompressDir);
    }

    @Override
    public ReleasedPkgFileContent getAppPackageFileContent(ReleasedPkgFileContentReqDto structureReqDto,
        String packageId) {
        // check packageId
        if (org.springframework.util.StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //check param
        if (structureReqDto == null) {
            LOGGER.error("structureReqDto(body param) is null");
            throw new IllegalRequestException("structureReqDto is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //query appPackage
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("packageId is error");
            throw new DataBaseException("packageId is error", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        String appId = appPackage.getAppId();
        String zipDecompressDir = "";
        if (StringUtils.isEmpty(appId)) {
            zipDecompressDir = ReleasedPackageUtil.getReleasedPkgDecompressPath(packageId);
        } else {
            zipDecompressDir = ReleasedPackageUtil.getAppPkgDecompressPath(packageId);
        }
        //get decompress dir and get file content
        File decompressDir = new File(zipDecompressDir);
        if (!decompressDir.exists() || !decompressDir.isDirectory()) {
            LOGGER.error("app pkg {} not decompress!", zipDecompressDir);
            throw new FileFoundFailException("app pkg not decompress", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        String content = ReleasedPackageUtil.getContentByInnerPath(structureReqDto.getFilePath(), zipDecompressDir);
        LOGGER.info("file content:{}", content);
        ReleasedPkgFileContent pkgFileContent = new ReleasedPkgFileContent();
        pkgFileContent.setFilePath(structureReqDto.getFilePath());
        pkgFileContent.setContent(content);

        return pkgFileContent;
    }

    @Override
    public ReleasedPkgFileContent updateAppPackageFileContent(ReleasedPkgFileContent releasedPkgFileContent,
        String packageId) {
        // check packageId
        if (org.springframework.util.StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("packageId is error");
            throw new DataBaseException("packageId is error", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        //check param
        if (releasedPkgFileContent == null) {
            LOGGER.error("releasedPkgFileContent(body param) is null");
            throw new IllegalRequestException("releasedPkgFileContent is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //get decompress dir
        String appId = appPackage.getAppId();
        String zipDecompressDir = "";
        if (StringUtils.isEmpty(appId)) {
            zipDecompressDir = ReleasedPackageUtil.getReleasedPkgDecompressPath(packageId);
        } else {
            zipDecompressDir = ReleasedPackageUtil.getAppPkgDecompressPath(packageId);
        }
        File decompressDir = new File(zipDecompressDir);
        if (!decompressDir.exists() || !decompressDir.isDirectory()) {
            LOGGER.error("app pkg decompress dir {} was not found!", zipDecompressDir);
            throw new FileFoundFailException("app pkg decompress dir was not found", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        //modify content
        boolean ret = ReleasedPackageUtil
            .modifyFileByPath(releasedPkgFileContent.getFilePath(), releasedPkgFileContent.getContent(),
                zipDecompressDir);
        if (!ret) {
            LOGGER.error("modify file  {} content failed!", releasedPkgFileContent.getFilePath());
            throw new FileOperateException("modify file content failed!", ResponseConsts.RET_WRITE_FILE_FAIL);
        }
        if (StringUtils.isEmpty(appId)) {
            ReleasedPackage releasedPackage = releasedPackageService.getReleasedPackageByPkgId(packageId);
            String compressZipName = getAppFileName(releasedPackage, "");
            //compress to csar
            if (!compressToCsar(packageId, compressZipName)) {
                LOGGER.error("compress csar file failed!");
                throw new FileOperateException("compress csar file failed", ResponseConsts.RET_WRITE_FILE_FAIL);
            }
        }

        //set ReleasedPkgFileContent
        ReleasedPkgFileContent queryFile = new ReleasedPkgFileContent();
        queryFile.setFilePath(releasedPkgFileContent.getFilePath());
        queryFile.setContent(
            ReleasedPackageUtil.getContentByInnerPath(releasedPkgFileContent.getFilePath(), zipDecompressDir));
        return queryFile;
    }

    private boolean compressToCsar(String packageId, String compressZipName) {
        String decompressDir = ReleasedPackageUtil.getReleasedPkgDecompressPath(packageId);
        String pkgDir = ReleasedPackageUtil.getAppPkgPath(packageId);
        File pkgDeCompressDir = new File(decompressDir);

        if (!pkgDeCompressDir.exists()) {
            LOGGER.error("can not found decompress path {}!", decompressDir);
            throw new FileFoundFailException("can not found pkg decompress path!", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        String tempPackageName = TEMPLATE_PATH + "-" + packageId;
        String tempPackagePath = pkgDir + File.separator + tempPackageName;
        try {
            DeveloperFileUtils.copyDirectory(pkgDeCompressDir, new File(pkgDir), tempPackageName);

            // compress appd
            String appdDir = tempPackagePath + APPD_ZIP_PATH;
            CompressFileUtils.fileToZip(appdDir, compressZipName);

            // compress tgz
            String tgzDir = tempPackagePath + CHARTS_TGZ_PATH;
            File chartsDir = new File(tgzDir);
            File[] charts = chartsDir.listFiles();
            if (charts == null || charts.length == 0) {
                LOGGER.error("can not found any tgz file under path  {}!", tgzDir);
                throw new FileFoundFailException("can not found any tgz file!", ResponseConsts.RET_FILE_NOT_FOUND);
            }
            CompressFileUtils.compressToTgzAndDeleteSrc(charts[0].getCanonicalPath(), tgzDir, charts[0].getName());

            //sign package
            boolean encryptedResult = encryptedService.encryptedCMS(tempPackagePath);
            if (!encryptedResult) {
                LOGGER.error("sign package failed");
                return false;
            }

            // compress csar
            CompressFileUtilsJava.compressToCsarAndDeleteSrc(tempPackagePath, pkgDir, packageId);
        } catch (IOException e) {
            LOGGER.error("package compress fail, package path:{}", tempPackagePath);
            return false;
        }
        return true;
    }

    private String getAppFileName(ReleasedPackage releasedPackage, String format) {
        return releasedPackage.getName() + "_" + releasedPackage.getProvider() + "_" + releasedPackage.getVersion()
            + "_" + releasedPackage.getArchitecture() + format;
    }

    @Override
    public AppPackage generateAppPackage(VMApplication application) {
        AppPackage appPackage = appPackageMapper.getAppPackageByAppId(application.getId());
        if (null == appPackage) {
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
        appPackage.setPackageFilePath(
            BusinessConfigUtil.getWorkspacePath().concat(application.getId()).concat(File.separator).concat(fileName));
        appPackageMapper.modifyAppPackage(appPackage);
        return appPackage;
    }

    @Override
    public AppPackage generateAppPackage(ContainerApplication application) {
        AppPackage appPackage = appPackageMapper.getAppPackageByAppId(application.getId());
        if (null == appPackage) {
            appPackage = new AppPackage();
            appPackage.setId(UUID.randomUUID().toString());
            appPackage.setAppId(application.getId());
            appPackageMapper.createAppPackage(appPackage);
        }

        ContainerPackageFileCreator containerPackageFileCreator = new ContainerPackageFileCreator(application,
            appPackage.getId());
        String fileName = containerPackageFileCreator.generateAppPackageFile();

        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("Generation app package error.");
            deletePackage(appPackage);
            throw new FileOperateException("Generation app package error.", ResponseConsts.RET_CREATE_FILE_FAIL);
        }
        appPackage.setPackageFileName(fileName);
        appPackage.setPackageFilePath(
            BusinessConfigUtil.getWorkspacePath().concat(application.getId()).concat(File.separator).concat(fileName));
        appPackageMapper.modifyAppPackage(appPackage);
        applicationService.updateApplicationStatus(application.getId(), EnumApplicationStatus.PACKAGED);
        return appPackage;
    }

    @Override
    public AppPackage zipPackage(String packageId) {
        AppPackage appPackage = getAppPackage(packageId);
        if (null == appPackage) {
            LOGGER.error("package does not exist, packageId:{}.", packageId);
            throw new FileFoundFailException("package does not exist!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        ApplicationDetail applicationDetail = applicationService.getApplicationDetail(appPackage.getAppId());
        if (applicationDetail.getVmApp() != null && applicationDetail.getVmApp().getAppClass()
            .equals(EnumAppClass.VM)) {
            PackageFileCreator packageFileCreator = new PackageFileCreator(applicationDetail.getVmApp(),
                appPackage.getId());
            String fileName = packageFileCreator.PackageFileCompress();
            if (StringUtils.isEmpty(fileName)) {
                LOGGER.error("zip package error.");
                throw new FileOperateException("zip package error.", ResponseConsts.RET_CREATE_FILE_FAIL);
            }
        }
        if (applicationDetail.getContainerApp() != null && applicationDetail.getContainerApp().getAppClass()
            .equals(EnumAppClass.CONTAINER)) {
            ContainerPackageFileCreator packageFileCreator = new ContainerPackageFileCreator(
                applicationDetail.getContainerApp(), appPackage.getId());
            String fileName = packageFileCreator.PackageFileCompress();
            if (StringUtils.isEmpty(fileName)) {
                LOGGER.error("zip package error.");
                throw new FileOperateException("zip package error.", ResponseConsts.RET_CREATE_FILE_FAIL);
            }
        }
        return appPackage;
    }

    @Override
    public boolean createPackage(AppPackage appPackage) {
        int res = appPackageMapper.createAppPackage(appPackage);
        if (res <= 0) {
            LOGGER.error("create app package failed.");
            throw new DataBaseException("create app package failed.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public boolean deletePackage(String packageId) {
        AppPackage appPackage = appPackageMapper.getAppPackage(packageId);
        if (appPackage == null) {
            LOGGER.error("package does not exist");
            return true;
        }
        return deletePackage(appPackage);

    }

    private boolean deletePackage(AppPackage appPackage) {
        // delete package file
        String appPkgDir = appPackage.getPackageFilePath();
        if (StringUtils.isNotEmpty(appPkgDir)) {
            File pkgFile = new File(appPkgDir);
            if (!pkgFile.exists()) {
                LOGGER.warn("package file {} does not exist", pkgFile.getName());
            } else {
                DeveloperFileUtils.deleteDir(pkgFile.getParent());
            }
        }
        appPackageMapper.deleteAppPackage(appPackage.getId());
        return true;
    }
}
