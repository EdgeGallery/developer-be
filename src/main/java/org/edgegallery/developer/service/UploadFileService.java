/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.mapper.ApiEmulatorMapper;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.GeneralConfig;
import org.edgegallery.developer.model.workspace.ApiEmulator;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

@Service("uploadFileService")
public class UploadFileService {

    public static final String REGEX_START = Pattern.quote("{{");

    public static final String REGEX_END = Pattern.quote("}}");

    public static final Pattern REPLACE_PATTERN = Pattern.compile(REGEX_START + "(.*?)" + REGEX_END);

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileService.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ApiEmulatorMapper apiEmulatorMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

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

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/octet-stream");
            headers.add("Content-Disposition", "attachment; filename=" + file.getName());
            LOGGER.info("get file success {}", fileId);
            byte[] fileData = getFileByteArray(file, userId, type);
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

    private byte[] getFileByteArray(File file, String userId, String type) throws IOException {
        if (userId == null || !EnumOpenMepType.OPENMEP.name().equals(type)) {
            return FileUtils.readFileToByteArray(file);
        }
        ApiEmulator apiEmulator = apiEmulatorMapper.getEmulatorByUserId(userId);
        if (apiEmulator != null) {
            MepHost mepHost = hostMapper.getHost(apiEmulator.getHostId());
            String host = mepHost.getIp() + ":" + apiEmulator.getPort();
            return FileUtils.readFileToString(file, "UTF-8").replace("{HOST}", host).getBytes(StandardCharsets.UTF_8);
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
        return Either.right(result);
    }

    /**
     * getSampleCode.
     *
     * @return
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> downloadSampleCode(List<String> apiFileIds)
        throws IOException {

        File tempDir = DeveloperFileUtils.createTempDir("mec_sample_code");

        // add sample resources code
        File sampleResource = new File(tempDir, InitConfigUtil.getSampleCodeDir());
        DeveloperFileUtils.deleteAndCreateDir(sampleResource);

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
                Yaml yaml = new Yaml();
                apiJson = new Gson().toJson(yaml.load(apiJson));
            }
            apiJsonList.add(apiJson);
        }

        SampleCodeServer generateCode = new SampleCodeServer();
        File tar = generateCode.analysis(apiJsonList);

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
        String userId, String projectId) {
        String content;
        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
            helmTemplateYaml.transferTo(tempFile);
            content = FileUtils.readFileToString(tempFile, Consts.FILE_ENCODING);
        } catch (IOException e) {
            LOGGER
                .error("Failed to read content of helm template yaml, userId: {}, projectId: {},exception: {}", userId,
                    projectId, e.getMessage());
            return Either
                .left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to read content of helm template yaml"));
        }
        HelmTemplateYamlRespDto helmTemplateYamlRespDto = new HelmTemplateYamlRespDto();
        if (!Objects.requireNonNull(helmTemplateYaml.getOriginalFilename()).endsWith(".yaml")) {
            return Either.right(helmTemplateYamlRespDto);
        }
        // replace {{(.*?)}}
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
                Yaml yaml = new Yaml();
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
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = getSuccessResult(helmTemplateYaml, userId, projectId,
            originalContent, helmTemplateYamlRespDto);
        return either;
    }

    private Either<FormatRespDto, HelmTemplateYamlRespDto> getSuccessResult(MultipartFile helmTemplateYaml,
        String userId, String projectId, String content, HelmTemplateYamlRespDto helmTemplateYamlRespDto) {
        HelmTemplateYamlPo helmTemplateYamlPo = new HelmTemplateYamlPo();
        helmTemplateYamlPo.setContent(content);
        String fileId = UUID.randomUUID().toString();
        String filename = helmTemplateYaml.getOriginalFilename();
        helmTemplateYamlPo.setFileId(fileId);
        helmTemplateYamlPo.setFileName(filename);
        helmTemplateYamlPo.setUserId(userId);
        helmTemplateYamlPo.setProjectId(projectId);
        helmTemplateYamlPo.setUploadTimeStamp(System.currentTimeMillis());
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
            for (String key : stringMap.keySet()) {
                if ("kind".equals(key)) {
                    if ("Service".equalsIgnoreCase(stringMap.get(key).toString())) {
                        requiredItems.remove("service");
                        helmTemplateYamlRespDto.setServiceSuccess(true);
                        continue;
                    }
                    if (stringMap.get("spec") != null) {
                        String specContent = stringMap.get("spec").toString();
                        if (specContent.contains("image")){
                            requiredItems.remove("image");
                            helmTemplateYamlRespDto.setImageSuccess(true);
                        }
                        if (specContent.contains("mep-agent")){
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
            // replace {{(.*?)}}
            String content = helmTemplateYamlPo.getContent().replaceAll("\r","");
            content = content.replaceAll(REPLACE_PATTERN.toString(), "");

            // verify yaml scheme
            String[] multiContent = content.split("---");
            List<Map<String, Object>> mapList = new ArrayList<>();
            try {
                for (String str : multiContent) {
                    if (StringUtils.isBlank(str)) {
                        continue;
                    }
                    Yaml yaml = new Yaml();
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
            }
            List<String> requiredItems = Lists.newArrayList("image", "service", "mep-agent");
            // verify service,image,mep-agent
            verifyHelmTemplate(mapList, requiredItems, helmTemplateYamlRespDto);

            if (!requiredItems.isEmpty() && requiredItems.size() >= 2) {
                LOGGER.error("Failed to verify helm template yaml, userId: {}, projectId: {},exception: verify: {} failed",
                    userId, projectId, String.join(",", requiredItems));
            } else if (!requiredItems.isEmpty() && requiredItems.size() == 1 && requiredItems.get(0).equals("mep-agent")) {
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
