package org.edgegallery.developer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.ContainerImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.EnumContainerImageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("imageServiceV2")
public class ImageServiceV2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceV2.class);

    private static final String SUBDIR_CONIMAGE = "ContainerImage";

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

    @Autowired
    private ContainerImageMapper containerImageMapper;

    /**
     * uploadHarborImage.
     *
     * @param request http request
     * @param chunk file chunk
     * @param imageId harbor imageId
     * @return
     */
    public ResponseEntity uploadHarborImage(HttpServletRequest request, Chunk chunk, String imageId) {
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

            LOGGER.info("update system image status.");
            int res = containerImageMapper
                .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOADING.toString());
            if (res < 1) {
                String errorMsg = "update image status failed.";
                LOGGER.error(errorMsg);
                throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_CONTAINER_IMAGE_STATUS_FAILED);
            }
            LOGGER.info("save file to local directory.");
            String rootDir = getUploadSysImageRootDir(imageId);
            File uploadRootDir = new File(rootDir);
            if (!uploadRootDir.exists()) {
                boolean isMk = uploadRootDir.mkdirs();
                if (!isMk) {
                    String mkErr = "create temporary upload path failed";
                    LOGGER.error(mkErr);
                    containerImageMapper
                        .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOAD_FAILED.toString());
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
     * mergeHarborImage.
     *
     * @param fileName file name
     * @param guid file guid
     * @param imageId harbor imageId
     * @return
     */
    public ResponseEntity mergeHarborImage(String fileName, String guid, String imageId) throws IOException {
        try {
            LOGGER.info("merge harbor image file, systemId = {}, fileName = {}, guid = {}", imageId, fileName, guid);
            containerImageMapper
                .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOADING_MERGING.toString());
            String rootDir = getUploadSysImageRootDir(imageId);
            String partFilePath = rootDir + guid;
            File partFileDir = new File(partFilePath);
            if (!partFileDir.exists() || !partFileDir.isDirectory()) {
                LOGGER.error("uploaded part file path not found!");
                containerImageMapper
                    .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOAD_FAILED.toString());
                throw new DeveloperException("uploaded part file path not found",
                    ResponseConsts.RET_FILE_PATH_NOT_FOUND);
            }

            File[] partFiles = partFileDir.listFiles();
            if (partFiles == null || partFiles.length == 0) {
                LOGGER.error("uploaded part file not found!");
                containerImageMapper
                    .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOAD_FAILED.toString());
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
            //push image to repo
            if (!pushImageToRepo(mergedFile, rootDir)) {
                return ResponseEntity.badRequest().build();
            }
            ContainerImage containerImage = containerImageMapper.getContainerImage(imageId);
            containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
            containerImage.setUploadTime(new Date());
            containerImage.setFileName(mergedFile.getName());
            int res = containerImageMapper.updateContainerImageByAdmin(containerImage);
            if (res < 1) {
                String mergeFail = "update image status success failed!";
                LOGGER.error(mergeFail);
                containerImageMapper
                    .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOAD_FAILED.toString());
                throw new DeveloperException(mergeFail, ResponseConsts.RET_MERGE_CONTAINER_IMAGE_FAILED);
            }
            // delete all file in "rootdir"
            File uploadPath = new File(rootDir);
            FileUtils.cleanDirectory(uploadPath);
            LOGGER.info("harbor image file upload succeed.");

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            LOGGER.error("process merged file exception!");
            throw new DeveloperException("process merged file exception",
                ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
        }
    }

    private boolean pushImageToRepo(File imageFile, String rootDir) throws IOException {
        DockerClient dockerClient = getDockerClient(devRepoEndpoint, devRepoUsername, devRepoPassword);
        try (InputStream inputStream = new FileInputStream(imageFile)) {
            //import image pkg
            dockerClient.loadImageCmd(inputStream).exec();
        } catch (FileNotFoundException e) {
            LOGGER.error("can not find image file,{}", e.getMessage());
            return false;
        }

        //Unzip the image package，Find outmanifest.jsonmiddleRepoTags
        File file = new File(rootDir);
        boolean res = deCompress(imageFile.getCanonicalPath(), file);
        String repoTags = "";
        if (res) {
            //Readmanifest.jsonContent
            File manFile = new File(rootDir + "manifest.json");
            String fileContent = FileUtils.readFileToString(manFile, "UTF-8");
            String[] st = fileContent.split(",");
            for (String repoTag : st) {
                if (repoTag.contains("RepoTags")) {
                    String[] repo = repoTag.split(":\\[");
                    repoTags = repo[1].substring(1, repo[1].length() - 2);
                }
            }
        }
        LOGGER.debug("repoTags: {} ", repoTags);
        String[] names = repoTags.split(":");
        //Judge the compressed packagemanifest.jsoninRepoTagsAnd the value ofloadAre the incoming mirror images equal
        LOGGER.debug(names[0]);
        List<Image> lists = dockerClient.listImagesCmd().withImageNameFilter(names[0]).exec();
        LOGGER.debug("lists is empty ?{},lists size {},number 0 {}", CollectionUtils.isEmpty(lists), lists.size(),
            lists.get(0));
        String imageId = "";
        if (!CollectionUtils.isEmpty(lists) && !StringUtils.isEmpty(repoTags)) {
            for (Image image : lists) {
                LOGGER.debug(image.getRepoTags()[0]);
                String[] images = image.getRepoTags();
                if (images[0].equals(repoTags)) {
                    imageId = image.getId();
                    LOGGER.debug(imageId);
                }
            }
        }
        LOGGER.debug("imageID: {} ", imageId);
        String uploadImgName = new StringBuilder(devRepoEndpoint).append("/").append(devRepoProject).append("/")
            .append(names[0]).toString();
        //Mirror tagging，Repush
        String[] repos = repoTags.split(":");
        if (repos.length > 1 && !imageId.equals("")) {
            //tag image
            dockerClient.tagImageCmd(imageId, uploadImgName, repos[1]).withForce().exec();
            LOGGER.debug("Upload tagged docker image: {}", uploadImgName);
            // set image path
            int result = containerImageMapper.updateContainerImagePath(imageId, uploadImgName);
            if (result < 1) {
                LOGGER.error("failed to update image {} path", imageId);
                return false;
            }
            //push image
            try {
                dockerClient.pushImageCmd(uploadImgName).start().awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("failed to push image {}", e.getMessage());
                return false;
            }
        }

        if (!res || imageId.equals("")) {
            LOGGER.error("decompress tar failed!");
            return false;
        }

        if (repoTags.equals("")) {
            LOGGER.error("get RepoTags in manifest file failed!");
            return false;
        }
        if (imageId.equals("")) {
            LOGGER.error("get image id failed!");
            return false;
        }

        return true;
    }

    private DockerClient getDockerClient(String repo, String userName, String password) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerTlsVerify(true)
            .withDockerCertPath("/usr/app/ssl").withRegistryUrl("https://" + repo).withRegistryUsername(userName)
            .withRegistryPassword(password).build();
        LOGGER.warn("docker register url: {}", config.getRegistryUrl());
        return DockerClientBuilder.getInstance(config).build();
    }

    private static boolean deCompress(String tarFile, File destFile) {
        TarArchiveInputStream tis = null;
        try (FileInputStream fis = new FileInputStream(tarFile)) {

            if (tarFile.contains(".tar")) {
                tis = new TarArchiveInputStream(new BufferedInputStream(fis));
            } else {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
                tis = new TarArchiveInputStream(gzipInputStream);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = tis.getNextTarEntry()) != null) {
                if (tarEntry.isDirectory()) {
                    continue;
                } else {
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    LOGGER.info("deCompressing... {}", outputFile.getName());
                    boolean result = outputFile.getParentFile().mkdirs();
                    LOGGER.debug("create directory result {}", result);
                    IOUtils.copy(tis, new FileOutputStream(outputFile));
                }
            }
        } catch (IOException ex) {
            LOGGER.error("failed to decompress, IO exception  {} ", ex.getMessage());
            return false;
        } finally {
            if (tis != null) {
                try {
                    tis.close();
                } catch (IOException ex) {
                    LOGGER.error("failed to close tar input stream {} ", ex.getMessage());
                }
            }
        }
        return true;
    }

    private String getUploadSysImageRootDir(String imageId) {
        return filePathTemp + File.separator + SUBDIR_CONIMAGE + File.separator + imageId + File.separator;
    }
}
