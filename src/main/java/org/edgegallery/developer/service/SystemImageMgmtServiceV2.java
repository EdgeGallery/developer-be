
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.model.system.UploadFileInfo;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.FileHashCode;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

@Service("systemImageMgmtServiceV2")
public class SystemImageMgmtServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    private static final String FILE_FORMAT_QCOW2 = "qcow2";

    private static final String FILE_FORMAT_ISO = "iso";

    @Value("${fileserver.address}")
    private String fileServerAddress;

    @Autowired
    private SystemImageMapper systemImageMapper;

    /**
     * getSystemImage.
     *
     * @param mepGetSystemImageReq mepGetSystemImageReq
     * @return
     */
    public Either<FormatRespDto, MepGetSystemImageRes> getSystemImages(MepGetSystemImageReq mepGetSystemImageReq) {
        LOGGER.info("Query SystemImage start");
        String userId = AccessUserUtil.getUser().getUserId();
        if (!SystemImageUtil.isAdminUser()) {
            mepGetSystemImageReq.setUserId(userId);
        }
        MepSystemQueryCtrl queryCtrl = mepGetSystemImageReq.getQueryCtrl();
        if (queryCtrl.getSortBy() == null || queryCtrl.getSortBy().equalsIgnoreCase("uploadTime")) {
            queryCtrl.setSortBy("upload_time");
        } else if (queryCtrl.getSortBy().equalsIgnoreCase("userName")) {
            queryCtrl.setSortBy("user_name");
        }
        if (queryCtrl.getSortOrder() == null) {
            queryCtrl.setSortBy("DESC");
        }
        String uploadTimeBegin = mepGetSystemImageReq.getUploadTimeBegin();
        String uploadTimeEnd = mepGetSystemImageReq.getUploadTimeEnd();
        if (!StringUtils.isBlank(uploadTimeBegin)) {
            mepGetSystemImageReq.setUploadTimeBegin(uploadTimeBegin + " 00:00:00");
        }
        if (!StringUtils.isBlank(uploadTimeEnd)) {
            mepGetSystemImageReq.setUploadTimeEnd(uploadTimeEnd + " 23:59:59");
        }
        mepGetSystemImageReq.setQueryCtrl(queryCtrl);
        MepGetSystemImageRes mepGetSystemImageRes = new MepGetSystemImageRes();
        Map map = new HashMap<>();
        map.put("systemName",mepGetSystemImageReq.getSystemName());
        if(StringUtils.isNotEmpty(mepGetSystemImageReq.getType())){
            map.put("types",SystemImageUtil.splitParam(mepGetSystemImageReq.getType()));
        }else {
            map.put("types",null);
        }
        map.put("userId",mepGetSystemImageReq.getUserId());
        if(StringUtils.isNotEmpty(mepGetSystemImageReq.getOperateSystem())){
            map.put("operateSystems", SystemImageUtil.splitParam(mepGetSystemImageReq.getOperateSystem()));
        }else {
            map.put("operateSystems", null);
        }
        if(StringUtils.isNotEmpty(mepGetSystemImageReq.getStatus())){
            map.put("statusList", SystemImageUtil.splitParam(mepGetSystemImageReq.getStatus()));
        }else {
            map.put("statusList", null);
        }
        map.put("uploadTimeBegin",mepGetSystemImageReq.getUploadTimeBegin());
        map.put("uploadTimeEnd",mepGetSystemImageReq.getUploadTimeEnd());
        map.put("queryCtrl",mepGetSystemImageReq.getQueryCtrl());
        mepGetSystemImageRes.setTotalCount(systemImageMapper.getSystemImagesCount(map));
        mepGetSystemImageRes.setImageList(systemImageMapper.getSystemImagesByCondition(map));
        return Either.right(mepGetSystemImageRes);

    }

    /**
     * createSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> createSystemImage(VmSystem vmImage) {
        LOGGER.info("Create SystemImage start");
        String userId = AccessUserUtil.getUser().getUserId();
        if (StringUtils.isBlank(vmImage.getSystemName())) {
            LOGGER.error("SystemName is blank.");
            throw new IllegalRequestException("SystemName is blank", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        vmImage.setUserId(userId);
        if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), null, userId) > 0) {
            LOGGER.error("SystemName can not duplicate.");
            throw new DataBaseException("SystemName can not duplicate", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        vmImage.setUserId(AccessUserUtil.getUser().getUserId());
        vmImage.setUserName(AccessUserUtil.getUser().getUserName());
        vmImage.setStatus(EnumSystemImageStatus.UPLOAD_WAIT);
        int ret = systemImageMapper.createSystemImage(vmImage);
        if (ret < 1) {
            LOGGER.error("Create SystemImage failed.");
            throw new DataBaseException("Create system image failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        LOGGER.info("Crete SystemImage {} success ", vmImage.getSystemId());
        return Either.right(true);
    }

    /**
     * updateSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> updateSystemImage(VmSystem vmImage, Integer systemId) {
        LOGGER.info("Update SystemImage start");
        String userId = AccessUserUtil.getUser().getUserId();
        if (!SystemImageUtil.isAdminUser()) {
            vmImage.setUserId(userId);
        }
        VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
        if (StringUtils.isAnyBlank(vmImage.getSystemName(), vmSystemImage.getUserId())) {
            String msg = "SystemName is blank or systemImage is not exist.";
            LOGGER.error(msg);
            throw new EntityNotFoundException(msg, ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), systemId, vmSystemImage.getUserId()) > 0) {
            LOGGER.error("SystemName can not duplicate.");
            throw new DataBaseException("SystemName can not duplicate", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        vmImage.setSystemId(systemId);

        int ret = systemImageMapper.updateSystemImage(vmImage);
        if (ret < 1) {
            LOGGER.error("Update system image failed.");
            throw new DataBaseException("Update system image failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        LOGGER.info("Update SystemImage success systemId = {}, userId = {}", systemId, userId);
        return Either.right(true);
    }

    /**
     * publishSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> publishSystemImage(Integer systemId) {
        LOGGER.info("Publish SystemImage start");
        String userId = AccessUserUtil.getUser().getUserId();
        int ret = systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.PUBLISHED.toString());
        if (ret < 1) {
            LOGGER.error("Publish system image failed.");
            throw new DataBaseException("Publish system image exception", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        LOGGER.info("Publish SystemImage {} success ", systemId);
        return Either.right(true);
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteSystemImage(Integer systemId) {
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
            throw new FileOperateException("Delete remote system image exception", ResponseConsts.RET_DELETE_FILE_FAIL);
        }

        LOGGER.info("delete system image record in database.");
        int res = systemImageMapper.deleteSystemImage(vmImage);
        if (res < 1) {
            LOGGER.error("Delete SystemImage {} failed", systemId);
            throw new DataBaseException("Delete system image failed", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        LOGGER.info("Delete SystemImage {} success", systemId);
        return Either.right(true);
    }

    /**
     * upload system image.
     *
     * @param request HTTP Servlet Request
     * @param chunk File Chunk
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
                throw new IllegalRequestException("upload request is invalid", ResponseConsts.RET_REQUEST_FORMAT_ERROR);
            }

            MultipartFile file = chunk.getFile();
            if (file == null) {
                LOGGER.error("there is no needed file");
                throw new FileFoundFailException("there is no needed file", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                LOGGER.error("invalid chunk number.");
                throw new IllegalRequestException("invalid chunk number", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
            }

            LOGGER.info("update system image status.");
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING.toString());
            LOGGER.info("save file to local directory.");
            String rootDir = SystemImageUtil.getUploadSysImageRootDir(systemId);
            File uploadRootDir = new File(rootDir);
            if (!uploadRootDir.exists()) {
                boolean isMk = uploadRootDir.mkdirs();
                if (!isMk) {
                    LOGGER.error("create temporary upload path failed");
                    systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                    throw new FileOperateException("create temporary upload path failed",
                        ResponseConsts.RET_CREATE_FILE_FAIL);
                }
            }

            File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
            LOGGER.info("upload to remote file server.");
            if (!HttpClientUtil.sliceUploadFile(fileServerAddress, chunk, outFile.getAbsolutePath())) {
                LOGGER.error("upload to remote file server failed.");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new FileOperateException("upload to remote file server failed", ResponseConsts.RET_UPLOAD_FILE_FAIL);
            }
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            LOGGER.error("upload system image file exception.{}", e.getMessage());
            throw new FileOperateException("upload system image file exception", ResponseConsts.RET_UPLOAD_FILE_FAIL);
        }
    }

    /**
     * cancel upload system image.
     *
     * @param systemId System Image ID
     * @param identifier
     * @return Resposne
     */
    public ResponseEntity cancelUploadSystemImage(Integer systemId, String identifier) {
        LOGGER.info("cancel upload system image file, systemId = {}, ", systemId);

        VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
        if (EnumSystemImageStatus.UPLOADING_MERGING == vmSystemImage.getStatus()) {
            LOGGER.error("system image is merging, it cannot be cancelled.");
            throw new DataBaseException("system image is merging, it cannot be cancelled",
                ResponseConsts.RET_QUERY_DATA_FAIL);
        }

        LOGGER.info("delete old system image on remote server.");
        deleteImageFileOnRemote(systemId);

        LOGGER.info("cancel request to remote file server.");
        if (!SystemImageUtil.cancelOnRemoteFileServer(identifier)) {
            LOGGER.error("remote file server cancel failed.");
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        LOGGER.info("update status and remove local directory.");
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_CANCELLED.toString());
        String rootDir = SystemImageUtil.getUploadSysImageRootDir(systemId);
        SystemImageUtil.cleanWorkDir(new File(rootDir));
        return ResponseEntity.ok().build();
    }

    /**
     * merge system image.
     *
     * @param fileName Merged File Name
     * @param identifier File Identifier
     * @param systemId System Image ID
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
                throw new FileFoundFailException("uploaded part file path not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            File[] partFiles = partFileDir.listFiles();
            if (partFiles == null || partFiles.length == 0) {
                LOGGER.error("uploaded part file not found!");
                SystemImageUtil.cancelOnRemoteFileServer(identifier);
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new FileFoundFailException("uploaded part file not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            File mergedFile = new File(rootDir + File.separator + fileName);
            FileOutputStream mergedFileStream = new FileOutputStream(mergedFile, true);
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, mergedFileStream);
                partFile.delete();
            }
            mergedFileStream.close();

            LOGGER.info("process merged file.");
            Either<UploadFileInfo, FormatRespDto> processResult = processMergedFile(mergedFile);
            if (processResult.isRight()) {
                LOGGER.error("process merged file failed!");
                SystemImageUtil.cancelOnRemoteFileServer(identifier);
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new FileOperateException("process merged file failed", ResponseConsts.RET_UPLOAD_FILE_FAIL);
            }

            LOGGER.info("delete old system image on remote server.");
            deleteImageFileOnRemote(systemId);

            LOGGER.info("merge on remote file server.");
            String uploadedSystemPath = SystemImageUtil.mergeOnRemoteFileServer(identifier, fileName);

            if (StringUtils.isEmpty(uploadedSystemPath)) {
                LOGGER.error("merge failed on remote file server!");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                throw new FileOperateException("push system image file failed", ResponseConsts.RET_PUSH_VM_IMAGE_FAIL);
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
            throw new FileOperateException("process merged file exception", ResponseConsts.RET_MERGE_FILE_FAIL);
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
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        String url = systemPath + "?isZip=true";
        byte[] dataStream = HttpClientUtil.downloadSystemImage(url);
        if (dataStream == null) {
            LOGGER.error("download SystemImage null!");
            throw new FileOperateException("download SystemImage null", ResponseConsts.RET_DOWNLOAD_FILE_EMPTY);
        }
        LOGGER.info("download SystemImage succeed!");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        VmSystem vmSystem = systemImageMapper.getVmImage(systemId);
        String fileName = vmSystem.getFileName();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(dataStream);
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
            throw new FileOperateException("zipFile format is mistake", ResponseConsts.RET_FILE_FORMAT_ERROR);
        } catch (IOException e) {
            LOGGER.error("process merged zip file failed, {}", e.getMessage());
            throw new FileOperateException("process merged zip file failed", ResponseConsts.RET_MERGE_FILE_FAIL);
        }
    }

    private boolean deleteImageFileOnRemote(Integer systemId) {
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        if (StringUtils.isEmpty(systemPath)) {
            LOGGER.debug("system path is invalid, no need to delete.");
            return true;
        }
        String url = systemPath.substring(0, systemPath.length() - 16);
        if (!HttpClientUtil.deleteSystemImage(url)) {
            LOGGER.error("delete SystemImage on remote failed!");
            return false;
        }
        return true;
    }
}
