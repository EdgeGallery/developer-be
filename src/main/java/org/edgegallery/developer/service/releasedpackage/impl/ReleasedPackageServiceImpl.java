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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.RestfulRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.mapper.releasedpackage.ReleasedPackageMapper;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgReqDto;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.apppackage.signature.EncryptedService;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.releasedpackage.ReleasedPackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ReleasedPackageServiceImpl implements ReleasedPackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasedPackageServiceImpl.class);

    private static final String APPD_ZIP_PATH = "/APPD/";

    private static final String CHARTS_TGZ_PATH = "/Artifacts/Deployment/Charts/";

    private static final String DOCS_ICON_PATH = "/Artifacts/Docs";

    private static final String TEMPLATE_PATH = "temp";

    @Autowired
    private ReleasedPackageMapper releasedPackageMapper;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private EncryptedService encryptedService;

    @Override
    public boolean synchronizePackage(User user, List<ReleasedPkgReqDto> pkgReqDtos) {
        // check user param
        if (user == null) {
            LOGGER.error("no user info was found!");
            throw new UnauthorizedException("no user info was found", ResponseConsts.RET_REQUEST_UNAUTHORIZED);
        }

        // check pkgReqDtos param
        if (CollectionUtils.isEmpty(pkgReqDtos)) {
            LOGGER.error("no request body info was found!");
            throw new IllegalRequestException("no request body info was found",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        for (ReleasedPkgReqDto reqDto : pkgReqDtos) {
            // call app store get pkg interface && insert released db
            ResponseEntity<String> queryPkgRes = AppStoreUtil
                .getPkgInfo(reqDto.getAppId(), reqDto.getPackageId(), user.getToken());
            if (queryPkgRes == null) {
                String msg = "call app store query pkg interface failed!";
                LOGGER.error(msg);
                throw new RestfulRequestException(msg, ResponseConsts.RET_RESTFUL_REQUEST_FAIL);
            }
            saveReleasedPkg(user, queryPkgRes);
            // download pkg
            ResponseEntity<byte[]> downloadPkgRes = AppStoreUtil
                .downloadPkg(reqDto.getAppId(), reqDto.getPackageId(), user.getToken());
            boolean res = saveDownloadResToFile(reqDto.getPackageId(), downloadPkgRes);
            if (!res) {
                String msg = "save download res to file failed!!";
                LOGGER.error(msg);
                throw new FileOperateException(msg, ResponseConsts.RET_SAVE_FILE_FAIL);
            }
            // insert upload-file db
            saveDownloadRes(user, reqDto.getPackageId());
        }
        return true;
    }

    private void saveReleasedPkg(User user, ResponseEntity<String> queryPkgRes) {
        JsonObject jsonObject = new JsonParser().parse(queryPkgRes.getBody()).getAsJsonObject();
        JsonObject dataObj = jsonObject.getAsJsonObject("data");
        String appId = dataObj.get("appId").getAsString();
        String packageId = dataObj.get("packageId").getAsString();

        ReleasedPackage queryReleasedPackage = releasedPackageMapper.getReleasedPackageById(appId, packageId);
        if (queryReleasedPackage != null) {
            releasedPackageMapper.deleteReleasedPackageById(appId, packageId);
        }

        ReleasedPackage releasedPackage = new ReleasedPackage();
        releasedPackage.setAppId(dataObj.get("appId").getAsString());
        releasedPackage.setPackageId(dataObj.get("packageId").getAsString());
        releasedPackage.setName(dataObj.get("name").getAsString());
        releasedPackage.setVersion(dataObj.get("version").getAsString());
        releasedPackage.setProvider(dataObj.get("provider").getAsString());
        releasedPackage.setIndustry(dataObj.get("industry").getAsString());
        releasedPackage.setType(dataObj.get("type").getAsString());
        releasedPackage.setArchitecture(dataObj.get("affinity").getAsString());
        releasedPackage.setShortDesc(dataObj.get("shortDesc").getAsString());
        releasedPackage.setSynchronizeDate(new Date());
        releasedPackage.setUserId(user.getUserId());
        releasedPackage.setUserName(user.getUserName());
        releasedPackage.setTestTaskId(dataObj.get("testTaskId").getAsString());

        int res = releasedPackageMapper.createReleasedPackage(releasedPackage);
        if (res <= 0) {
            String msg = "save released pkg info failed!";
            LOGGER.error(msg);
            throw new DataBaseException(msg, ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    private boolean saveDownloadResToFile(String packageId, ResponseEntity<byte[]> downloadPkgRes) {
        try {
            byte[] result = downloadPkgRes.getBody();
            if (result == null) {
                LOGGER.error("download pkg failed!");
                throw new RestfulRequestException("download pkg failed!", ResponseConsts.RET_RESTFUL_REQUEST_FAIL);
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

            FileUtils.writeByteArrayToFile(pkgFile, result);
        } catch (IOException e) {
            LOGGER.error("save file occur {}", e.getMessage());
            return false;
        }
        return true;
    }

    private void saveDownloadRes(User user, String packageId) {
        UploadFile queryUploadFile = uploadFileService.getFile(packageId);
        if (queryUploadFile != null) {
            boolean res = uploadFileService.deleteFileRecord(packageId);
            LOGGER.info("del res:{}", res);
        }

        UploadFile uploadFile = new UploadFile();
        uploadFile.setFileId(packageId);
        uploadFile.setFileName(packageId + ".zip");
        uploadFile
            .setFilePath(BusinessConfigUtil.getUploadfilesPath() + packageId + File.separator + packageId + ".zip");
        uploadFile.setTemp(false);
        uploadFile.setUploadDate(new Date());
        uploadFile.setUserId(user.getUserId());

        int res = uploadFileService.saveFile(uploadFile);
        if (res <= 0) {
            String msg = "save released pkg info to table upload file failed!";
            LOGGER.error(msg);
            throw new DataBaseException(msg, ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    private String getPackagePath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + packageId
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
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        // query uploadFile and releasedPackage
        UploadFile uploadFile = uploadFileService.getFile(packageId);
        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (uploadFile == null || releasedPackage == null) {
            LOGGER.error("packageId is error");
            throw new DataBaseException("packageId is error", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String pkgDir = InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath();
        File pkgFile = new File(pkgDir);
        if (!pkgFile.exists()) {
            LOGGER.error("pkg {} not found", pkgFile.getName());
            throw new FileFoundFailException("pkg(.zip) not found!", ResponseConsts.RET_FILE_NOT_FOUND);
        }

        //decompress zip
        String zipDecompressDir = decompressAppPkg(uploadFile, releasedPackage, pkgDir, packageId);
        LOGGER.info("zipDecompressDir:{}", zipDecompressDir);

        //get zip catalog
        return ReleasedPackageUtil.getCatalogue(zipDecompressDir);
    }

    private String decompressAppPkg(UploadFile uploadFile, ReleasedPackage releasedPackage, String pkgDir,
        String packageId) {

        //decompress zip
        String zipPath = uploadFile.getFilePath();
        String zipParentDir = zipPath.substring(0, zipPath.lastIndexOf(File.separator));
        String zipDecompressDir = InitConfigUtil.getWorkSpaceBaseDir() + zipParentDir + File.separator + "decompress-"
            + packageId;
        boolean ret = ReleasedPackageUtil.decompressAppPkg(pkgDir, zipDecompressDir);
        if (!ret) {
            LOGGER.error("decompress zip file {} failed!", uploadFile.getFileName());
            throw new FileOperateException("decompress pkg(.zip) failed!", ResponseConsts.RET_DECOMPRESS_FILE_FAIL);
        }

        // decompress zip under \APPD
        String appdFileName = releasedPackage.getName() + "_" + releasedPackage.getProvider() + "_" + releasedPackage
            .getVersion() + "_" + releasedPackage.getArchitecture() + ".zip";
        String appdZipDir = zipDecompressDir + APPD_ZIP_PATH + appdFileName;
        String appdZipParentDir = zipDecompressDir + APPD_ZIP_PATH;
        File appdZipFile = new File(appdZipDir);
        if (!appdZipFile.exists()) {
            LOGGER.error("pkg {} not found", appdFileName);
            throw new FileFoundFailException("pkg(.zip) not found!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        boolean appdRet = ReleasedPackageUtil.decompressAppPkg(appdZipDir, appdZipParentDir);
        if (!appdRet) {
            LOGGER.error("decompress zip file {} failed!", uploadFile.getFileName());
            throw new FileOperateException("decompress pkg(.zip) failed!", ResponseConsts.RET_DECOMPRESS_FILE_FAIL);
        }


        // decompress tgz under \Artifacts\Deployment\Charts
        String chartsTgzParentDir = zipDecompressDir + CHARTS_TGZ_PATH;
        List<File> fileList = ReleasedPackageUtil.getFiles(chartsTgzParentDir);
        if (CollectionUtils.isEmpty(fileList)) {
            LOGGER.error("no tgz file found under path {}", chartsTgzParentDir);
            throw new FileFoundFailException("pkg(.tgz) not found!", ResponseConsts.RET_FILE_NOT_FOUND);
        }


        try {
            for (File tgzFile : fileList) {
                boolean tgzRet = ReleasedPackageUtil.decompressAppPkg(tgzFile.getCanonicalPath(), chartsTgzParentDir);
                if (!tgzRet) {
                    LOGGER.error("decompress tgz file {} failed!", tgzFile.getName());
                    throw new FileOperateException("decompress pkg(.tgz) failed!",
                        ResponseConsts.RET_DECOMPRESS_FILE_FAIL);
                }
                FileUtils.forceDelete(tgzFile);
            }
            FileUtils.forceDelete(appdZipFile);
        } catch (IOException e) {
            LOGGER.error("delete zip file {} failed!", appdZipFile.getName());
            throw new FileOperateException("delete pkg(.zip) failed!", ResponseConsts.RET_DELETE_FILE_FAIL);
        }

        return zipDecompressDir;
    }

    @Override
    public ReleasedPkgFileContent getAppPkgFileContent(ReleasedPkgFileContentReqDto structureReqDto, String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //check param
        if (structureReqDto == null) {
            LOGGER.error("structureReqDto(body param) is null");
            throw new IllegalRequestException("structureReqDto is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //get decompress dir and get file content
        String zipDecompressDir = getAppPkgDecompressPath(packageId);
        File decompressDir = new File(zipDecompressDir);
        if (!decompressDir.exists() || !decompressDir.isDirectory()) {
            LOGGER.error("app pkg not decompress!");
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
    public ReleasedPkgFileContent editAppPkgFileContent(ReleasedPkgFileContent releasedPkgFileContent,
        String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (releasedPackage == null) {
            LOGGER.error("packageId is error");
            throw new DataBaseException("packageId is error", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        //check param
        if (releasedPkgFileContent == null) {
            LOGGER.error("releasedPkgFileContent(body param) is null");
            throw new IllegalRequestException("releasedPkgFileContent is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        //get decompress dir
        String zipDecompressDir = getAppPkgDecompressPath(packageId);
        File decompressDir = new File(zipDecompressDir);
        if (!decompressDir.exists() || !decompressDir.isDirectory()) {
            LOGGER.error("app pkg decompress dir was not found!");
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

        //compress to csar
        if (!compressToCsar(packageId, releasedPackage)) {
            LOGGER.error("compress csar file failed!");
            throw new FileOperateException("compress csar file failed", ResponseConsts.RET_WRITE_FILE_FAIL);
        }

        //set ReleasedPkgFileContent
        ReleasedPkgFileContent queryFile = new ReleasedPkgFileContent();
        queryFile.setFilePath(releasedPkgFileContent.getFilePath());
        queryFile.setContent(
            ReleasedPackageUtil.getContentByInnerPath(releasedPkgFileContent.getFilePath(), zipDecompressDir));
        return queryFile;
    }

    private boolean compressToCsar(String packageId, ReleasedPackage releasedPackage) {
        String decompressDir = getAppPkgDecompressPath(packageId);
        String pkgDir = getAppPkgPath(packageId);
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
            CompressFileUtils.fileToZip(appdDir, getAppFileName(releasedPackage, ""));

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
    public boolean deleteAppPkg(String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        // query uploadFile and releasedPackage
        UploadFile uploadFile = uploadFileService.getFile(packageId);
        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (uploadFile == null || releasedPackage == null) {
            LOGGER.warn("packageId is error");
            return true;
        }

        boolean ret = uploadFileService.deleteFile(packageId);
        if (!ret) {
            LOGGER.error("delete upload file data {} failed!", packageId);
            throw new DataBaseException("delete upload file data failed", ResponseConsts.RET_DELETE_DATA_FAIL);
        }

        int deletePkgRet = releasedPackageMapper.deleteReleasedPackageByPkgId(packageId);
        if (deletePkgRet <= 0) {
            LOGGER.error("delete released pkg data {} failed!", packageId);
            throw new DataBaseException("delete released pkg data failed", ResponseConsts.RET_DELETE_DATA_FAIL);
        }

        //delete file
        String pkgDirPath = getAppPkgPath(packageId);
        File pkgDir = new File(pkgDirPath);
        if (!pkgDir.exists()) {
            LOGGER.warn("app pkg dir {} not exist", pkgDirPath);
            return true;
        }

        try {
            FileUtils.cleanDirectory(pkgDir);
        } catch (IOException e) {
            LOGGER.error("clean app pkg dir {} failed! {}", pkgDirPath, e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean releaseAppPkg(User user, PublishAppReqDto publishAppReqDto, String packageId) {
        // check packageId
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.error("packageId is null");
            throw new IllegalRequestException("packageId is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        // query uploadFile and releasedPackage
        UploadFile uploadFile = uploadFileService.getFile(packageId);
        ReleasedPackage releasedPackage = releasedPackageMapper.getReleasedPackageByPkgId(packageId);
        if (uploadFile == null || releasedPackage == null) {
            LOGGER.warn("packageId is error");
            return false;
        }

        String appPKgPath = getAppPkgPath(packageId) + packageId + ".csar";
        File appPkg = new File(appPKgPath);
        if (!appPkg.exists()) {
            LOGGER.warn("The synchronized pkg has not been packaged(.csar) yet");
            return false;
        }

        List<File> list = getIconList(packageId);
        if (CollectionUtils.isEmpty(list)) {
            LOGGER.warn("can not found icon under art/docs dir");
            return false;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("file", new FileSystemResource(appPkg));
        map.put("icon", new FileSystemResource(list.get(0)));
        map.put("type", releasedPackage.getType());
        map.put("shortDesc", releasedPackage.getShortDesc());
        map.put("affinity", releasedPackage.getArchitecture());
        map.put("industry", releasedPackage.getIndustry());
        map.put("testTaskId", releasedPackage.getTestTaskId());
        ResponseEntity<String> uploadReslut = AppStoreUtil.storeToAppStore(map, user);
        checkInnerParamNull(uploadReslut, "upload app to appstore fail!");

        LOGGER.info("upload appstore result:{}", uploadReslut);
        JsonObject jsonObject = new JsonParser().parse(uploadReslut.getBody()).getAsJsonObject();
        JsonElement appStoreAppId = jsonObject.get("appId");
        JsonElement appStorePackageId = jsonObject.get("packageId");

        checkInnerParamNull(appStoreAppId, "response from upload to appstore does not contain appId");
        checkInnerParamNull(appStorePackageId, "response from upload to appstore does not contain packageId");

        ResponseEntity<String> publishRes = AppStoreUtil
            .publishToAppStore(appStoreAppId.getAsString(), appStorePackageId.getAsString(), user.getToken(),
                publishAppReqDto);
        checkInnerParamNull(publishRes, "publish app to appstore fail!");
        return true;
    }

    private String getAppPkgDecompressPath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + packageId
            + File.separator + "decompress-" + packageId + File.separator;
    }

    private String getAppPkgPath(String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + packageId
            + File.separator;
    }

    private List<File> getIconList(String packageId) {
        String iconPath = getAppPkgDecompressPath(packageId) + DOCS_ICON_PATH;
        File icon = new File(iconPath);
        return Arrays.stream(icon.listFiles()).filter(item -> item.getName().endsWith("jpg")||item.getName().endsWith("png"))
            .collect(Collectors.toList());
    }

    private <T> void checkInnerParamNull(T innerParam, String msg) {
        if (null == innerParam) {
            LOGGER.error(msg);
            throw new IllegalRequestException(msg, ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
    }
}
