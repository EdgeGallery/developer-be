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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.AppPkgStructure;
import org.edgegallery.developer.model.GeneralConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.RuntimeUtil;
import org.edgegallery.developer.util.samplecode.SampleCodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("uploadFileService")
public class UploadFileService {

    public static final String REGEX_START = Pattern.quote("{{");

    public static final String REGEX_END = Pattern.quote("}}");

    public static final Pattern REPLACE_PATTERN = Pattern.compile(REGEX_START + "(.*?)" + REGEX_END);

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileService.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private String sampleCodePath;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private AppReleaseService appReleaseService;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private ProjectMapper projectMapper;

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

    /**
     * getFile.
     *
     * @return
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> getFile(String fileId, String userId, String type) {

        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("can not find file {} in db", fileId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in db."));
        }
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
        if (!file.exists()) {
            LOGGER.error("can not find file {} in repository", fileId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in repository."));
        }
        String fileName = uploadedFile.getFileName();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/octet-stream");
            headers.add("Content-Disposition", "attachment; filename=" + file.getName());
            LOGGER.info("get file success {}", fileId);
            byte[] fileData = getFileByteArray(file, userId, type, fileName);
            return Either.right(ResponseEntity.ok().headers(headers).body(fileData));
        } catch (IOException e) {
            LOGGER.error("Failed to get file stream: {}", e.getMessage());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Failed to get file stream.");
            return Either.left(error);
        }
    }

    /**
     * getApiFile.
     *
     * @return
     */
    public Either<FormatRespDto, UploadedFile> getApiFile(String fileId, String userId) {

        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile != null) {
            File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
            if (!file.exists()) {
                LOGGER.error("can not find file {} in repository", fileId);
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in repository."));
            }
        }
        return Either.right(uploadedFile);
    }

    private byte[] getFileByteArray(File file, String userId, String type, String fileName) throws IOException {
        String fileFormat = fileName.substring(fileName.lastIndexOf("."));
        if (userId == null || !EnumOpenMepType.OPENMEP.name().equals(type)) {
            return FileUtils.readFileToByteArray(file);
        }
        if (fileFormat.equals(".yaml") || fileFormat.equals(".json")) {
            List<MepHost> enabledHosts = hostMapper.getHostsByStatus(EnumHostStatus.NORMAL, "admin", "X86", "K8S");
            if (!enabledHosts.isEmpty()) {
                String host = enabledHosts.get(0).getLcmIp() + ":" + "32119";
                return FileUtils.readFileToString(file, "UTF-8").replace("{HOST}", host)
                    .getBytes(StandardCharsets.UTF_8);
            }
        }

        return FileUtils.readFileToByteArray(file);
    }

    /**
     * uploadFile.
     *
     * @return
     */
    public Either<FormatRespDto, UploadedFile> uploadFile(String userId, MultipartFile uploadFile) {
        LOGGER.info("Begin upload file");
        UploadedFile result = new UploadedFile();
        String fileName = uploadFile.getOriginalFilename();
        if (!FileChecker.isValid(fileName)) {
            LOGGER.error("File Name is invalid.");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "File Name is invalid."));
        }
        String fileId = UUID.randomUUID().toString();
        String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
        String fileRealPath = upLoadDir + fileId;
        File dir = new File(upLoadDir);
        if (!dir.isDirectory()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "make file dir failed"));
            }
        }

        File newFile = new File(fileRealPath);
        try {
            uploadFile.transferTo(newFile);
            result.setFileName(fileName);
            result.setFileId(fileId);
            result.setUserId(userId);
            result.setUploadDate(new Date());
            result.setTemp(true);
            result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
            uploadedFileMapper.saveFile(result);
        } catch (IOException e) {
            LOGGER.error("Failed to save file with IOException. {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Failed to save file."));
        }
        LOGGER.info("upload file success {}", fileName);
        //upload success
        result.setFilePath("");
        return Either.right(result);
    }

    /**
     * getSampleCode.
     *
     * @return
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> downloadSampleCode(List<String> apiFileIds) {
        Either<FormatRespDto, File> res = generateTgz(apiFileIds);
        if (res.isLeft()) {
            return Either.left(res.getLeft());
        }
        File tar = res.getRight();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add("Content-Disposition", "attachment; filename=SampleCode.tgz");
            byte[] fileData = FileUtils.readFileToByteArray(tar);
            LOGGER.info("get sample code file success");
            DeveloperFileUtils.deleteTempFile(tar);
            return Either.right(ResponseEntity.ok().headers(headers).body(fileData));
        } catch (IOException e) {
            LOGGER.error("get sample code file failed : {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get sample code file failed "));
        }

    }

    /**
     * getSampleCodeStru.
     *
     * @return
     */
    public Either<FormatRespDto, AppPkgStructure> getSampleCodeStru(List<String> apiFileIds) {
        Either<FormatRespDto, File> res = generateTgz(apiFileIds);
        if (res.isLeft()) {
            return Either.left(res.getLeft());
        }
        File sampleTgz = res.getRight();
        boolean decompressRes = false;
        String samplePath = "";
        try {
            samplePath = sampleTgz.getCanonicalPath();
            decompressRes = CompressFileUtils.decompress(samplePath, samplePath.substring(0, samplePath.length() - 15));
        } catch (IOException e) {
            LOGGER.error("get sample code dir fail,{}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get sample code dir fail"));
        }

        if (!decompressRes) {
            LOGGER.error("decompress sample code file fail");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "decompress sample code file fail"));
        }
        DeveloperFileUtils.deleteTempFile(sampleTgz);
        // get csar pkg structure
        AppPkgStructure structure;
        try {
            structure = appReleaseService
                .getFiles(samplePath.substring(0, samplePath.length() - 15), new AppPkgStructure());
            sampleCodePath = samplePath.substring(0, samplePath.length() - 15);
        } catch (IOException ex) {
            LOGGER.error("get sample code pkg occur io exception: {}", ex.getMessage());
            String message = "get sample code pkg occur io exception!";
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, message);
            return Either.left(error);
        }
        return Either.right(structure);
    }

    /**
     * getSampleCodeContent.
     *
     * @return
     */
    public Either<FormatRespDto, String> getSampleCodeContent(String fileName) {
        if (StringUtils.isEmpty(sampleCodePath)) {
            LOGGER.error("decompress sample code tgz failed!");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get sample code path fail!"));
        }
        File dir = new File(sampleCodePath);
        List<String> paths = appReleaseService.getFilesPath(dir);
        if (paths == null || paths.isEmpty()) {
            LOGGER.error("can not find any file!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not find any file!");
            return Either.left(error);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = appReleaseService.readFileIntoString(path);
            }
        }
        if (fileContent.equals("")) {
            LOGGER.warn("file has not any content!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "file is null!");
            return Either.left(error);
        }

        if (fileContent.equals("error")) {
            LOGGER.warn("file is not readable!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "file is not readable!");
            return Either.left(error);
        }
        return Either.right(fileContent);
    }

    private Either<FormatRespDto, File> generateTgz(List<String> apiFileIds) {
        File tempDir = DeveloperFileUtils.createTempDir("mec_sample_code");
        // add sample resources code
        File sampleResource = new File(tempDir, InitConfigUtil.getSampleCodeDir());
        try {
            DeveloperFileUtils.deleteAndCreateDir(sampleResource);
        } catch (IOException e) {
            String msg = "create sample code dir failed!";
            LOGGER.error("create sample code dir failed! occur {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, msg));
        }

        for (String apiFileId : apiFileIds) {
            if (!apiFileId.matches(REGEX_UUID)) {
                LOGGER.error("The input is not in UUID format.");
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "The input is not in UUID format."));
            }
        }
        // add sample api file
        List<String> apiJsonList = new ArrayList<>();
        for (String apiFileId : apiFileIds) {
            UploadedFile apifile = uploadedFileMapper.getFileById(apiFileId);
            if (apifile == null) {
                LOGGER.error("can not find file {} in db", apiFileId);
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in db."));
            }
            String fileRealPath = InitConfigUtil.getWorkSpaceBaseDir() + apifile.getFilePath();
            String apiJson;
            try {
                apiJson = DeveloperFileUtils.readFileToString(new File(fileRealPath));
            } catch (IOException e) {
                LOGGER.error("read api file to string exception: {}", e.getMessage());
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "read api file to string exception"));
            }
            if (apifile.getFileName().endsWith(".yaml") || apifile.getFileName().endsWith(".yml")) {
                Yaml yaml = new Yaml(new SafeConstructor());
                try {
                    apiJson = new Gson().toJson(yaml.load(apiJson));
                } catch (DomainException e) {
                    LOGGER.error("Yaml deserialization failed {}", e.getMessage());
                    FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Yaml deserialization failed");
                    return Either.left(error);
                }
            }
            apiJsonList.add(apiJson);
        }

        SampleCodeServer generateCode = new SampleCodeServer();
        File tar = generateCode.analysis(apiJsonList);
        return Either.right(tar);
    }

    /**
     * delete template files. If uploaded files have not been used over 30min, should be deleted.
     */
    public void deleteTempFile() {
        LOGGER.info("Begin delete temp file.");
        Date now = new Date();
        List<String> tempIds = uploadedFileMapper.getAllTempFiles();
        if (tempIds == null) {
            return;
        }
        for (String tempId : tempIds) {
            UploadedFile tempFile = uploadedFileMapper.getFileById(tempId);
            Date uploadDate = tempFile.getUploadDate();
            if ((int) ((now.getTime() - uploadDate.getTime()) / Consts.MINUTE) < Consts.TEMP_FILE_TIMEOUT) {
                continue;
            }

            String realPath = InitConfigUtil.getWorkSpaceBaseDir() + tempFile.getFilePath();
            File temp = new File(realPath);
            if (temp.exists()) {
                DeveloperFileUtils.deleteTempFile(temp);
                uploadedFileMapper.deleteFile(tempId);
                LOGGER.info("Delete temp file {} success.", tempFile.getFileName());
            }
        }
    }

    /**
     * uploadHelmTemplateYaml.
     *
     * @return
     */
    public Either<FormatRespDto, HelmTemplateYamlRespDto> uploadHelmTemplateYaml(MultipartFile helmTemplateYaml,
        String userId, String projectId, String configType) throws IOException {
        String fileName = helmTemplateYaml.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "file name is null!"));
        }
        if (!fileName.endsWith("yaml") && !fileName.endsWith("yml") && !fileName.endsWith("YAML") && !fileName
            .endsWith("YML")) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "file type is not yaml!"));
        }
        String content;
        File tempFile;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
            helmTemplateYaml.transferTo(tempFile);
            content = readFile(tempFile);
        } catch (IOException e) {
            String errorMsg = "Failed to read content of helm template yaml, userId: {}, projectId: {},exception: {}";
            LOGGER.error(errorMsg, userId, projectId, e.getMessage());
            String returnMsg = "Failed to read content of helm template yaml";
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, returnMsg));
        }
        //yamlEmpty
        if (StringUtils.isEmpty(content)) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "yaml file is empty!"));
        }
        //judgmentyamlIs there a configurationnamespace
        if (!content.contains("namespace")) {
            content = addNameSpace(content);
        } else {
            //replacenamespacecontent
            content = replaceContent(content);
        }

        //The image format isname:tag(nameDoes not contain delimiter)
        HelmTemplateYamlRespDto helmTemplateYamlRespDto = new HelmTemplateYamlRespDto();
        String oriName = helmTemplateYaml.getOriginalFilename();
        if (!StringUtils.isEmpty(oriName) && !oriName.endsWith(".yaml")) {
            return Either.right(helmTemplateYamlRespDto);
        }
        String originalContent = content;
        content = content.replaceAll(REPLACE_PATTERN.toString(), "");
        // verify yaml scheme
        String[] multiContent = content.split("---");
        List<Map<String, Object>> mapList = new ArrayList<>();
        try {
            for (String str : multiContent) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                Yaml yaml = new Yaml(new SafeConstructor());
                Map<String, Object> loaded = yaml.load(str);
                mapList.add(loaded);
            }
            helmTemplateYamlRespDto.setFormatSuccess(true);
        } catch (Exception e) {
            LOGGER.error("Failed to validate yaml scheme, userId: {}, projectId: {},exception: {}", userId, projectId,
                e.getMessage());
            helmTemplateYamlRespDto.setFormatSuccess(false);
            helmTemplateYamlRespDto.setMepAgentSuccess(null);
            helmTemplateYamlRespDto.setServiceSuccess(null);
            helmTemplateYamlRespDto.setImageSuccess(null);
            return Either.right(helmTemplateYamlRespDto);
        }
        List<String> requiredItems = Lists.newArrayList("image", "service", "mep-agent");
        // verify service,image,mep-agent
        verifyHelmTemplate(mapList, requiredItems, helmTemplateYamlRespDto);

        if (!requiredItems.isEmpty() && requiredItems.size() >= 2) {
            LOGGER.error("Failed to verify helm template yaml, userId: {}, projectId: {},exception: verify: {} failed",
                userId, projectId, String.join(",", requiredItems));
            return Either.right(helmTemplateYamlRespDto);
        } else if (!requiredItems.isEmpty() && requiredItems.size() == 1 && requiredItems.get(0).equals("mep-agent")) {
            //delete all helm by projectId
            boolean isSaved = saveImage(tempFile, projectId);
            if (!isSaved) {
                LOGGER.error("Failed to save Image");
                return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to save image"));
            }
            List<HelmTemplateYamlPo> list = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
            if (!CollectionUtils.isEmpty(list)) {
                for (HelmTemplateYamlPo po : list) {
                    helmTemplateYamlMapper.deleteYamlByFileId(po.getFileId());
                }
            }
            // create HelmTemplateYamlPo
            HelmTemplateYamlPo helmTemplateYamlPo = new HelmTemplateYamlPo();
            helmTemplateYamlPo.setContent(originalContent);
            String fileId = UUID.randomUUID().toString();
            String filename = helmTemplateYaml.getOriginalFilename();
            helmTemplateYamlPo.setFileId(fileId);
            helmTemplateYamlPo.setFileName(filename);
            helmTemplateYamlPo.setUserId(userId);
            helmTemplateYamlPo.setProjectId(projectId);
            helmTemplateYamlPo.setUploadTimeStamp(System.currentTimeMillis());
            helmTemplateYamlPo.setConfigType(configType);
            int saveResult = helmTemplateYamlMapper.saveYaml(helmTemplateYamlPo);
            if (saveResult <= 0) {
                LOGGER.error("Failed to save helm template yaml, file id : {}", fileId);
                return Either
                    .left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to save helm template yaml"));
            }
            helmTemplateYamlRespDto.setResponse(helmTemplateYamlPo);
            helmTemplateYamlRespDto.setImageSuccess(true);
            helmTemplateYamlRespDto.setServiceSuccess(true);
            helmTemplateYamlRespDto.setMepAgentSuccess(false);

            LOGGER.info("Succeed to save helm template yaml with file id : {}", fileId);
            return Either.right(helmTemplateYamlRespDto);
        }
        return getSuccessResult(helmTemplateYaml, userId, projectId, originalContent, helmTemplateYamlRespDto,
            configType, tempFile);

    }

    private boolean saveImage(File helmYaml, String projectId) {
        //yamlRead aslist
        List<String> list = readFileByLine(helmYaml);
        List<String> podImages = new ArrayList<>();
        //query image and save
        for (String str : list) {
            if (str.contains("image:")) {
                if (str.contains(".Values.imagelocation.domainname")) {
                    String[] images = str.split("\'");
                    podImages.add(images[1].trim());
                } else {
                    String[] images = str.split(":");
                    podImages.add(images[1].trim() + ":" + images[2].trim());
                }
            }

        }
        //verify image info
        LOGGER.warn("podImages {}", podImages);
        boolean result = verifyImage(podImages);
        if (!result) {
            LOGGER.error("the image configuration in the yaml file is incorrect");
            return false;
        }
        List<ProjectImageConfig> listImage = projectImageMapper.getAllImage(projectId);
        if (!CollectionUtils.isEmpty(listImage)) {
            for (ProjectImageConfig po : listImage) {
                projectImageMapper.deleteImage(po.getProjectId());
            }
        }
        ApplicationProject project = projectMapper.getProjectById(projectId);
        ProjectImageConfig config = new ProjectImageConfig();
        config.setId(UUID.randomUUID().toString());
        config.setPodName(project.getName());
        config.setPodContainers(podImages.toString());
        config.setProjectId(projectId);
        int res = projectImageMapper.saveImage(config);
        if (res <= 0) {
            return false;
        }
        return true;
    }

    private static List<String> readFileByLine(File fin) {
        String line;
        List<String> sb = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(fin);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.add(line + "\r\n");
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return sb;
    }

    private boolean verifyImage(List<String> imageList) {
        if (CollectionUtils.isEmpty(imageList)) {
            LOGGER.error("deploy yaml has no any image info");
            return false;
        }
        DockerClient dockerClient = getDockerClient(devRepoEndpoint, devRepoUsername, devRepoPassword);
        for (String image : imageList) {
            LOGGER.info("deploy yaml image: {}", image);
            //judge image in format
            if (!image.contains(":") || image.endsWith(":")) {
                LOGGER.error("image {} must be in xxx:xxx format!", image);
                return false;
            }
            //The image format is{{.Values.imagelocation.domainname}}/{{.Values.imagelocation.project}}/name:tag
            // Or forharborWarehouse Address
            String envStr = "{{.Values.imagelocation.domainname}}/{{.Values.imagelocation.project}}";
            String harborStr = devRepoEndpoint + "/" + devRepoProject;
            if (image.contains(envStr) || image.contains(harborStr)) {
                if (image.contains(envStr)) {
                    image = image.replace(envStr, harborStr);
                }
                try {
                    LOGGER.warn("before pull image {}", image);
                    dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().close();
                    LOGGER.warn("after pull image {}", image);
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("pull image {} from harbor repo failed! {}", image, e.getMessage());
                    return false;
                }
            } else {
                //Mirror in other formats，Pull,tag,push
                pullAndPushImage(image);
            }
        }
        return true;
    }

    private boolean pullAndPushImage(String image) {
        //Mirror information is notharborWarehouse format，Pull(failure，returnfalse)，hitTag，Repush
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(protocol + "://" + devRepoEndpoint + ":" + port).build();
        DockerCmdExecFactory factory = new NettyDockerCmdExecFactory().withConnectTimeout(100000);
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(factory).build();
        try {
            InspectImageResponse imageInfo = dockerClient.inspectImageCmd(image).exec();
            if (imageInfo != null) {
                if (StringUtils.isNotEmpty(imageInfo.getId())) {
                    dockerClient.removeImageCmd(imageInfo.getId()).withForce(true).exec();
                }
                if (StringUtils.isNotEmpty(imageInfo.getContainer())) {
                    ContainerConfig containerConfig = imageInfo.getContainerConfig();
                    LOGGER.info("containerConfig : {} ", containerConfig);
                    if (containerConfig != null && StringUtils.isNotEmpty(containerConfig.getHostName())) {
                        LOGGER.info("containerConfig hostName: {} ", containerConfig.getHostName());
                        dockerClient.stopContainerCmd(containerConfig.getHostName()).exec();
                    }
                }
            }
        } catch (NotFoundException e) {
            LOGGER.warn("not found image : {} ", image);
        }
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().close();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("pull image {} from other public repo  failed! {}", image, e.getMessage());
            return false;
        }
        LOGGER.warn("pull image : {} success", image);
        //According to the image name，Get mirrorid
        String[] imageNames = image.split(":");
        List<Image> lists = dockerClient.listImagesCmd().withImageNameFilter(imageNames[0]).exec();
        String imageId = "";
        if (!CollectionUtils.isEmpty(lists)) {
            for (Image imageLocal : lists) {
                if (imageLocal.getRepoTags() != null && imageLocal.getRepoTags().length > 0) {
                    LOGGER.info(imageLocal.getRepoTags()[0]);
                    String[] imagNames = imageLocal.getRepoTags();
                    if (imageNames != null && imagNames[0].equals(image)) {
                        imageId = imageLocal.getId();
                        LOGGER.info(imageId);
                    }
                }
            }
        }
        LOGGER.warn("image {} imageID: {} ", image, imageId);

        String targetName = "";
        if (!image.contains("/")) {
            targetName = devRepoEndpoint + "/" + devRepoProject + "/" + image;
            dockerClient.tagImageCmd(imageId, targetName, imageNames[1]).withForce().exec();
            LOGGER.warn("image {} re-tag success", image);
        } else {
            String[] targetImageArray = image.split("/");
            if (targetImageArray.length == 2) {
                targetName = devRepoEndpoint + "/" + devRepoProject + "/" + image;
            } else if (targetImageArray.length == 3) {
                targetName = devRepoEndpoint + "/" + devRepoProject + "/" + targetImageArray[2];
            } else {
                targetName = devRepoEndpoint + "/" + devRepoProject + "/" + targetImageArray[3];
            }
            dockerClient.tagImageCmd(imageId, targetName, imageNames[1]).withForce().exec();
            LOGGER.warn("image {} re-tag {} success", image, targetName);
        }
        //push image
        DockerClient pushClient = getDockerClient(devRepoEndpoint, devRepoUsername, devRepoPassword);
        try {
            pushClient.pushImageCmd(targetName).exec(new PushImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("failed to push image {} occur {}", targetName, e.getMessage());
            return false;
        }
        LOGGER.warn("image {} push success", targetName);
        return true;

    }

    private DockerClient getDockerClient(String repo, String userName, String password) {
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerTlsVerify(true)
                .withDockerCertPath("/usr/app/ssl").withRegistryUrl("https://" + repo).withRegistryUsername(userName)
                .withRegistryPassword(password).build();
            LOGGER.warn("docker register url: {}", config.getRegistryUrl());
            return DockerClientBuilder.getInstance(config).build();
        } catch (DockerClientException e) {
            LOGGER.error("get docker instance occur {}", e.getMessage());
            return null;
        }
    }

    private String readFile(File fin) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(fin);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
        } catch (IOException e) {
            LOGGER.error("read file by line occur {}", e.getMessage());
            return null;
        }
        return sb.toString();
    }

    private String replaceContent(String content) {
        String[] multiContent = content.split("\r\n");
        for (int i = 0; i < multiContent.length; i++) {
            if (multiContent[i].contains("namespace")) {
                multiContent[i] = "namespace: '{{ .Values.appconfig.appnamespace }}'";
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String newStr : multiContent) {
            if (newStr.contains("namespace")) {
                sb.append("  " + newStr + "\r\n");
            } else {
                sb.append(newStr + "\r\n");
            }
        }
        return sb.toString();
    }

    private String addNameSpace(String content) {
        String[] multiContent = content.split("\r\n");
        List<String> list = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < multiContent.length; i++) {
            list.add(multiContent[i]);
        }
        String in = getIndexOfSameObject(list);
        String[] indexs = in.split(",");
        for (String index : indexs) {
            nums.add(Integer.parseInt(index));
        }
        for (int i = 0; i < nums.size(); i++) {
            list.add(nums.get(i) + i, "namespace: '{{ .Values.appconfig.appnamespace }}'");
        }
        StringBuilder sb = new StringBuilder();
        for (String newStr : list) {
            if (newStr.contains("namespace")) {
                sb.append("  " + newStr + "\r\n");
            } else {
                sb.append(newStr + "\r\n");
            }
        }
        return sb.toString();
    }

    private String getIndexOfSameObject(List<String> list) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            String old = map.get(key);
            if (old != null) {
                map.put(key, old + "," + (i + 1));
            } else {
                map.put(key, "" + (i + 1));
            }
        }
        Set<Map.Entry<String, String>> sets = map.entrySet();
        String index = "";
        for (Map.Entry<String, String> entry : sets) {
            if (entry.getKey().startsWith("metadata")) {
                index = entry.getValue();
            }
        }
        return index;
    }

    private Either<FormatRespDto, HelmTemplateYamlRespDto> getSuccessResult(MultipartFile helmTemplateYaml,
        String userId, String projectId, String content, HelmTemplateYamlRespDto helmTemplateYamlRespDto,
        String configType, File tempFile) {
        //save image
        boolean isSaved = saveImage(tempFile, projectId);
        if (!isSaved) {
            LOGGER.error("Failed to save image!");
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to save image!"));
        }
        //delete all helm by projectId
        List<HelmTemplateYamlPo> list = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        if (!CollectionUtils.isEmpty(list)) {
            for (HelmTemplateYamlPo po : list) {
                helmTemplateYamlMapper.deleteYamlByFileId(po.getFileId());
            }
        }
        HelmTemplateYamlPo helmTemplateYamlPo = new HelmTemplateYamlPo();
        helmTemplateYamlPo.setContent(content);
        String fileId = UUID.randomUUID().toString();
        String filename = helmTemplateYaml.getOriginalFilename();
        helmTemplateYamlPo.setFileId(fileId);
        helmTemplateYamlPo.setFileName(filename);
        helmTemplateYamlPo.setUserId(userId);
        helmTemplateYamlPo.setProjectId(projectId);
        helmTemplateYamlPo.setUploadTimeStamp(System.currentTimeMillis());
        helmTemplateYamlPo.setConfigType(configType);
        int saveResult = helmTemplateYamlMapper.saveYaml(helmTemplateYamlPo);
        if (saveResult <= 0) {
            LOGGER.error("Failed to save helm template yaml, file id : {}", fileId);
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to save helm template yaml"));
        }
        helmTemplateYamlRespDto.setResponse(helmTemplateYamlPo);
        LOGGER.info("Succeed to save helm template yaml with file id : {}", fileId);
        return Either.right(helmTemplateYamlRespDto);

    }

    private void verifyHelmTemplate(List<Map<String, Object>> mapList, List<String> requiredItems,
        HelmTemplateYamlRespDto helmTemplateYamlRespDto) {
        for (Map<String, Object> stringMap : mapList) {
            for (Map.Entry<String, Object> entry : stringMap.entrySet()) {
                if ("kind".equals(entry.getKey())) {
                    if ("Service".equalsIgnoreCase(stringMap.get(entry.getKey()).toString())) {
                        requiredItems.remove("service");
                        helmTemplateYamlRespDto.setServiceSuccess(true);
                        continue;
                    }
                    if (stringMap.get("spec") != null) {
                        String specContent = stringMap.get("spec").toString();
                        if (specContent.contains("image")) {
                            requiredItems.remove("image");
                            helmTemplateYamlRespDto.setImageSuccess(true);
                        }
                        if (specContent.contains("mep-agent")) {
                            helmTemplateYamlRespDto.setMepAgentSuccess(true);
                            requiredItems.remove("mep-agent");
                        }
                    }
                }
            }
        }
    }

    /**
     * getHelmTemplateYamlList.
     *
     * @return
     */
    public Either<FormatRespDto, List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(String userId,
        String projectId) {
        List<HelmTemplateYamlPo> templateYamlPoList = helmTemplateYamlMapper
            .queryTemplateYamlByProjectId(userId, projectId);
        List<HelmTemplateYamlRespDto> helmTemplateYamlRespDtoList = new ArrayList<>();
        templateYamlPoList.forEach(helmTemplateYamlPo -> {
            HelmTemplateYamlRespDto helmTemplateYamlRespDto = new HelmTemplateYamlRespDto();
            helmTemplateYamlRespDto.setResponse(helmTemplateYamlPo);
            String content = helmTemplateYamlPo.getContent().replaceAll("\r", "");
            content = content.replaceAll(REPLACE_PATTERN.toString(), "");
            // verify yaml scheme
            String[] multiContent = content.split("---");
            List<Map<String, Object>> mapList = new ArrayList<>();
            try {
                for (String str : multiContent) {
                    if (StringUtils.isBlank(str)) {
                        continue;
                    }
                    Yaml yaml = new Yaml(new SafeConstructor());
                    Map<String, Object> loaded = yaml.load(str);
                    mapList.add(loaded);
                }
                helmTemplateYamlRespDto.setFormatSuccess(true);
            } catch (Exception e) {
                LOGGER
                    .error("Failed to validate yaml scheme, userId: {}, projectId: {},exception: {}", userId, projectId,
                        e.getMessage());
                helmTemplateYamlRespDto.setFormatSuccess(false);
                helmTemplateYamlRespDto.setMepAgentSuccess(null);
                helmTemplateYamlRespDto.setServiceSuccess(null);
                helmTemplateYamlRespDto.setImageSuccess(null);
            }
            List<String> requiredItems = Lists.newArrayList("image", "service", "mep-agent");
            // verify service,image,mep-agent
            verifyHelmTemplate(mapList, requiredItems, helmTemplateYamlRespDto);

            if (!requiredItems.isEmpty() && requiredItems.size() >= 2) {
                LOGGER.error(
                    "Failed to verify helm template yaml, userId: {}, projectId: {},exception: verify: {} failed",
                    userId, projectId, String.join(",", requiredItems));
            } else if (!requiredItems.isEmpty() && requiredItems.size() == 1 && requiredItems.get(0)
                .equals("mep-agent")) {
                helmTemplateYamlRespDto.setImageSuccess(true);
                helmTemplateYamlRespDto.setServiceSuccess(true);
                helmTemplateYamlRespDto.setMepAgentSuccess(false);
            }
            helmTemplateYamlRespDtoList.add(helmTemplateYamlRespDto);
        });
        LOGGER.info("Succeed to query helm template yaml with user id : {}, project id : {}", userId, projectId);
        return Either.right(helmTemplateYamlRespDtoList);
    }

    /**
     * deleteHelmTemplateYamlByFileId.
     *
     * @return
     */
    public Either<FormatRespDto, String> deleteHelmTemplateYamlByFileId(String fileId) {
        //delete  pod image
        String projectId = helmTemplateYamlMapper.queryProjectId(fileId);
        if (!org.springframework.util.StringUtils.isEmpty(projectId)) {
            int res = projectImageMapper.deleteImage(projectId);
            if (res <= 0) {
                LOGGER.error("delete image by projectId failed!");
                return Either
                    .left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "delete image by projectId failed!"));
            }
        } else {
            LOGGER.error("query image by fileId failed!");
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "query image by fileId failed!"));
        }
        int deleteResult = helmTemplateYamlMapper.deleteYamlByFileId(fileId);
        if (deleteResult <= 0) {
            LOGGER.error("Failed to delete helm template yaml with file id : {}", fileId);
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to delete helm template yaml"));
        }
        LOGGER.info("Succeed to delete helm template yaml with file id : {}", fileId);
        return Either.right("Delete helm template yaml success");
    }

    /**
     * getgetSdkProject.
     *
     * @return
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> getSdkProject(String fileId, String lan) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("can not find file {} in db", fileId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in db."));
        }
        OpenMepCapabilityDetail openMepCapabilityDetail = openMepCapabilityMapper.getDetailByApiFileId(fileId);
        //generate code
        GeneralConfig config = new GeneralConfig();
        config.setApiPackage("jar");
        config.setArtifactId("org.edgegallery");
        config.setInvokerPackage("edgegallerys");
        config.setModelPackage("edgegallerysdk");
        config.setArtifactVersion(openMepCapabilityDetail.getVersion());
        config.setGroupId("org.edgegallery");
        config.setOutput(InitConfigUtil.getWorkSpaceBaseDir());
        config.setProjectName(openMepCapabilityDetail.getHost());
        config.setInputSpec(uploadedFile.getFilePath());
        String sdkPath = InitConfigUtil.getWorkSpaceBaseDir() + config.getOutput() + openMepCapabilityDetail.getHost();

        try {

            List<String> commandList = RuntimeUtil.buildCommand(lan, config);
            String ret = RuntimeUtil.execCommand(commandList);
            if (ret.endsWith("SUCCESS")) {
                LOGGER.info("codegenSDK {} successful", config.getProjectName());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to build project", e.getMessage());
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to build project"));
        }

        try {
            CompressFileUtils
                .compressToTgzAndDeleteSrc(sdkPath, InitConfigUtil.getWorkSpaceBaseDir() + config.getOutput(),
                    config.getProjectName());
        } catch (IOException e) {
            LOGGER.error("Failed to compress project", e.getMessage());
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to compress project"));
        }

        File tar = new File(sdkPath + ".tgz");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add("Content-Disposition", "attachment; filename=" + openMepCapabilityDetail.getHost() + ".tgz");
            byte[] fileData = FileUtils.readFileToByteArray(tar);
            LOGGER.info("get sample code file success");
            DeveloperFileUtils.deleteTempFile(tar);
            return Either.right(ResponseEntity.ok().headers(headers).body(fileData));
        } catch (IOException e) {
            LOGGER.error("get sample code file failed : {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get sample code file failed "));
        }

    }

}
