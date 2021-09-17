package org.edgegallery.developer.service.uploadfile.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.GeneralConfig;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.OpenMepCapability;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.AppReleaseService;
import org.edgegallery.developer.service.uploadfile.UploadService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.RuntimeUtil;
import org.edgegallery.developer.util.UploadFileUtil;
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

@Service("uploadService")
public class UploadServiceImpl implements UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_START = Pattern.quote("{{");

    private static final String REGEX_END = Pattern.quote("}}");

    private static final Pattern REPLACE_PATTERN = Pattern.compile(REGEX_START + "(.*?)" + REGEX_END);

    private static Gson gson = new Gson();

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private AppReleaseService appReleaseService;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    private String sampleCodePath;

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

    @Override
    public Either<FormatRespDto, ResponseEntity<byte[]>> getFile(String fileId, String userId, String type) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("can not find file {} in db", fileId);
            throw new DeveloperException("can not find file in db!", ResponseConsts.UPLOADED_FILE_NOT_EXIST);
        }
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
        if (!file.exists()) {
            LOGGER.error("can not find file {} in repository", fileId);
            throw new DeveloperException("can not find file in repository!", ResponseConsts.UPLOADED_FILE_NOT_EXIST);
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
            throw new DeveloperException("can not find file in repository!", ResponseConsts.GET_UPLOADED_FILE_FAILED);
        }
    }

    private byte[] getFileByteArray(File file, String userId, String type, String fileName) throws IOException {
        String fileFormat = fileName.substring(fileName.lastIndexOf("."));
        if (userId == null || !EnumOpenMepType.OPENMEP.name().equals(type)) {
            return FileUtils.readFileToByteArray(file);
        }
        if (fileFormat.equals(".yaml") || fileFormat.equals(".json")) {
            List<MepHost> enabledHosts = hostMapper.getHostsByStatus(EnumHostStatus.NORMAL, "X86", "K8S");
            if (!enabledHosts.isEmpty()) {
                String host = enabledHosts.get(0).getLcmIp() + ":" + "32119";
                return FileUtils.readFileToString(file, "UTF-8").replace("{HOST}", host)
                    .getBytes(StandardCharsets.UTF_8);
            }
        }

        return FileUtils.readFileToByteArray(file);
    }

    @Override
    public Either<FormatRespDto, UploadedFile> getApiFile(String fileId, String userId) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile != null) {
            File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
            if (!file.exists()) {
                LOGGER.error("can not find file {} in repository", fileId);
                throw new DeveloperException("api file not exist!", ResponseConsts.UPLOADED_FILE_NOT_EXIST);
            }
        }
        return Either.right(uploadedFile);
    }

    @Override
    public Either<FormatRespDto, UploadedFile> uploadFile(String userId, MultipartFile uploadFile) {
        LOGGER.info("Start uploading file");
        UploadedFile result = saveFileToLocal(uploadFile, userId);
        if (result == null) {
            throw new DeveloperException("Failed to save file.!", ResponseConsts.SAVE_UPLOADED_FILE_FAILED);
        }
        return Either.right(result);
    }

    @Override
    public Either<FormatRespDto, UploadedFile> uploadMdFile(String userId, MultipartFile uploadFile) {
        return null;
    }

    @Override
    public Either<FormatRespDto, HelmTemplateYamlRespDto> uploadHelmTemplateYaml(MultipartFile helmTemplateYaml,
        String userId, String projectId, String configType) {
        String fileName = helmTemplateYaml.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "file name is null!"));
        }
        if (!fileName.endsWith("yaml") && !fileName.endsWith("yml") && !fileName.endsWith("YAML") && !fileName
            .endsWith("YML")) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "file type is not yaml!"));
        }
        String content;
        File tempFile;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
            helmTemplateYaml.transferTo(tempFile);
            content = UploadFileUtil.readFile(tempFile);
        } catch (IOException e) {
            String errorMsg = "Failed to read content of helm template yaml, userId: {}, projectId: {},exception: {}";
            LOGGER.error(errorMsg, userId, projectId, e.getMessage());
            String returnMsg = "Failed to read content of helm template yaml";
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, returnMsg));
        }
        //empty yaml
        if (StringUtils.isEmpty(content)) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "yaml file is empty!"));
        }
        //Verify whether there exists a configuration namespace
        if (!content.contains("namespace")) {
            content = UploadFileUtil.addNameSpace(content);
        } else {
            //replace namespace content
            content = UploadFileUtil.replaceContent(content);
        }
        //The image format isname:tag(nameDoes not contain delimiter)
        HelmTemplateYamlRespDto helmTemplateYamlRespDto = new HelmTemplateYamlRespDto();
        String oriName = helmTemplateYaml.getOriginalFilename();
        if (!StringUtils.isEmpty(oriName) && !oriName.endsWith(".yaml")) {
            return Either.right(helmTemplateYamlRespDto);
        }
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
        UploadFileUtil.verifyHelmTemplate(mapList, requiredItems, helmTemplateYamlRespDto);

        if (!requiredItems.isEmpty() && requiredItems.size() >= 2) {
            LOGGER.error("Failed to verify helm template yaml, userId: {}, projectId: {},exception: verify: {} failed",
                userId, projectId, String.join(",", requiredItems));
            return Either.right(helmTemplateYamlRespDto);
        } else if (!requiredItems.isEmpty() && requiredItems.size() == 1 && requiredItems.get(0).equals("mep-agent")) {
            //delete all helm by projectId
            boolean isSaved = saveImage(tempFile, projectId);
            if (!isSaved) {
                LOGGER.error("Failed to save Image");
                return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save image"));
            }

            List<HelmTemplateYamlPo> list = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
            if (!CollectionUtils.isEmpty(list)) {
                for (HelmTemplateYamlPo po : list) {
                    helmTemplateYamlMapper.deleteYamlByFileId(po.getFileId());
                }
            }
            // create HelmTemplateYamlPo
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
                return Either.left(
                    new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save helm template yaml"));
            }
            helmTemplateYamlRespDto.setResponse(helmTemplateYamlPo);
            helmTemplateYamlRespDto.setImageSuccess(true);
            helmTemplateYamlRespDto.setServiceSuccess(true);
            helmTemplateYamlRespDto.setMepAgentSuccess(false);

            LOGGER.info("Succeed to save helm template yaml with file id : {}", fileId);
            return Either.right(helmTemplateYamlRespDto);
        }
        return getSuccessResult(helmTemplateYaml, userId, projectId, content, helmTemplateYamlRespDto, configType,
            tempFile);
    }

    private Either<FormatRespDto, HelmTemplateYamlRespDto> getSuccessResult(MultipartFile helmTemplateYaml,
        String userId, String projectId, String content, HelmTemplateYamlRespDto helmTemplateYamlRespDto,
        String configType, File tempFile) {
        //save image
        boolean isSaved = saveImage(tempFile, projectId);
        if (!isSaved) {
            LOGGER.error("Failed to save image!");
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save image!"));
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
            return Either
                .left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save helm template yaml"));
        }
        helmTemplateYamlRespDto.setResponse(helmTemplateYamlPo);
        LOGGER.info("Succeed to save helm template yaml with file id : {}", fileId);
        return Either.right(helmTemplateYamlRespDto);

    }

    @Override
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
            UploadFileUtil.verifyHelmTemplate(mapList, requiredItems, helmTemplateYamlRespDto);

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

    @Override
    public Either<FormatRespDto, String> deleteHelmTemplateYamlByFileId(String fileId) {
        //delete  pod image
        String projectId = helmTemplateYamlMapper.queryProjectId(fileId);
        if (!org.springframework.util.StringUtils.isEmpty(projectId)) {
            int res = projectImageMapper.deleteImage(projectId);
            if (res <= 0) {
                LOGGER.error("delete image by projectId failed!");
                return Either.left(
                    new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "delete image by projectId failed!"));
            }
        } else {
            LOGGER.error("query image by fileId failed!");
            return Either
                .left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "query image by fileId failed!"));
        }
        int deleteResult = helmTemplateYamlMapper.deleteYamlByFileId(fileId);
        if (deleteResult <= 0) {
            LOGGER.error("Failed to delete helm template yaml with file id : {}", fileId);
            return Either
                .left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete helm template yaml"));
        }
        LOGGER.info("Succeed to delete helm template yaml with file id : {}", fileId);
        return Either.right("Delete helm template yaml success");
    }

    @Override
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
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "get sample code file failed "));
        }
    }

    @Override
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
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "get sample code dir fail"));
        }

        if (!decompressRes) {
            LOGGER.error("decompress sample code file fail");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "decompress sample code file fail"));
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
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, message);
            return Either.left(error);
        }
        return Either.right(structure);
    }

    @Override
    public Either<FormatRespDto, String> getSampleCodeContent(String fileName) {
        if (StringUtils.isEmpty(sampleCodePath)) {
            LOGGER.error("decompress sample code tgz failed!");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "get sample code path fail!"));
        }
        File dir = new File(sampleCodePath);
        List<String> paths = FileUtil.getAllFilePath(dir);
        if (paths == null || paths.isEmpty()) {
            LOGGER.error("can not find any file!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any file!");
            return Either.left(error);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = FileUtil.readFileContent(path);
            }
        }
        if (fileContent.equals("")) {
            LOGGER.warn("file has not any content!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is null!");
            return Either.left(error);
        }

        if (fileContent.equals("error")) {
            LOGGER.warn("file is not readable!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is not readable!");
            return Either.left(error);
        }
        return Either.right(fileContent);
    }

    @Override
    public Either<FormatRespDto, ResponseEntity<byte[]>> getSdkProject(String fileId, String lan) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("can not find file {} in db", fileId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find file in db."));
        }
        OpenMepCapability openMepCapabilityDetail = openMepCapabilityMapper.getDetailByApiFileId(fileId);
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
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to build project"));
        }

        try {
            CompressFileUtils
                .compressToTgzAndDeleteSrc(sdkPath, InitConfigUtil.getWorkSpaceBaseDir() + config.getOutput(),
                    config.getProjectName());
        } catch (IOException e) {
            LOGGER.error("Failed to compress project", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "Failed to compress project"));
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
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "get sample code file failed "));
        }
    }

    /**
     * save Config File.
     *
     * @param uploadFile config file
     * @param userId userid
     * @return
     */
    @Override
    public UploadedFile saveFileToLocal(MultipartFile uploadFile, String userId) {
        UploadedFile result = new UploadedFile();
        String fileName = uploadFile.getOriginalFilename();
        String fileId = UUID.randomUUID().toString();
        String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
        String fileRealPath = upLoadDir + fileId;
        File dir = new File(upLoadDir);

        if (!dir.isDirectory()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                LOGGER.error("make file dir failed");
                return null;
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
            LOGGER.error("Failed to save file.");
            return null;
        }
        LOGGER.info("upload file success {}", fileName);
        //upload success
        result.setFilePath("");
        return result;
    }

    /**
     * moveFileToWorkSpaceById.
     */
    public void moveFileToWorkSpaceById(String srcId, String applicationId) {
        uploadedFileMapper.updateFileStatus(srcId, false);
        // to confirm, whether the status is updated
        UploadedFile file = uploadedFileMapper.getFileById(srcId);
        if (file == null || file.isTemp()) {
            uploadedFileMapper.updateFileStatus(srcId, true);
            LOGGER.error("Can not find file, please upload again.");
            throw new DeveloperException("Can not find file", ResponseConsts.UPLOADED_FILE_NOT_EXIST);
        }
        // get temp file
        String tempFilePath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + srcId;
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists() || tempFile.isDirectory()) {
            uploadedFileMapper.updateFileStatus(srcId, true);
            LOGGER.error("Can not find file, please upload again.");
            throw new DeveloperException("Can not find file", ResponseConsts.UPLOADED_FILE_NOT_EXIST);
        }
        // move file
        File desFile = new File(DeveloperFileUtils.getAbsolutePath(applicationId) + srcId);
        try {
            DeveloperFileUtils.moveFile(tempFile, desFile);
            String filePath = BusinessConfigUtil.getWorkspacePath() + applicationId + File.separator + srcId;
            uploadedFileMapper.updateFilePath(srcId, filePath);
        } catch (IOException e) {
            LOGGER.error("move icon file failed {}", e.getMessage());
            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new DeveloperException("Move icon file failed.", ResponseConsts.MOVE_UPLOADED_FILE_FAILED);
        }
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
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, msg));
        }

        for (String apiFileId : apiFileIds) {
            if (!apiFileId.matches(REGEX_UUID)) {
                LOGGER.error("The input is not in UUID format.");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "The input is not in UUID format."));
            }
        }
        // add sample api file
        List<String> apiJsonList = new ArrayList<>();
        for (String apiFileId : apiFileIds) {
            UploadedFile apifile = uploadedFileMapper.getFileById(apiFileId);
            if (apifile == null) {
                LOGGER.error("can not find file {} in db", apiFileId);
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find file in db."));
            }
            String fileRealPath = InitConfigUtil.getWorkSpaceBaseDir() + apifile.getFilePath();
            String apiJson;
            try {
                apiJson = DeveloperFileUtils.readFileToString(new File(fileRealPath));
            } catch (IOException e) {
                LOGGER.error("read api file to string exception: {}", e.getMessage());
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "read api file to string exception"));
            }
            if (StringUtils.isEmpty(apiJson)) {
                continue;
            }
            if (apifile.getFileName().endsWith(".yaml") || apifile.getFileName().endsWith(".yml")) {
                Yaml yaml = new Yaml(new SafeConstructor());
                try {
                    apiJson = new Gson().toJson(yaml.load(apiJson));
                } catch (Exception e) {
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

    private boolean saveImage(File helmYaml, String projectId) {
        //yamlRead aslist
        List<String> list = UploadFileUtil.readFileByLine(helmYaml);
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
        boolean result = UploadFileUtil.isExist(podImages);
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
}
