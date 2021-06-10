package org.edgegallery.developer.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.HttpClientUtil;
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

@Service("systemImageMgmtService")
public class SystemImageMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    private static final String SUBDIR_SYSIMAGE = "SystemImage";

    @Value("${upload.tempPath}")
    private String tempUploadPath;

    @Value("${fileserver.address}")
    private String fileServerAddress;

    @Autowired
    private SystemImageMapper systemImageMapper;

    /**
     * getSystemImage.
     *
     * @param mepGetSystemImageReq
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
            LOGGER.error("Create SystemImages failed");
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
            if (StringUtils.isAnyBlank(vmImage.getSystemName(), systemImageMapper.getVMImage(systemId).getUserId())) {
                LOGGER.error("Update SystemImage failed");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update a SystemImage."));
            }
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), systemId, systemImageMapper.getVMImage(systemId).getUserId()) > 0) {
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
            LOGGER.error("Update SystemImages failed");
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
        String userId = AccessUserUtil.getUser().getUserId();
        VmSystem vmImage = new VmSystem();
        vmImage.setSystemId(systemId);
        vmImage.setStatus(EnumSystemImageStatus.PUBLISHED);
        int ret = systemImageMapper.publishSystemImage(vmImage);
        if (ret > 0) {
            LOGGER.info("Publish SystemImage {} success ", userId);
            return Either.right(true);
        }
        LOGGER.error("Publish SystemImage failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not publish a SystemImage."));
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
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        if (systemPath != null) {
            try {
                String url = systemPath.substring(0, systemPath.length() - 16);
                if (!HttpClientUtil.deleteSystemImage(url)) {
                    FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete SystemImage failed.");
                    return Either.left(error);
                }
            } catch (Exception e) {
                FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete SystemImage failed.");
                return Either.left(error);
            }
        }
        int res = systemImageMapper.deleteSystemImage(vmImage);
        if (res < 1) {
            LOGGER.error("Delete SystemImage {} failed", userId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete SystemImage failed.");
            return Either.left(error);
        }
        LOGGER.info("Delete SystemImage {} success", userId);
        return Either.right(true);
    }

    /**
     * update system image status.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> updateSystemImageStatus(Integer systemId, EnumSystemImageStatus status, String systemPath) {
        LOGGER.info("update system image status, systemId = {}, status = {}", systemId, status);
        VmSystem vmImage = new VmSystem();
        vmImage.setSystemId(systemId);
        vmImage.setStatus(status);
        if (EnumSystemImageStatus.UPLOAD_SUCCEED.equals(status)) {
            vmImage.setSystemPath(systemPath);
        }

        int ret = systemImageMapper.updateSystemImageStatus(vmImage);
        if (ret <= 0) {
            LOGGER.error("update system image failed, systemId = {}", systemId);
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                "update system image failed"));
        }

        LOGGER.info("update system image success.");
        return Either.right(true);
    }

    /**
     * upload system image.
     *
     * @param request HTTP Servlet Request
     * @param chunk File Chunk
     * @param systemId System Image ID
     * @return Resposne
     * @throws IOException
     */
    public ResponseEntity uploadSystemImage(HttpServletRequest request,
        Chunk chunk, Integer systemId) throws IOException {
        LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}",
            chunk.getFilename(), chunk.getIdentifier(), chunk.getChunkNumber());
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            LOGGER.error("upload request is invalid.");
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("update status.");
        updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOADING, "");

        MultipartFile file = chunk.getFile();
        if (file == null) {
            LOGGER.error("can not find any needed file");
            updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED, "");
            return ResponseEntity.badRequest().build();
        }

        File tmpUploadDir = new File(getUploadSysImageRootDir(systemId));
        if (!tmpUploadDir.exists()) {
            boolean isMk = tmpUploadDir.mkdirs();
            if (!isMk) {
                LOGGER.error("create temporary upload path failed");
                updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED, "");
                return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
            }
        }

        Integer chunkNumber = chunk.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }
        File outFile = new File(getUploadSysImageRootDir(systemId)
            + chunk.getIdentifier(), chunkNumber + ".part");
        InputStream inputStream = file.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, outFile);
        return ResponseEntity.ok().build();
    }

    /**
     * merge system image.
     *
     * @param fileName Merged File Name
     * @param identifier File Identifier
     * @param systemId System Image ID
     * @return Resposne
     * @throws IOException
     */
    public ResponseEntity mergeSystemImage(String fileName, String identifier, Integer systemId) throws IOException {
        LOGGER.info("merge system image file, systemId = {}, fileName = {}, identifier = {}",
                systemId, fileName, identifier);
        String partFilePath = getUploadSysImageRootDir(systemId) + identifier;
        File partFileDir = new File(partFilePath);
        if (!partFileDir.exists() || !partFileDir.isDirectory()) {
            LOGGER.error("uploaded part file path not found!");
            updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED, "");
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("uploaded part file not found!");
            updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED, "");
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        File mergedFile = new File(getUploadSysImageRootDir(systemId) + File.separator + fileName);
        FileOutputStream destTempfos = new FileOutputStream(mergedFile, true);
        for (File partFile : partFiles) {
            FileUtils.copyFile(partFile, destTempfos);
        }
        destTempfos.close();
        FileUtils.deleteDirectory(partFileDir);
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        if (systemPath != null) {
            try {
                String url = systemPath.substring(0, systemPath.length() - 16);
                if (!HttpClientUtil.deleteSystemImage(url)) {
                    LOGGER.error("delete SystemImage failed!");
                    return ResponseEntity.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
                }
            } catch (Exception e) {
                LOGGER.error("delete SystemImage failed!");
                return ResponseEntity.status(Response.Status.EXPECTATION_FAILED.getStatusCode()).build();
            }
        }
        String uploadedSystemPath = pushSystemImage(mergedFile);
        if (StringUtils.isEmpty(uploadedSystemPath)) {
            LOGGER.error("push system image file failed!");
            updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_FAILED, "");
            return ResponseEntity.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        updateSystemImageStatus(systemId, EnumSystemImageStatus.UPLOAD_SUCCEED, uploadedSystemPath);
        return ResponseEntity.ok().build();
    }

    private String pushSystemImage(File systemImgFile) {
        try {
            String uploadResult = HttpClientUtil
                .uploadSystemImage(fileServerAddress, systemImgFile.getPath(), AccessUserUtil.getUserId());
            if (uploadResult == null) {
                LOGGER.error("upload system image file failed.");
                return null;
            }

            try {
                Gson gson = new Gson();
                Map<String, String> uploadResultModel = gson.fromJson(uploadResult, Map.class);
                return fileServerAddress + String.format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, uploadResultModel.get("imageId"));
            } catch (JsonSyntaxException e) {
                LOGGER.error("upload system image file failed.");
                return null;
            }
        } finally {
            LOGGER.info("delete system image file.");
            if (!systemImgFile.delete()) {
                LOGGER.error("delete system image failed.");
            }
        }
    }

    private String getUploadSysImageRootDir(int systemId) {
        return tempUploadPath + File.separator + SUBDIR_SYSIMAGE + File.separator + systemId + File.separator;
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

    public ResponseEntity<byte[]> downloadSystemImage(Integer systemId) {
        Assert.notNull(systemImageMapper.getSystemImagesPath(systemId), "systemPath is null");
        try {
            String systemPath = systemImageMapper.getSystemImagesPath(systemId);
            String url = systemPath;
            byte[] dataStream = HttpClientUtil.downloadSystemImage(url);
            if (dataStream == null) {
                LOGGER.error("download SystemImage failed!");
                return null;
            }
            LOGGER.info("download SystemImage succeed!");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            VmSystem vmSystem = systemImageMapper.getVMImage(systemId);
            String systemName = vmSystem.getSystemName();
            headers.add("Content-Disposition", "attachment; filename=" + systemName + ".zip");
            return ResponseEntity.ok().headers(headers).body(dataStream);
        } catch (Exception e) {
            LOGGER.error("download SystemImage failed!");
            return null;
        }
    }
}
