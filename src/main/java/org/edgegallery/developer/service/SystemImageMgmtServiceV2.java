
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

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.system.*;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.FileHashCode;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service("systemImageMgmtServiceV2")
public class SystemImageMgmtServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    private static final String FILE_FORMAT_QCOW2 = "qcow2";

    private static final String FILE_FORMAT_ISO = "iso";

    @Autowired
    private SystemImageMapper systemImageMapper;

    /**
     * getSystemImage.
     *
     * @param mepGetSystemImageReq mepGetSystemImageReq
     * @return
     */
    public Either<FormatRespDto, MepGetSystemImageRes> getSystemImages(MepGetSystemImageReq mepGetSystemImageReq) {
        try {
            LOGGER.info("Query SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!SystemImageUtil.isAdminUser()) {
                mepGetSystemImageReq.setUserId(userId);
            }
            MepSystemQueryCtrl queryCtrl = mepGetSystemImageReq.getQueryCtrl();
            if (queryCtrl.getSortBy() == null || queryCtrl.getSortBy().equalsIgnoreCase("createTime")) {
                queryCtrl.setSortBy("create_time");
            } else if (queryCtrl.getSortBy().equalsIgnoreCase("userName")) {
                queryCtrl.setSortBy("user_name");
            }
            if (queryCtrl.getSortOrder() == null) {
                queryCtrl.setSortBy("DESC");
            }
            String createTimeBegin = mepGetSystemImageReq.getCreateTimeBegin();
            String createTimeEnd = mepGetSystemImageReq.getCreateTimeEnd();
            if (!StringUtils.isBlank(createTimeBegin)) {
                mepGetSystemImageReq.setCreateTimeBegin(createTimeBegin + " 00:00:00");
            }
            if (!StringUtils.isBlank(createTimeEnd)) {
                mepGetSystemImageReq.setCreateTimeEnd(createTimeEnd + " 23:59:59");
            }
            mepGetSystemImageReq.setQueryCtrl(queryCtrl);
            MepGetSystemImageRes mepGetSystemImageRes = new MepGetSystemImageRes();
            mepGetSystemImageRes.setTotalCount(systemImageMapper.getSystemImagesCount(mepGetSystemImageReq));
            mepGetSystemImageRes.setImageList(systemImageMapper.getSystemImagesByCondition(mepGetSystemImageReq));
            return Either.right(mepGetSystemImageRes);
        } catch (Exception e) {
            LOGGER.error("Query SystemImages failed");
            throw new DeveloperException("Get system image failed", ResponseConsts.RET_GET_SYSTEM_IMAGE_FAILED);
        }
    }

    /**
     * createSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> createSystemImage(VmSystem vmImage) {
        try {
            LOGGER.info("Create SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (StringUtils.isBlank(vmImage.getSystemName())) {
                LOGGER.error("SystemName is blank.");
                throw new DeveloperException("SystemName is blank", ResponseConsts.RET_SYSTEM_NAME_BLANK);
            }
            vmImage.setUserId(userId);
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), null, userId) > 0) {
                LOGGER.error("SystemName can not duplicate.");
                throw new DeveloperException("SystemName can not duplicate", ResponseConsts.RET_SYSTEM_NAME_DUPLICATE);
            }
            vmImage.setUserId(AccessUserUtil.getUser().getUserId());
            vmImage.setUserName(AccessUserUtil.getUser().getUserName());
            vmImage.setStatus(EnumSystemImageStatus.UPLOAD_WAIT);
            int ret = systemImageMapper.createSystemImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Crete SystemImage {} success ", vmImage.getUserId());
                return Either.right(true);
            }
            LOGGER.error("Create SystemImage failed.");
            throw new DeveloperException("Create system image failed", ResponseConsts.RET_CREATE_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Create SystemImages exception.");
            throw new DeveloperException("Create system image exception", ResponseConsts.RET_CREATE_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * updateSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> updateSystemImage(VmSystem vmImage, Integer systemId) {
        try {
            LOGGER.info("Update SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!SystemImageUtil.isAdminUser()) {
                vmImage.setUserId(userId);
            }
            VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
            if (StringUtils.isAnyBlank(vmImage.getSystemName(), vmSystemImage.getUserId())) {
                LOGGER.error("SystemName is blank or systemImage is not exist.");
                throw new DeveloperException("SystemName is blank or systemImage is not exists", ResponseConsts.RET_SYSTEM_NAME_BLANK_OR_IMAGE_NOT_EXISTS);
            }
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), systemId,
                    vmSystemImage.getUserId()) > 0) {
                LOGGER.error("SystemName can not duplicate.");
                throw new DeveloperException("SystemName can not duplicate", ResponseConsts.RET_SYSTEM_NAME_DUPLICATE);
            }
            vmImage.setSystemId(systemId);

            int ret = systemImageMapper.updateSystemImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Update SystemImage success systemId = {}, userId = {}", systemId, userId);
                return Either.right(true);
            }
            LOGGER.error("Update system image failed.");
            throw new DeveloperException("Update system image failed", ResponseConsts.RET_UPDATE_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Update system image exception.");
            throw new DeveloperException("Update system image exception", ResponseConsts.RET_UPDATE_SYSTEM_IMAGE_EXCEPTION);
        }
    }


    /**
     * publishSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> publishSystemImage(Integer systemId) {
        try {
            LOGGER.info("Publish SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            int ret = systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.PUBLISHED.toString());
            if (ret > 0) {
                LOGGER.info("Publish SystemImage {} success ", userId);
                return Either.right(true);
            }
            LOGGER.error("Publish system image failed.");
            throw new DeveloperException("Publish system image exception", ResponseConsts.RET_PUBLISH_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Publish system image exception.");
            throw new DeveloperException("Publish system image exception", ResponseConsts.RET_PUBLISH_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteSystemImage(Integer systemId) {
        try {
            LOGGER.info("Delete SystemImage start");
            VmSystem vmImage = new VmSystem();
            String userId = AccessUserUtil.getUser().getUserId();
            if (!SystemImageUtil.isAdminUser()) {
                vmImage.setUserId(userId);
            }
            vmImage.setSystemId(systemId);

            LOGGER.info("delete system image on remote server.");
            if (!deleteImageFileOnRemote(systemId)) {
                LOGGER.error("delete system image on remote server failed.");
                throw new DeveloperException("Delete system image exception", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_ON_REMOTE_SERVER_FAILED);
            }

            LOGGER.info("delete system image record in database.");
            int res = systemImageMapper.deleteSystemImage(vmImage);
            if (res < 1) {
                LOGGER.error("Delete SystemImage {} failed", userId);
                throw new DeveloperException("Delete system image failed", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_FAILED);
            }
            LOGGER.info("Delete SystemImage {} success", userId);
            return Either.right(true);
        } catch (Exception e) {
            LOGGER.error("Delete system image exception.");
            throw new DeveloperException("Delete system image exception", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * upload system image.
     *
     * @param request  HTTP Servlet Request
     * @param chunk    File Chunk
     * @param systemId System Image ID
     * @return Resposne
     * @throws IOException IOException
     */
    public ResponseEntity uploadSystemImage(HttpServletRequest request, Chunk chunk, Integer systemId) {
        try {
            LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
                    chunk.getIdentifier(), chunk.getChunkNumber());

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                LOGGER.error("upload request is invalid.");
                throw new DeveloperException("upload request is invalid", ResponseConsts.RET_REQUEST_INVALID);
            }

            MultipartFile file = chunk.getFile();
            if (file == null) {
                LOGGER.error("there is no needed file");
                throw new DeveloperException("there is no needed file", ResponseConsts.RET_NO_NEEDED_FILE);
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                LOGGER.error("invalid chunk number.");
                throw new DeveloperException("invalid chunk number", ResponseConsts.RET_CHUNK_NUMBER_INVALID);
            }

            LOGGER.info("update system image status and upload file.");
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING.toString());
            String rootDir = SystemImageUtil.getUploadSysImageRootDir(systemId);
            File uploadRootDir = new File(rootDir);
            if (!uploadRootDir.exists()) {
                boolean isMk = uploadRootDir.mkdirs();
                if (!isMk) {
                    LOGGER.error("create temporary upload path failed");
                    systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                    throw new DeveloperException("create temporary upload path failed", ResponseConsts.RET_TEMPORARY_PATH_FAILED);
                }
            }

            File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            LOGGER.error("upload system image file exception.");
            throw new DeveloperException("upload system image file exception", ResponseConsts.RET_UPLOAD_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * cancel upload system image.
     *
     * @param systemId System Image ID
     * @return Resposne
     */
    public ResponseEntity cancelUploadSystemImage(Integer systemId) {
        LOGGER.info("cancel upload system image file, systemId = {}, ", systemId);

        VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
        if (EnumSystemImageStatus.UPLOADING_MERGING == vmSystemImage.getStatus()) {
            LOGGER.error("system image is merging, it cannot be cancelled.");
            throw new DeveloperException("system image is merging, it cannot be cancelled", ResponseConsts.RET_SYSTEM_IMAGE_CANCELLED_FAILED);
        }

        LOGGER.info("execute cancel action.");
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_CANCELLED.toString());
        String rootDir = SystemImageUtil.getUploadSysImageRootDir(systemId);
        SystemImageUtil.cleanWorkDir(new File(rootDir));
        return ResponseEntity.ok().build();
    }

    /**
     * merge system image.
     *
     * @param fileName   Merged File Name
     * @param identifier File Identifier
     * @param systemId   System Image ID
     * @return Resposne
     * @throws IOException IOException
     */
    public ResponseEntity mergeSystemImage(String fileName, String identifier, Integer systemId) {
        try {
            LOGGER.info("merge system image file, systemId = {}, fileName = {}, identifier = {}", systemId, fileName,
                    identifier);
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING_MERGING.toString());

            String rootDir = SystemImageUtil.getUploadSysImageRootDir(systemId);
            String partFilePath = rootDir + identifier;
            File partFileDir = new File(partFilePath);
            if (!partFileDir.exists() || !partFileDir.isDirectory()) {
                LOGGER.error("uploaded part file path not found!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new DeveloperException("uploaded part file path not found", ResponseConsts.RET_FILE_PATH_NOT_FOUND);
            }

            File[] partFiles = partFileDir.listFiles();
            if (partFiles == null || partFiles.length == 0) {
                LOGGER.error("uploaded part file not found!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new DeveloperException("uploaded part file not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            File mergedFile = new File(rootDir + File.separator + fileName);
            FileOutputStream mergedFileStream = new FileOutputStream(mergedFile, true);
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, mergedFileStream);
            }
            mergedFileStream.close();

            LOGGER.info("process merged file.");
            Either<UploadFileInfo, FormatRespDto> processResult = processMergedFile(mergedFile);
            if (processResult.isRight()) {
                LOGGER.error("process merged file failed!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                SystemImageUtil.cleanWorkDir(mergedFile.getParentFile());
                throw new DeveloperException("process merged file failed", ResponseConsts.RET_PROCESS_MERGED_FILE_FAILED);
            }

            LOGGER.info("delete old system image on remote server.");
            if (!deleteImageFileOnRemote(systemId)) {
                LOGGER.error("delete old system image on remote server failed!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                SystemImageUtil.cleanWorkDir(mergedFile.getParentFile());
                throw new DeveloperException("delete old system image on remote server failed", ResponseConsts.RET_DELETE_IMAGE_OR_SERVER_FAILED);
            }

            LOGGER.info("push system image file to remote server.");
            String uploadedSystemPath = SystemImageUtil.pushSystemImage(mergedFile);
            if (StringUtils.isEmpty(uploadedSystemPath)) {
                LOGGER.error("push system image file failed!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new DeveloperException("push system image file failed", ResponseConsts.RET_PUSH_IMAGE_FILE_FAILED);
            }

            LOGGER.info("system image file upload succeed.");
            UploadFileInfo uploadFileInfo = processResult.getLeft();
            uploadFileInfo.assign(systemId, FILE_FORMAT_QCOW2.equalsIgnoreCase(uploadFileInfo.getFileFormat())
                    ? EnumSystemImageStatus.PUBLISHED
                    : EnumSystemImageStatus.UPLOAD_SUCCEED, uploadedSystemPath);
            systemImageMapper.updateSystemImageUploadInfo(uploadFileInfo);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            LOGGER.error("process merged file exception!");
            throw new DeveloperException("process merged file exception", ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
        }
    }

    /**
     * downloadSystemImage.
     *
     * @param systemId systemId
     * @return
     */
    public ResponseEntity<byte[]> downloadSystemImage(Integer systemId) {
        Assert.notNull(systemImageMapper.getSystemImagesPath(systemId), "systemPath is null");
        try {
            String systemPath = systemImageMapper.getSystemImagesPath(systemId);
            String url = systemPath + "?isZip=true";
            byte[] dataStream = HttpClientUtil.downloadSystemImage(url);
            if (dataStream == null) {
                LOGGER.error("download SystemImage null!");
                throw new DeveloperException("download SystemImage null", ResponseConsts.RET_DOWNLOAD_SYSTEM_IMAGE_NULL);
            }
            LOGGER.info("download SystemImage succeed!");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            VmSystem vmSystem = systemImageMapper.getVmImage(systemId);
            String fileName = vmSystem.getFileName();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            return ResponseEntity.ok().headers(headers).body(dataStream);
        } catch (Exception e) {
            LOGGER.error("download SystemImage failed!");
            throw new DeveloperException("download SystemImage exception", ResponseConsts.RET_DOWNLOAD_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    private Either<UploadFileInfo, FormatRespDto> processMergedFile(File mergedFile) {
        try (ZipFile zipFile = new ZipFile(mergedFile)) {
            String fileMd5 = null;
            String fileFormat = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                fileFormat = name.substring(name.lastIndexOf(".") + 1, name.length());
                if (fileFormat.equalsIgnoreCase(FILE_FORMAT_QCOW2) || fileFormat.equalsIgnoreCase(FILE_FORMAT_ISO)) {
                    fileMd5 = FileHashCode.md5HashCode32(zipFile.getInputStream(entry));
                    return Either.left(new UploadFileInfo(mergedFile.getName(), fileMd5, fileFormat));
                }
            }
            LOGGER.error("zipFile format is mistake!");
            throw new DeveloperException("zipFile format is mistake", ResponseConsts.RET_ZIP_FILE_INVALID);
        } catch (Exception e) {
            LOGGER.error("process merged zip file failed, {}", e.getMessage());
            throw new DeveloperException("process merged zip file failed", ResponseConsts.RET_ZIP_FILE_EXCEPTION);
        }
    }


    private boolean deleteImageFileOnRemote(Integer systemId) {
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        if (StringUtils.isEmpty(systemPath)) {
            LOGGER.debug("system path is invalid, no need to delete.");
            return true;
        }

        try {
            String url = systemPath.substring(0, systemPath.length() - 16);
            if (!HttpClientUtil.deleteSystemImage(url)) {
                LOGGER.error("delete SystemImage on remote failed!");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("delete old SystemImage failed, {}", e.getMessage());
            return false;
        }

        return true;
    }

}
