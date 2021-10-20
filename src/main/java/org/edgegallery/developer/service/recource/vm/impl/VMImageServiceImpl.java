/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.recource.vm.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.RestfulRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.mapper.resource.vm.VMImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.restful.VMImageQuery;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.edgegallery.developer.model.resource.vm.EnumProcessErrorType;
import org.edgegallery.developer.model.resource.vm.EnumVmImageSlimStatus;
import org.edgegallery.developer.model.resource.vm.EnumVmImageStatus;
import org.edgegallery.developer.model.resource.vm.UploadFileInfo;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.model.workspace.EnumSystemImageSlimStatus;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.FileHashCode;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Service("VMImageService")
public class VMImageServiceImpl implements VMImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VMImageServiceImpl.class);

    private static final String SUBDIR_VMIMAGE = "SystemImage";

    private static final String FILE_FORMAT_QCOW2 = "qcow2";

    private static final String FILE_FORMAT_ISO = "iso";

    private static final String FILE_SLIM_PATH = "/action/slim";

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 30 * 1000L;

    @Value("${fileserver.address}")
    private String fileServerAddress;

    @Autowired
    private VMImageMapper vmImageMapper;

    @Override
    public VMImageRes getVmImages(VMImageReq vmImageReq) {
        try {
            LOGGER.info("Query vm image start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                vmImageReq.setUserId(userId);
            }
            VMImageQuery queryCtrl = vmImageReq.getQueryCtrl();
            if (queryCtrl.getSortBy() == null || queryCtrl.getSortBy().equalsIgnoreCase("uploadTime")) {
                queryCtrl.setSortBy("upload_time");
            } else if (queryCtrl.getSortBy().equalsIgnoreCase("userName")) {
                queryCtrl.setSortBy("user_name");
            }
            if (queryCtrl.getSortOrder() == null) {
                queryCtrl.setSortOrder("DESC");
            }
            String uploadTimeBegin = vmImageReq.getUploadTimeBegin();
            String uploadTimeEnd = vmImageReq.getUploadTimeEnd();
            if (!StringUtils.isBlank(uploadTimeBegin)) {
                vmImageReq.setUploadTimeBegin(uploadTimeBegin + " 00:00:00");
            }
            if (!StringUtils.isBlank(uploadTimeEnd)) {
                vmImageReq.setUploadTimeEnd(uploadTimeEnd + " 23:59:59");
            }
            vmImageReq.setQueryCtrl(queryCtrl);
            VMImageRes vmImageRes = new VMImageRes();
            Map map = new HashMap<>();
            map.put("name", vmImageReq.getName());
            if (StringUtils.isNotEmpty(vmImageReq.getVisibleType())) {
                map.put("visibleTypes", SystemImageUtil.splitParam(vmImageReq.getVisibleType()));
            } else {
                map.put("visibleTypes", null);
            }
            map.put("userId", vmImageReq.getUserId());
            if (StringUtils.isNotEmpty(vmImageReq.getOsType())) {
                map.put("osTypes", SystemImageUtil.splitParam(vmImageReq.getOsType()));
            } else {
                map.put("osTypes", null);
            }
            if (StringUtils.isNotEmpty(vmImageReq.getStatus())) {
                map.put("statusList", SystemImageUtil.splitParam(vmImageReq.getStatus()));
            } else {
                map.put("statusList", null);
            }
            map.put("uploadTimeBegin", vmImageReq.getUploadTimeBegin());
            map.put("uploadTimeEnd", vmImageReq.getUploadTimeEnd());
            map.put("queryCtrl", vmImageReq.getQueryCtrl());
            vmImageRes.setTotalCount(vmImageMapper.getVmImagesCount(map));
            vmImageRes.setImageList(vmImageMapper.getVmImagesByCondition(map));
            return vmImageRes;
        } catch (Exception e) {
            LOGGER.error("Query vm images failed {}", e.getMessage());
            throw new DataBaseException("Query vm images failed", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    @Override
    public Boolean createVmImage(VMImage vmImage) {
        try {
            LOGGER.info("Create vm images start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (StringUtils.isBlank(vmImage.getName())) {
                LOGGER.error("Create vm images failed");
                throw new IllegalRequestException("Create vm images failed", ResponseConsts.RET_CERATE_DATA_FAIL);
            }
            vmImage.setUserId(userId);
            if (vmImageMapper.getVmNameCount(vmImage.getName(), null, userId) > 0) {
                LOGGER.error("image Name can not duplicate.");
                throw new IllegalRequestException("image Name can not duplicate.", ResponseConsts.RET_CERATE_DATA_FAIL);
            }
            vmImage.setUserId(AccessUserUtil.getUser().getUserId());
            vmImage.setUserName(AccessUserUtil.getUser().getUserName());
            vmImage.setStatus(EnumVmImageStatus.UPLOAD_WAIT);
            int ret = vmImageMapper.createVmImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Create vm image {} success ", vmImage.getUserId());
                return true;
            }
            LOGGER.error("Create vm image failed.");
            throw new DataBaseException("Create vm image failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        } catch (Exception e) {
            LOGGER.error("Create vm image failed {}", e.getMessage());
            throw new DataBaseException("Create vm image failed", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    @Override
    public Boolean updateVmImage(VMImage vmImage, Integer imageId) {
        try {
            LOGGER.info("Update vm image start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                vmImage.setUserId(userId);
            }
            VMImage oldVmImage = vmImageMapper.getVmImage(imageId);
            if (StringUtils.isAnyBlank(vmImage.getName(), oldVmImage.getUserId())) {
                LOGGER.error("Update vm image failed");
                throw new IllegalRequestException("Update vm image failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
            }
            if (vmImageMapper.getVmNameCount(vmImage.getName(), imageId, oldVmImage.getUserId())
                > 0) {
                LOGGER.error("name can not duplicate.");
                throw new IllegalRequestException("name can not duplicate.", ResponseConsts.RET_UPDATE_DATA_FAIL);
            }
            vmImage.setId(imageId);

            int ret = vmImageMapper.updateVmImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Update vm image success imageId = {}, userId = {}", imageId, userId);
                return true;
            }
            LOGGER.error("Update vm image failed ");
            throw new DataBaseException("Update vm image failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        } catch (Exception e) {
            LOGGER.error("Update vm image failed {}", e.getMessage());
            throw new DataBaseException("Update vm image failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
    }

    @Override
    public Boolean deleteVmImage(Integer imageId) {
        LOGGER.info("Delete vm image start");
        VMImage vmImage = new VMImage();
        String userId = AccessUserUtil.getUser().getUserId();
        if (!isAdminUser()) {
            vmImage.setUserId(userId);
        }
        vmImage.setId(imageId);

        LOGGER.info("delete vm image on remote server.");
        if (!deleteImageFileOnRemote(imageId)) {
            LOGGER.error("delete vm image on remote server failed.");
            throw new RestfulRequestException("delete vm image on remote server failed.", ResponseConsts.RET_RESTFUL_REQUEST_FAIL);
        }

        LOGGER.info("delete vm image record in database.");
        int res = vmImageMapper.deleteVmImage(vmImage);
        if (res < 1) {
            LOGGER.error("Delete vm image {} failed", userId);
            throw new DataBaseException("Delete vm image failed", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        LOGGER.info("Delete vm images {} success", userId);
        return true;
    }

    @Override
    public Boolean publishVmImage(Integer imageId) {

        LOGGER.info("Publish vm image start");
        int ret = vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.PUBLISHED.toString());
        if (ret > 0) {
            LOGGER.info("Publish vm image {} success ", imageId);
            return true;
        }
        LOGGER.error("Publish vm image failed ");
        throw new DataBaseException("Publish vm image failed", ResponseConsts.RET_UPDATE_DATA_FAIL);
    }

    /**
     * reset image status.
     *
     * @param imageId vm image id
     * @return
     */
    @Override
    public Boolean resetImageStatus(Integer imageId) {
        LOGGER.info("Reset vm image status, systemId = {}", imageId);
        VMImage vmImage = vmImageMapper.getVmImage(imageId);
        if (vmImage == null) {
            LOGGER.error("vm image not found, systemId = {}", imageId);
            throw new IllegalRequestException("vm image not found", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        if (!isAdminUser() && !vmImage.getUserId().equalsIgnoreCase(AccessUserUtil.getUserId())) {
            LOGGER.error("forbidden reset the image");
            throw new UnauthorizedException("forbidden reset the image", ResponseConsts.RET_REQUEST_UNAUTHORIZED);
        }

        LOGGER.info("clean uploaded file.");
        cleanUploadedFile(imageId, vmImage.getFileIdentifier());

        LOGGER.info("update vm image status to upload_wait.");
        vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_WAIT.toString());
        return true;
    }

    @Override
    public ResponseEntity uploadVmImage(HttpServletRequest request, Chunk chunk, Integer imageId) {
        LOGGER.info("upload vm image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
            chunk.getIdentifier(), chunk.getChunkNumber());

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            LOGGER.error("upload request is invalid.");
            return ResponseEntity.badRequest().build();
        }

        MultipartFile file = chunk.getFile();
        if (file == null) {
            LOGGER.error("there is no needed file");
            return ResponseEntity.badRequest().build();
        }

        Integer chunkNumber = chunk.getChunkNumber();
        if (chunkNumber == null) {
            LOGGER.error("invalid chunk number.");
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("update vm image status to uploading and file identifier.");
        vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOADING.toString());
        vmImageMapper.updateVmImageIdentifier(imageId, chunk.getIdentifier());

        LOGGER.info("save file to local directory.");
        String rootDir = getUploadVmImageRootDir(imageId);
        File uploadRootDir = new File(rootDir);
        if (!uploadRootDir.exists()) {
            boolean isMk = uploadRootDir.mkdirs();
            if (!isMk) {
                LOGGER.error("create temporary upload path failed");
                vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
                return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
            }
        }

        File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
        try {
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        } catch (Exception e) {
            throw new FileOperateException("get file stream fail  when upload vm image", ResponseConsts.RET_UPLOAD_FILE_FAIL);
        }


        LOGGER.info("upload to remote file server.");
        if (!HttpClientUtil.sliceUploadFile(fileServerAddress, chunk, outFile.getAbsolutePath())) {
            LOGGER.error("upload to remote file server failed.");
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            vmImageMapper
                .updateVmImageErrorType(imageId, EnumProcessErrorType.FILESYSTEM_UPLOAD_FAILED.getErrorType());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public List<Integer> checkUploadedChunks(Integer imageId, String identifier) {
        LOGGER.info("check uploaded chunks, imageId = {}, identifier = {}", imageId, identifier);
        String rootDir = getUploadVmImageRootDir(imageId);
        String partFilePath = rootDir + identifier;
        File partFileDir = new File(partFilePath);
        if (!partFileDir.exists() || !partFileDir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            return Collections.emptyList();
        }

        List<Integer> uploadedChunks = new ArrayList<>();
        for (File partFile : partFiles) {
            String partFileName = partFile.getName();
            uploadedChunks.add(Integer.parseInt(partFileName.substring(0, partFileName.indexOf('.'))));
        }
        Collections.sort(uploadedChunks);

        int retransPartCount = uploadedChunks.size() > Consts.UPLOAD_RETRANSPART_COUNT
            ? Consts.UPLOAD_RETRANSPART_COUNT
            : uploadedChunks.size();
        for (int i = 0; i < retransPartCount; i++) {
            uploadedChunks.remove(uploadedChunks.size() - 1);
        }

        LOGGER.info("uploadedChunks = {}", uploadedChunks);
        return uploadedChunks;
    }

    @Override
    public ResponseEntity cancelUploadVmImage(Integer imageId, String identifier) {
        LOGGER.info("cancel upload vm image file, imageId = {}", imageId);
        VMImage vmImage = vmImageMapper.getVmImage(imageId);
        if (EnumVmImageStatus.UPLOADING_MERGING == vmImage.getStatus()) {
            LOGGER.error("vm image is merging, it cannot be cancelled.");
            return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        LOGGER.info("clean uploaded file.");
        cleanUploadedFile(imageId, identifier);

        LOGGER.info("update image status to upload_cancelled.");
        vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_CANCELLED.toString());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity mergeVmImage(String fileName, String identifier, Integer imageId) {
        LOGGER.info("merge vm image file, imageId = {}, fileName = {}, identifier = {}", imageId, fileName,
            identifier);
        vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOADING_MERGING.toString());

        String rootDir = getUploadVmImageRootDir(imageId);
        String partFilePath = rootDir + identifier;
        File partFileDir = new File(partFilePath);
        if (!partFileDir.exists() || !partFileDir.isDirectory()) {
            LOGGER.error("uploaded part file path not found!");
            cancelOnRemoteFileServer(identifier);
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("uploaded part file not found!");
            cancelOnRemoteFileServer(identifier);
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        File mergedFile = new File(rootDir + File.separator + fileName);
        try (FileOutputStream mergedFileStream = new FileOutputStream(mergedFile, true);) {
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, mergedFileStream);
                partFile.delete();
            }
        } catch (Exception ex) {
            LOGGER.error("merge local file failed: {}", ex.getMessage());
            cancelOnRemoteFileServer(identifier);
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            vmImageMapper.updateVmImageErrorType(imageId, EnumProcessErrorType.OPEN_FAILED.getErrorType());
            cleanWorkDir(mergedFile.getParentFile());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        LOGGER.info("process merged file.");
        UploadFileInfo uploadFileInfo = processMergedFile(mergedFile);
        if (!uploadFileInfo.isSucceeded()) {
            LOGGER.error("process merged file failed!");
            cancelOnRemoteFileServer(identifier);
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            vmImageMapper.updateVmImageErrorType(imageId, uploadFileInfo.getErrorType());
            return ResponseEntity.status(uploadFileInfo.getRespStatusCode()).build();
        }

        LOGGER.info("delete old vm image on remote server.");
        deleteImageFileOnRemote(imageId);

        LOGGER.info("merge on remote file server.");
        String uploadedSystemPath = mergeOnRemoteFileServer(identifier, fileName);
        if (StringUtils.isEmpty(uploadedSystemPath)) {
            LOGGER.error("merge failed on remote file server!");
            vmImageMapper.updateVmImageStatus(imageId, EnumVmImageStatus.UPLOAD_FAILED.toString());
            vmImageMapper
                .updateVmImageErrorType(imageId, EnumProcessErrorType.FILESYSTEM_MERGE_FAILED.getErrorType());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        LOGGER.info("vm image file upload succeed.");
        uploadFileInfo.assign(imageId, FILE_FORMAT_QCOW2.equalsIgnoreCase(uploadFileInfo.getFileFormat())
            ? EnumVmImageStatus.PUBLISHED
            : EnumVmImageStatus.UPLOAD_SUCCEED, uploadedSystemPath);
        vmImageMapper.updateVmImageUploadInfo(uploadFileInfo);
        vmImageMapper.updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIM_WAIT.toString());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<byte[]> downloadVmImage(Integer imageId) {
        Assert.notNull(vmImageMapper.getVmImagesPath(imageId), "vm image path is null");
        try {
            String systemPath = vmImageMapper.getVmImagesPath(imageId);
            String url = systemPath + "?isZip=true";
            byte[] dataStream = HttpClientUtil.downloadSystemImage(url);
            if (dataStream == null) {
                LOGGER.error("download vm image failed!");
                return null;
            }
            LOGGER.info("download vm image succeed!");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            VMImage vmImage = vmImageMapper.getVmImage(imageId);
            String fileName = vmImage.getImageFileName();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            return ResponseEntity.ok().headers(headers).body(dataStream);
        } catch (Exception e) {
            LOGGER.error("download vm image failed!");
            return null;
        }
    }

    @Override
    public Boolean imageSlim(Integer imageId) {
        LOGGER.info("slim vm image status, imageId = {}", imageId);
        VMImage vmImage = vmImageMapper.getVmImage(imageId);
        if (vmImage == null) {
            LOGGER.error("vm image not found, imageId = {}", imageId);
            throw new IllegalRequestException("vm image not found", ResponseConsts.RET_QUERY_DATA_FAIL);
        }

        if (!isAdminUser() && !vmImage.getUserId().equalsIgnoreCase(AccessUserUtil.getUserId())) {
            LOGGER.error("forbidden slim the image");
            throw new UnauthorizedException("forbidden slim the image", ResponseConsts.RET_REQUEST_UNAUTHORIZED);
        }

        LOGGER.info("slim image by filesystem.");
        boolean slimResult = imageSlimByFileServer(imageId);
        if (!slimResult) {
            LOGGER.error("image slim fail.");
            vmImageMapper.updateVmImageSlimStatus(imageId, EnumVmImageSlimStatus.SLIM_FAILED.toString());
            throw new RestfulRequestException("image slim fail.", ResponseConsts.RET_RESTFUL_REQUEST_FAIL);
        }

        LOGGER.info("update image status to SLIMMING.");
        vmImageMapper.updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIMMING.toString());
        new GetVmImageSlimProcessor(imageId).start();
        return true;

    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

    private boolean deleteImageFileOnRemote(Integer imageId) {
        String systemPath = vmImageMapper.getVmImagesPath(imageId);
        if (StringUtils.isEmpty(systemPath)) {
            LOGGER.debug("vm image path is invalid, no need to delete.");
            return true;
        }

        try {
            String url = systemPath.substring(0, systemPath.length() - 16);
            if (!HttpClientUtil.deleteSystemImage(url)) {
                LOGGER.error("delete vm image on remote failed!");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("delete old vm image failed, {}", e.getMessage());
            return false;
        }

        return true;
    }

    private void cleanUploadedFile(Integer imageId, String identifier) {
        LOGGER.info("delete old vm image on remote server.");
        deleteImageFileOnRemote(imageId);

        LOGGER.info("cancel request to remote file server");
        if (!StringUtils.isEmpty(identifier) && !cancelOnRemoteFileServer(identifier)) {
            LOGGER.warn("remote file server cancel failed.");
        }

        LOGGER.info("remove local directory.");
        String rootDir = getUploadVmImageRootDir(imageId);
        cleanWorkDir(new File(rootDir));
    }
    private boolean cancelOnRemoteFileServer(String identifier) {
        return HttpClientUtil.cancelSliceUpload(fileServerAddress, identifier);
    }

    private String mergeOnRemoteFileServer(String identifier, String mergeFileName) {
        try {
            String uploadResult = HttpClientUtil
                .sliceMergeFile(fileServerAddress, identifier, mergeFileName, AccessUserUtil.getUserId());
            if (uploadResult == null) {
                LOGGER.error("merge on remote file server failed.");
                return null;
            }
            Map<String, String> uploadResultModel = gson.fromJson(uploadResult, Map.class);
            return fileServerAddress + String
                .format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, uploadResultModel.get("imageId"));
        } catch (Exception e) {
            LOGGER.error("merge on remote file server failed. {}", e.getMessage());
            return null;
        }
    }

    private void cleanWorkDir(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            LOGGER.error("delete work directory failed.");
        }
    }

    private String getUploadVmImageRootDir(int imageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getTmpPath() + SUBDIR_VMIMAGE + File.separator
            + imageId + File.separator;
    }

    private UploadFileInfo processMergedFile(File mergedFile) {
        try (ZipFile zipFile = new ZipFile(mergedFile)) {
            String fileMd5 = null;
            String fileFormat = null;
            Long fileSize = 0L;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                fileSize = entry.getCompressedSize();
                fileFormat = name.substring(name.lastIndexOf(".") + 1, name.length());
                if (fileFormat.equalsIgnoreCase(FILE_FORMAT_QCOW2) || fileFormat.equalsIgnoreCase(FILE_FORMAT_ISO)) {
                    fileMd5 = FileHashCode.md5HashCode32(zipFile.getInputStream(entry));
                    return new UploadFileInfo(mergedFile.getName(), fileMd5, fileFormat, fileSize);
                }
            }
            LOGGER.error("zipFile format is mistake!");
            return new UploadFileInfo(Response.Status.BAD_REQUEST.getStatusCode(),
                EnumProcessErrorType.FORMAT_MISTAKE.getErrorType());
        } catch (Exception e) {
            LOGGER.error("process merged zip file failed, {}", e.getMessage());
            return new UploadFileInfo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                EnumProcessErrorType.OPEN_FAILED.getErrorType());
        } finally {
            cleanWorkDir(mergedFile.getParentFile());
        }
    }

    private boolean imageSlimByFileServer(Integer imageId) {
        String imagePath = vmImageMapper.getVmImagesPath(imageId);
        if (StringUtils.isEmpty(imagePath)) {
            LOGGER.debug("image path is invalid, no need to delete.");
            return false;
        }
        try {
            String url = imagePath.substring(0, imagePath.length() - 16) + FILE_SLIM_PATH;
            boolean slimResult = HttpClientUtil.imageSlim(url);
            if (!slimResult) {
                LOGGER.error("image slim by file server failed.");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("image slim by file server failed. {}", e.getMessage());
            return false;
        }
        return true;
    }

    public class GetVmImageSlimProcessor extends Thread {

        Integer imageId;

        public GetVmImageSlimProcessor(Integer imageId) {
            this.imageId = imageId;
        }

        @Override
        public void run() {
            Boolean res = getImageFileInfo(imageId);
            if(res) {
                LOGGER.info("slim image success");
            }else {
                LOGGER.info("slim image fail");
            }
        }

        private Boolean getImageFileInfo(int imageId) {
            String imagePath = vmImageMapper.getVmImagesPath(imageId);
            String url = imagePath.substring(0, imagePath.length() - 16);
            long startTime = System.currentTimeMillis();
            FileSystemResponse imageResult;
            while (System.currentTimeMillis() - startTime < MAX_SECONDS * 60) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("sleep fail! {}", e.getMessage());
                }
                String slimResult = HttpClientUtil.getImageSlim(url);
                if (slimResult==null) {
                    vmImageMapper
                        .updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIM_FAILED.toString());
                    return false;
                }
                try {
                    imageResult = new ObjectMapper().readValue(slimResult.getBytes(), FileSystemResponse.class);
                } catch (Exception e) {
                    return false;
                }
                LOGGER.info("image slim result: {}", slimResult);
                int slimStatus = imageResult.getSlimStatus();

                if (slimStatus==2) {
                    vmImageMapper
                        .updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIM_SUCCEED.toString());
                    Long imageSize = Long.parseLong(imageResult.getCheckStatusResponse().getCheckInfo().getImageInfo().getImageSize());
                    String checkSum = imageResult.getCheckStatusResponse().getCheckInfo().getChecksum();
                    vmImageMapper.updateVmImageInfo(imageId, imageSize, checkSum);
                    return true;
                } else if (slimStatus==1) {
                    vmImageMapper
                        .updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIMMING.toString());
                } else {
                    vmImageMapper
                        .updateVmImageSlimStatus(imageId, EnumSystemImageSlimStatus.SLIM_FAILED.toString());
                    return false;
                }
            }
            return false;
        }
    }
}
