package org.edgegallery.developer.service;

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.system.EnumProcessErrorType;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.model.system.UploadFileInfo;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.FileHashCode;
import org.edgegallery.developer.util.HttpClientUtil;
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

@Service("systemImageMgmtService")
public class SystemImageMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    private static final String SUBDIR_SYSIMAGE = "SystemImage";

    private static final String FILE_FORMAT_QCOW2 = "qcow2";

    private static final String FILE_FORMAT_ISO = "iso";

    @Value("${upload.tempPath}")
    private String tempUploadPath;

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
        try {
            LOGGER.info("Query SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                mepGetSystemImageReq.setUserId(userId);
            }
            MepSystemQueryCtrl queryCtrl = mepGetSystemImageReq.getQueryCtrl();
            if (queryCtrl.getSortBy() == null || queryCtrl.getSortBy().equalsIgnoreCase("createTime")) {
                queryCtrl.setSortBy("create_time");
            } else if (queryCtrl.getSortBy().equalsIgnoreCase("userName")) {
                queryCtrl.setSortBy("user_name");
            }
            if (queryCtrl.getSortOrder() == null) {
                queryCtrl.setSortOrder("DESC");
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
            LOGGER.error("Query SystemImages failed {}",e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not query SystemImages."));
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
                LOGGER.error("Create SystemImage failed");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create a SystemImage."));
            }
            vmImage.setUserId(userId);
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), null, userId) > 0) {
                LOGGER.error("SystemName can not duplicate.");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "SystemName can not duplicate."));
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
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create a SystemImage."));
        } catch (Exception e) {
            LOGGER.error("Create SystemImages failed {}", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create SystemImages."));
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
            if (!isAdminUser()) {
                vmImage.setUserId(userId);
            }
            VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
            if (StringUtils.isAnyBlank(vmImage.getSystemName(), vmSystemImage.getUserId())) {
                LOGGER.error("Update SystemImage failed");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update a SystemImage."));
            }
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), systemId, vmSystemImage.getUserId())
                > 0) {
                LOGGER.error("SystemName can not duplicate.");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "SystemName can not duplicate."));
            }
            vmImage.setSystemId(systemId);

            int ret = systemImageMapper.updateSystemImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Update SystemImage success systemId = {}, userId = {}", systemId, userId);
                return Either.right(true);
            }
            LOGGER.error("Update SystemImage failed ");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update a SystemImage."));
        } catch (Exception e) {
            LOGGER.error("Update SystemImages failed {}", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update SystemImages."));
        }
    }

    /**
     * publishSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> publishSystemImage(Integer systemId) throws Exception {
        LOGGER.info("Publish SystemImage start");
        int ret = systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.PUBLISHED.toString());
        if (ret > 0) {
            LOGGER.info("Publish SystemImage {} success ", systemId);
            return Either.right(true);
        }
        LOGGER.error("Publish SystemImage failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not publish a SystemImage."));
    }

    /**
     * reset image status.
     *
     * @param systemId system image id
     * @return
     */
    public Either<FormatRespDto, Boolean> resetImageStatus(Integer systemId) throws Exception {
        LOGGER.info("Reset SystemImage status, systemId = {}", systemId);
        VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
        if (vmSystemImage == null) {
            LOGGER.error("SystemImage not found, systemId = {}", systemId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "SystemImage not found."));
        }

        if (!isAdminUser() && !vmSystemImage.getUserId().equalsIgnoreCase(AccessUserUtil.getUserId())) {
            LOGGER.error("forbidden reset the image");
            return Either.left(new FormatRespDto(Response.Status.FORBIDDEN, "Forbidden reset the image."));
        }

        LOGGER.info("clean uploaded file.");
        cleanUploadedFile(systemId, vmSystemImage.getFileIdentifier());

        LOGGER.info("update image status to upload_wait.");
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_WAIT.toString());
        return Either.right(true);
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteSystemImage(Integer systemId) throws Exception {
        LOGGER.info("Delete SystemImage start");
        VmSystem vmImage = new VmSystem();
        String userId = AccessUserUtil.getUser().getUserId();
        if (!isAdminUser()) {
            vmImage.setUserId(userId);
        }
        vmImage.setSystemId(systemId);

        LOGGER.info("delete system image on remote server.");
        if (!deleteImageFileOnRemote(systemId)) {
            LOGGER.error("delete system image on remote server failed.");
            FormatRespDto error = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                "delete SystemImage failed.");
            return Either.left(error);
        }

        LOGGER.info("delete system image record in database.");
        int res = systemImageMapper.deleteSystemImage(vmImage);
        if (res < 1) {
            LOGGER.error("Delete SystemImage {} failed", userId);
            FormatRespDto error = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                "delete SystemImage failed.");
            return Either.left(error);
        }
        LOGGER.info("Delete SystemImage {} success", userId);
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
    public ResponseEntity uploadSystemImage(HttpServletRequest request, Chunk chunk, Integer systemId)
        throws IOException {
        LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
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

        LOGGER.info("update system image status to uploading and file identifer.");
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING.toString());
        systemImageMapper.updateSystemImageIdentifier(systemId, chunk.getIdentifier());

        LOGGER.info("save file to local directory.");
        String rootDir = getUploadSysImageRootDir(systemId);
        File uploadRootDir = new File(rootDir);
        if (!uploadRootDir.exists()) {
            boolean isMk = uploadRootDir.mkdirs();
            if (!isMk) {
                LOGGER.error("create temporary upload path failed");
                systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
                return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
            }
        }

        File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
        InputStream inputStream = file.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, outFile);

        LOGGER.info("upload to remote file server.");
        if (!HttpClientUtil.sliceUploadFile(fileServerAddress, chunk, outFile.getAbsolutePath())) {
            LOGGER.error("upload to remote file server failed.");
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
            systemImageMapper.updateSystemImageErrorType(systemId, EnumProcessErrorType.FILESYSTEM_UPLOAD_FAILED.getErrorType());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    public List<Integer> checkUploadedChunks(Integer systemId, String identifier) {
        LOGGER.info("check uploaded chunks, systemId = {}, identifier = {}", systemId, identifier);
        String rootDir = getUploadSysImageRootDir(systemId);
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
        for (int i = 1; i <= partFiles.length - Consts.UPLOAD_CONCURRENT_COUNT; i++) {
            uploadedChunks.add(i);
        }

        LOGGER.info("uploadedChunks = {}", uploadedChunks);
        return uploadedChunks;
    }

    /**
     * cancel upload system image.
     *
     * @param systemId System Image ID
     * @param identifier File Identifier
     * @return Resposne
     */
    public ResponseEntity cancelUploadSystemImage(Integer systemId, String identifier) {
        LOGGER.info("cancel upload system image file, systemId = {}", systemId);
        VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
        if (EnumSystemImageStatus.UPLOADING_MERGING == vmSystemImage.getStatus()) {
            LOGGER.error("system image is merging, it cannot be cancelled.");
            return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }

        LOGGER.info("clean uploaded file.");
        cleanUploadedFile(systemId, identifier);

        LOGGER.info("update image status to upload_cancelled.");
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_CANCELLED.toString());
        return ResponseEntity.ok().build();
    }

    private void cleanUploadedFile(Integer systemId, String identifier) {
        LOGGER.info("delete old system image on remote server.");
        deleteImageFileOnRemote(systemId);

        LOGGER.info("cancel request to remote file server");
        if (!StringUtils.isEmpty(identifier) && !cancelOnRemoteFileServer(identifier)) {
            LOGGER.warn("remote file server cancel failed.");
        }

        LOGGER.info("remove local directory.");
        String rootDir = getUploadSysImageRootDir(systemId);
        cleanWorkDir(new File(rootDir));
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
    public ResponseEntity mergeSystemImage(String fileName, String identifier, Integer systemId) throws IOException {
        LOGGER.info("merge system image file, systemId = {}, fileName = {}, identifier = {}", systemId, fileName,
            identifier);
        systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING_MERGING.toString());

        String rootDir = getUploadSysImageRootDir(systemId);
        String partFilePath = rootDir + identifier;
        File partFileDir = new File(partFilePath);
        if (!partFileDir.exists() || !partFileDir.isDirectory()) {
            LOGGER.error("uploaded part file path not found!");
            cancelOnRemoteFileServer(identifier);
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("uploaded part file not found!");
            cancelOnRemoteFileServer(identifier);
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
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
        UploadFileInfo uploadFileInfo = processMergedFile(mergedFile);
        if (!uploadFileInfo.isSucceeded()) {
            LOGGER.error("process merged file failed!");
            cancelOnRemoteFileServer(identifier);
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
            systemImageMapper.updateSystemImageErrorType(systemId, uploadFileInfo.getErrorType());
            return ResponseEntity.status(uploadFileInfo.getRespStatusCode()).build();
        }

        LOGGER.info("delete old system image on remote server.");
        deleteImageFileOnRemote(systemId);

        LOGGER.info("merge on remote file server.");
        String uploadedSystemPath = mergeOnRemoteFileServer(identifier, fileName);
        if (StringUtils.isEmpty(uploadedSystemPath)) {
            LOGGER.error("merge failed on remote file server!");
            systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED.toString());
            systemImageMapper
                .updateSystemImageErrorType(systemId, EnumProcessErrorType.FILESYSTEM_MERGE_FAILED.getErrorType());
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        LOGGER.info("system image file upload succeed.");
        uploadFileInfo.assign(systemId, FILE_FORMAT_QCOW2.equalsIgnoreCase(uploadFileInfo.getFileFormat())
            ? EnumSystemImageStatus.PUBLISHED
            : EnumSystemImageStatus.UPLOAD_SUCCEED, uploadedSystemPath);
        systemImageMapper.updateSystemImageUploadInfo(uploadFileInfo);
        return ResponseEntity.ok().build();
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

            Gson gson = new Gson();
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

    private String getUploadSysImageRootDir(int systemId) {
        return tempUploadPath + File.separator + SUBDIR_SYSIMAGE + File.separator + systemId + File.separator;
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
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
                LOGGER.error("download SystemImage failed!");
                return null;
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
            return null;
        }
    }

    private UploadFileInfo processMergedFile(File mergedFile) {
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
                    return new UploadFileInfo(mergedFile.getName(), fileMd5, fileFormat);
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
}
