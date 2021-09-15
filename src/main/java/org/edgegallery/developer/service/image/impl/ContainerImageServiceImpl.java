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

package org.edgegallery.developer.service.image.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.image.ContainersImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;
import org.edgegallery.developer.model.containerimage.EnumContainerImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.image.ContainerImageService;
import org.edgegallery.developer.util.ContainerImageUtil;
import org.edgegallery.developer.util.ListUtil;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("containerImageService")
public class ContainerImageServiceImpl implements ContainerImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageServiceImpl.class);

    @Autowired
    private ContainersImageMapper containerImageMapper;

    @Value("${upload.tempPath}")
    private String filePathTemp;

    @Value("${imagelocation.domainname:}")
    private String devRepoEndpoint;

    @Value("${imagelocation.username:}")
    private String devRepoUsername;

    @Value("${imagelocation.password:}")
    private String devRepoPassword;

    @Value("${imagelocation.project:}")
    private String devRepoProject;

    @Value("${imagelocation.port:}")
    private String port;

    @Value("${imagelocation.protocol:}")
    private String protocol;

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

    /**
     * uploadContainerImage.
     *
     * @param request http request
     * @param chunk file chunk
     * @param imageId harbor imageId
     * @return
     */
    @Override
    public ResponseEntity uploadContainerImage(HttpServletRequest request, Chunk chunk, String imageId) {

        try {
            LOGGER.info("upload harbor image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
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

            LOGGER.info("save file to local directory.");
            String rootDir = ContainerImageUtil.getUploadSysImageRootDir(imageId);
            File uploadRootDir = new File(rootDir);
            if (!uploadRootDir.exists()) {
                boolean isMk = uploadRootDir.mkdirs();
                if (!isMk) {
                    String mkErr = "create temporary upload path failed";
                    LOGGER.error(mkErr);
                    throw new DeveloperException(mkErr, ResponseConsts.RET_TEMPORARY_PATH_FAILED);
                }
            }

            File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            String errMsg = "upload container image file exception.";
            LOGGER.error(errMsg);
            throw new DeveloperException(errMsg, ResponseConsts.RET_UPLOAD_CONTAINER_IMAGE_FAILED);
        }
    }

    /**
     * mergeContainerImage.
     *
     * @param fileName fileName
     * @param guid part file
     * @param imageId imageId
     * @return
     */
    @Override
    public ResponseEntity mergeContainerImage(String fileName, String guid, String imageId) {
        try {
            LOGGER.info("merge harbor image file, harborImage = {}, fileName = {}, guid = {}", imageId, fileName, guid);
            String rootDir = ContainerImageUtil.getUploadSysImageRootDir(imageId);
            String partFilePath = rootDir + guid;
            File partFileDir = new File(partFilePath);
            if (!partFileDir.exists() || !partFileDir.isDirectory()) {
                LOGGER.error("uploaded part file path not found!");
                throw new DeveloperException("uploaded part file path not found",
                    ResponseConsts.RET_FILE_PATH_NOT_FOUND);
            }

            File[] partFiles = partFileDir.listFiles();
            if (partFiles == null || partFiles.length == 0) {
                LOGGER.error("uploaded part file not found!");
                throw new DeveloperException("uploaded part file not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            File mergedFile = new File(rootDir + File.separator + fileName);
            FileOutputStream mergedFileStream = new FileOutputStream(mergedFile, true);
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, mergedFileStream);
                partFile.delete();
            }
            mergedFileStream.close();
            //create repo by current user id
            String userName = AccessUserUtil.getUser().getUserName();
            String projectName = userName.replaceAll(Consts.PATTERN, "").toLowerCase();
            // judge user private harbor repo is exist
            boolean isExist = ContainerImageUtil.isExist(projectName);
            if (!isExist && !SystemImageUtil.isAdminUser()) {
                boolean createRes = ContainerImageUtil.createHarborRepo(projectName);
                if (!createRes) {
                    String errMsg = "create harbor repo failed!";
                    LOGGER.error(errMsg);
                    throw new DeveloperException(errMsg, ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
                }
            }
            //push image to created repo by current user id
            if (!pushImageToRepo(mergedFile, rootDir, projectName, imageId, fileName)) {
                String errMsg = "push image to repo failed!";
                LOGGER.error(errMsg);
                throw new DeveloperException(errMsg, ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
            }
            File uploadPath = new File(rootDir);
            FileUtils.cleanDirectory(uploadPath);
            LOGGER.info("harbor image file upload succeed.");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            String errorMsg = "process merged file occur exception!";
            LOGGER.error("process merged file exception! {}", e.getMessage());
            throw new DeveloperException(errorMsg, ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
        }
    }

    /**
     * createContainerImage.
     *
     * @param containerImage containerImage
     * @return
     */
    @Override
    public Either<FormatRespDto, ContainerImage> createContainerImage(ContainerImage containerImage) {
        String imageName = containerImage.getImageName();
        String imageVersion = containerImage.getImageVersion();
        String userId = containerImage.getUserId();
        String userName = containerImage.getUserName();
        if (StringUtils.isEmpty(imageName) || StringUtils.isEmpty(imageVersion) || StringUtils.isEmpty(userId)
            || StringUtils.isEmpty(userName)) {
            String errorMsg
                = "The required parameter is empty. pls check imageName or imageVersion or userId or userName";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_CREATE_CONTAINER_IMAGE_CHECK_PARAM_FAILED);
        }
        //keep imageName imageVersion unique
        List<ContainerImage> imageList = containerImageMapper.getAllImage();
        if (!CollectionUtils.isEmpty(imageList)) {
            for (ContainerImage image : imageList) {
                if (imageName.equals(image.getImageName()) && imageVersion.equals(image.getImageVersion()) && userName
                    .equals(image.getUserName())) {
                    String errorMsg = "exist the same imageName";
                    LOGGER.error(errorMsg);
                    throw new DeveloperException(errorMsg, ResponseConsts.RET_EXIST_SAME_NAME_AND_VERSION);
                }
            }
        }
        containerImage.setUploadTime(new Date());
        containerImage.setCreateTime(new Date());
        containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
        int retCode = containerImageMapper.createContainerImage(containerImage);
        if (retCode < 1) {
            String errorMsg = "Create ContainerImage failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_CREATE_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("create ContainerImage success");
        ContainerImage queryImage = containerImageMapper.getContainerImage(containerImage.getImageId());
        return Either.right(queryImage);
    }

    /**
     * getAllImage.
     *
     * @param containerImageReq request body
     * @return
     */
    @Override
    public Page<ContainerImage> getAllImage(ContainerImageReq containerImageReq) {
        PageHelper.offsetPage(containerImageReq.getOffset(), containerImageReq.getLimit());
        String createTimeBegin = containerImageReq.getCreateTimeBegin();
        String createTimeEnd = containerImageReq.getCreateTimeEnd();
        if (!StringUtils.isBlank(createTimeBegin)) {
            containerImageReq.setCreateTimeBegin(createTimeBegin + " 00:00:00");
        }
        if (!StringUtils.isBlank(createTimeEnd)) {
            containerImageReq.setCreateTimeEnd(createTimeEnd + " 23:59:59");
        }
        PageInfo pageInfo;
        if (SystemImageUtil.isAdminUser()) {
            pageInfo = new PageInfo<>(containerImageMapper.getAllImageByAdminAuth(containerImageReq));
        } else {
            pageInfo = new PageInfo<>(containerImageMapper.getAllImageByOrdinaryAuth(containerImageReq));
        }
        LOGGER.info("Get all container image success.");
        return new Page<ContainerImage>(pageInfo.getList(), containerImageReq.getLimit(), containerImageReq.getOffset(),
            pageInfo.getTotal());
    }

    /**
     * updateContainerImage.
     *
     * @param imageId imageId
     * @param containerImage containerImage
     * @return
     */
    @Override
    public Either<FormatRespDto, ContainerImage> updateContainerImage(String imageId, ContainerImage containerImage) {
        String loginUserId = AccessUserUtil.getUser().getUserId();
        ContainerImage oldImage = containerImageMapper.getContainerImage(imageId);
        String oldUserId = oldImage.getUserId();
        if (StringUtils.isNotEmpty(oldUserId) && !loginUserId.equals(oldImage.getUserId())) {
            String errorMsg = "Cannot modify data created by others";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_IMAGE_AUTH_CHECK_FAILED);
        }
        String type = containerImage.getImageType();
        int retCode = 0;
        if (StringUtils.isNotEmpty(oldUserId) && StringUtils.isNotEmpty(type)) {
            retCode = containerImageMapper.updateContainerImageType(imageId, oldUserId, type);
        }
        if (retCode < 1) {
            String errorMsg = "update ContainerImage type failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("update ContainerImage type success");
        ContainerImage queryImage = containerImageMapper.getContainerImage(imageId);
        return Either.right(queryImage);
    }

    /**
     * delete image.
     *
     * @param imageId imageId
     * @return
     */
    @Override
    public Either<FormatRespDto, Boolean> deleteContainerImage(String imageId) {
        String loginUserId = AccessUserUtil.getUser().getUserId();
        ContainerImage oldImage = containerImageMapper.getContainerImage(imageId);
        int retCode;
        boolean isDel;
        if (!SystemImageUtil.isAdminUser() && !loginUserId.equals(oldImage.getUserId())) {
            String errorMsg = "Cannot delete data created by others";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_IMAGE_AUTH_CHECK_FAILED);
        }
        isDel = ContainerImageUtil.deleteImage(oldImage.getImagePath(), oldImage.getUserName());
        retCode = containerImageMapper.deleteContainerImageById(imageId);
        LOGGER.warn("isDel {}", isDel);
        LOGGER.warn("retcode {}", retCode);
        if (!isDel || retCode < 1) {
            String errorMsg = "delete ContainerImage failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_DEL_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("delete ContainerImage success");
        return Either.right(true);
    }

    /**
     * download harbor image.
     *
     * @param imageId imageId
     * @return
     */
    @Override
    public ResponseEntity<InputStreamResource> downloadHarborImage(String imageId) {
        if (StringUtils.isEmpty(imageId)) {
            LOGGER.error("imageId is null");
            throw new DeveloperException("imageId is null", ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        ContainerImage containerImage = containerImageMapper.getContainerImage(imageId);
        if (containerImage == null) {
            LOGGER.error("imageId is incorrect");
            throw new DeveloperException("imageId is incorrect", ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        String image = containerImage.getImagePath();
        String fileName = containerImage.getFileName();
        if (StringUtils.isEmpty(image) || StringUtils.isEmpty(fileName)) {
            String msg = "image or fileName is empty";
            LOGGER.error(msg);
            throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(protocol + "://" + devRepoEndpoint + ":" + port).build();
            DockerCmdExecFactory factory = new NettyDockerCmdExecFactory().withConnectTimeout(100000);
            DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(factory)
                .build();
            //pull image
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().close();
            String[] images = image.trim().split(":");
            //save image
            SaveImageCmd saveImage = dockerClient.saveImageCmd(images[0]).withTag(images[1]);
            InputStream input = saveImage.exec();
            if (input == null) {
                String msg = "save image  failed!";
                LOGGER.error(msg);
                throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .body(new InputStreamResource(input));
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            String msg = "download Harbor image occur exception!";
            LOGGER.error("download Harbor image failed! {}", e.getMessage());
            throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
    }

    /**
     * cancel upload.
     *
     * @param imageId imageId
     * @return
     */
    @Override
    public ResponseEntity cancelUploadHarborImage(String imageId) {
        String rootDir = ContainerImageUtil.getUploadSysImageRootDir(imageId);
        SystemImageUtil.cleanWorkDir(new File(rootDir));
        return ResponseEntity.ok().build();
    }

    /**
     * synchronizeHarborImage.
     *
     * @return
     */
    @Override
    public ResponseEntity synchronizeHarborImage() {
        LOGGER.info("begin synchronize image...");
        // get imagePath list from db
        List<ContainerImage> containerImages = containerImageMapper.getAllImageByAdmin();
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(containerImages)) {
            for (ContainerImage containerImage : containerImages) {
                String image = containerImage.getImagePath();
                if (StringUtils.isNotEmpty(image)) {
                    list.add(image.substring(image.lastIndexOf("/") + 1).trim());
                }
            }
        }
        LOGGER.warn("list: {}", list);
        // get Harbor image list
        List<String> harborList = ContainerImageUtil.getHarborImageList();
        if (CollectionUtils.isEmpty(harborList)) {
            LOGGER.warn("harbor repo no images!");
            return ResponseEntity.ok("harbor repo no images!");
        }
        LOGGER.warn("harborList: {}", harborList);
        List<String> imageList = new ArrayList<>();
        for (String harbor : harborList) {
            imageList.add(harbor.substring(harbor.indexOf("/") + 1, harbor.indexOf("+")).trim());
        }
        LOGGER.warn("imageList: {}", imageList);
        LOGGER.warn("bijiao: {}", ListUtil.isEquals(list, imageList));
        LOGGER.warn("bijiao2: {}", list.containsAll(imageList));
        List<String> compareList = new ArrayList<>();
        if (ListUtil.isEquals(list, imageList) || list.containsAll(imageList)) {
            LOGGER.warn("no need synchronize!");
            return ResponseEntity.ok("already the latest image list!");
        } else {
            imageList.removeAll(list);
            for (int i = 0; i < imageList.size(); i++) {
                for (int j = 0; j < harborList.size(); j++) {
                    String harbor = harborList.get(j)
                        .substring(harborList.get(j).indexOf("/") + 1, harborList.get(j).indexOf("+")).trim();
                    if (imageList.get(i).equals(harbor)) {
                        compareList.add(harborList.get(j));
                    }
                }
            }
        }
        LOGGER.warn("compareList: {}", compareList);
        if (!CollectionUtils.isEmpty(compareList)) {
            for (String harborImage : compareList) {
                ContainerImage containerImage = new ContainerImage();
                containerImage.setImageId(UUID.randomUUID().toString());
                String imageName = harborImage.substring(harborImage.indexOf("/") + 1, harborImage.indexOf(":"));
                containerImage.setImageName(imageName);
                containerImage
                    .setImageVersion(harborImage.substring(harborImage.indexOf(":") + 1, harborImage.indexOf("+")));
                containerImage.setUserId(AccessUserUtil.getUser().getUserId());
                containerImage.setUserName(AccessUserUtil.getUser().getUserName());
                String pushTime = harborImage.substring(harborImage.indexOf("+") + 1);
                containerImage.setUploadTime(new Date(Instant.parse(pushTime).toEpochMilli()));
                containerImage.setCreateTime(new Date());
                containerImage.setImageType("private");
                containerImage.setImagePath(devRepoEndpoint + "/" + harborImage.substring(0, harborImage.indexOf("+")));
                containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
                containerImage.setFileName(imageName + ".tar");
                int res = containerImageMapper.createContainerImage(containerImage);
                if (res < 1) {
                    LOGGER.error("create container image failed!");
                    throw new DeveloperException("create container image failed",
                        ResponseConsts.RET_CREATE_CONTAINER_IMAGE_FAILED);
                }
            }
            LOGGER.info("end synchronize image...");
        }

        return ResponseEntity.ok("synchronized successfully!");
    }

    private boolean pushImageToRepo(File imageFile, String rootDir, String projectName, String inputImageId,
        String fileName) throws IOException {
        DockerClient dockerClient = ContainerImageUtil.getDockerClient();
        try (InputStream inputStream = new FileInputStream(imageFile)) {
            //import image pkg
            dockerClient.loadImageCmd(inputStream).exec();
        } catch (FileNotFoundException e) {
            LOGGER.error("can not find image file,{}", e.getMessage());
            return false;
        }

        //Unzip the image package，Find out manifest.json middle RepoTags
        String repoTags = ContainerImageUtil.deCompressAndGetRePoTags(rootDir, imageFile);
        if (repoTags.equals("")) {
            return false;
        }
        //get image id
        String imageId = ContainerImageUtil.getImageIdFromRepoTags(repoTags, dockerClient);
        if (imageId.equals("")) {
            return false;
        }
        //push
        boolean ret = ContainerImageUtil.retagAndPush(dockerClient, imageId, projectName, repoTags);
        if (!ret) {
            return false;
        }
        //create container image
        boolean retContainer = createContainerImage(repoTags, inputImageId, fileName, projectName);
        if (!retContainer) {
            return false;
        }
        return true;
    }

    private boolean createContainerImage(String repoTags, String inputImageId, String fileName, String projectName) {
        String uploadImgPath = "";
        String[] images = repoTags.split(":");
        String imageName = images[0];
        String imageVersion = images[1];
        if (SystemImageUtil.isAdminUser()) {
            uploadImgPath = new StringBuilder(devRepoEndpoint).append("/").append(devRepoProject).append("/")
                .append(imageName).append(":").append(imageVersion).toString();
        } else {
            uploadImgPath = new StringBuilder(devRepoEndpoint).append("/").append(projectName).append("/")
                .append(imageName).append(":").append(imageVersion).toString();
        }
        ContainerImage containerImage = new ContainerImage();
        containerImage.setImageId(inputImageId);
        containerImage.setImageType("private");
        containerImage.setImageName(imageName.trim());
        containerImage.setImageVersion(imageVersion.trim());
        containerImage.setImagePath(uploadImgPath);
        containerImage.setUserId(AccessUserUtil.getUser().getUserId());
        containerImage.setUserName(AccessUserUtil.getUser().getUserName());
        containerImage.setFileName(fileName);
        Either<FormatRespDto, ContainerImage> either = createContainerImage(containerImage);
        if (either.isLeft()) {
            LOGGER.error("create harbor image db record failed!");
            return false;
        }
        return true;
    }



}
