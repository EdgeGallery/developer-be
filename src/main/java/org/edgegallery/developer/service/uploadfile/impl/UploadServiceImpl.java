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

package org.edgegallery.developer.service.uploadfile.impl;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.model.GeneralConfig;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.service.AppReleaseService;
import org.edgegallery.developer.service.uploadfile.UploadService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.RuntimeUtil;
import org.edgegallery.developer.util.samplecode.SampleCodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("uploadService")
public class UploadServiceImpl implements UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final List<String> FILE_TYPE_LIST = Arrays.asList("icon", "api", "md");

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Autowired
    private AppReleaseService appReleaseService;

    private String sampleCodePath;

    @Override
    public byte[] getFileStream(UploadedFile uploadedFile, String userId) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
        if (!file.exists()) {
            LOGGER.error("can not find file {} in repository", uploadedFile.getFileId());
            throw new FileFoundFailException("can not find file in repository!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        String fileName = uploadedFile.getFileName();
        try {
            return getFileByteArray(file, userId, fileName);
        } catch (IOException e) {
            LOGGER.error("Failed to get file stream: {}", e.getMessage());
            throw new FileFoundFailException("can not find file in repository!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
    }

    private byte[] getFileByteArray(File file, String userId, String fileName) throws IOException {
        String fileFormat = fileName.substring(fileName.lastIndexOf("."));
        if (userId == null) {
            return FileUtils.readFileToByteArray(file);
        }
        if (fileFormat.equals(".yaml") || fileFormat.equals(".json")) {
            List<MepHost> enabledHosts = hostMapper.getHostsByStatus(EnumHostStatus.NORMAL, "X86", "K8S");
            if (!enabledHosts.isEmpty()) {
                String host = enabledHosts.get(0).getLcmIp() + ":" + "32119";
                return FileUtils.readFileToString(file, StandardCharsets.UTF_8).replace("{HOST}", host)
                    .getBytes(StandardCharsets.UTF_8);
            }
        }
        return FileUtils.readFileToByteArray(file);
    }

    @Override
    public UploadedFile getFile(String fileId, String userId) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile != null) {
            File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath());
            if (!file.exists()) {
                LOGGER.error("can not find file {} in repository", fileId);
                throw new FileFoundFailException("api file not exist!", ResponseConsts.RET_FILE_NOT_FOUND);
            }
        }
        return uploadedFile;
    }

    @Override
    public UploadedFile uploadFile(String userId, String fileType, MultipartFile uploadFile) {
        //check format
        LOGGER.info("Start uploading file");
        String fileName = uploadFile.getOriginalFilename();
        if (!FILE_TYPE_LIST.contains(fileType)) {
            String msg = "fileType is error,must be one of [icon,md,api]";
            LOGGER.error(msg);
            throw new IllegalRequestException(msg, ResponseConsts.RET_REQUEST_FORMAT_ERROR);
        }
        boolean typeRes = FileUtil.checkFileType(fileName, fileType);
        if (!typeRes) {
            LOGGER.error("file suffix is error.");
            throw new IllegalRequestException("file suffix is error.", ResponseConsts.RET_REQUEST_FORMAT_ERROR);
        }
        if (fileType.equals("icon")) {
            boolean sizeRes = FileUtil.checkFileSize(uploadFile.getSize(), 2, "M");
            if (!sizeRes) {
                String errorMsg = "icon file size can not be greater than 2m";
                LOGGER.error(errorMsg);
                throw new IllegalRequestException(errorMsg, ResponseConsts.RET_FILE_FORMAT_ERROR);
            }
        }
        UploadedFile result = saveFileToLocal(uploadFile, userId);
        if (result == null) {
            LOGGER.error("Failed to save icon file!");
            throw new FileOperateException("Failed to save picture file.", ResponseConsts.RET_SAVE_FILE_FAIL);
        }
        return result;
    }

    @Override
    public boolean deleteFile(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            LOGGER.error("fileId is empty!");
            throw new IllegalRequestException("fileId does not exist.", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("the queried Object(UploadedFile) is null!");
            return true;
        }
        String filePath = uploadedFile.getFilePath();
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        if (!file.exists()) {
            LOGGER.warn("the queried file may be deleted or moved!");
            return true;
        }
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            LOGGER.error("delete file occur {}", e.getMessage());
            return false;
        }
        //delete db record
        int ret = uploadedFileMapper.deleteFile(fileId);
        if (ret < 1) {
            LOGGER.error("delete file failed!");
            throw new DataBaseException("delete file failed!", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public byte[] downloadSampleCode(List<String> apiFileIds) {
        File res = generateTgz(apiFileIds);
        if (res == null) {
            throw new FileOperateException("generate samplecode file failed!", ResponseConsts.RET_SAVE_FILE_FAIL);
        }
        try {
            byte[] fileData = FileUtils.readFileToByteArray(res);
            LOGGER.info("get sample code file success");
            DeveloperFileUtils.deleteTempFile(res);
            return fileData;
        } catch (IOException e) {
            LOGGER.error("get sample code file failed : {}", e.getMessage());
            throw new FileOperateException("get samplecode file failed!", ResponseConsts.RET_DOWNLOAD_FILE_FAIL);
        }
    }

    @Override
    public AppPkgStructure getSampleCodeStru(List<String> apiFileIds) {
        File res = generateTgz(apiFileIds);
        if (res == null) {
            throw new FileOperateException("generate samplecode file failed!", ResponseConsts.RET_SAVE_FILE_FAIL);
        }
        boolean decompressRes;
        String samplePath = "";
        try {
            samplePath = res.getCanonicalPath();
            decompressRes = CompressFileUtils.decompress(samplePath, samplePath.substring(0, samplePath.length() - 15));
        } catch (IOException e) {
            LOGGER.error("get sample code dir fail,{}", e.getMessage());
            throw new FileOperateException("get sample code dir fail!", ResponseConsts.RET_DECOPRESS_FILE_FAIL);
        }
        if (!decompressRes) {
            LOGGER.error("decompress sample code file fail");
            throw new FileOperateException("decompress file failed!", ResponseConsts.RET_DECOPRESS_FILE_FAIL);
        }
        DeveloperFileUtils.deleteTempFile(res);
        // get csar pkg structure
        AppPkgStructure structure;
        try {
            structure = appReleaseService
                .getFiles(samplePath.substring(0, samplePath.length() - 15), new AppPkgStructure());
            sampleCodePath = samplePath.substring(0, samplePath.length() - 15);
        } catch (IOException ex) {
            LOGGER.error("get sample code pkg occur io exception: {}", ex.getMessage());
            String message = "get sample code pkg occur io exception!";
            throw new FileOperateException(message, ResponseConsts.RET_FILE_STRUCTURE_FAIL);
        }
        return structure;
    }

    @Override
    public String getSampleCodeContent(String fileName) {
        if (StringUtils.isEmpty(sampleCodePath)) {
            LOGGER.error("decompress sample code tgz failed!");
            throw new FileOperateException("decompress file failed!", ResponseConsts.RET_DECOPRESS_FILE_FAIL);
        }
        File dir = new File(sampleCodePath);
        List<String> paths = FileUtil.getAllFilePath(dir);
        if (paths.isEmpty()) {
            LOGGER.error("can not find any file!");
            throw new FileFoundFailException("can not find any file!", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = FileUtil.readFileContent(path);
            }
        }
        if (fileContent.equals("")) {
            LOGGER.error("file has not any content!");
            throw new IllegalRequestException("file has not any content!", ResponseConsts.RET_FILE_EMPTY);
        }

        if (fileContent.equals("error")) {
            LOGGER.error("file is not readable!");
            throw new IllegalRequestException("file is not readable!", ResponseConsts.RET_FILE_NOT_READABLE);
        }
        return fileContent;
    }

    @Override
    public byte[] getSdkProject(String fileId, String lan, List<Capability> capabilities) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile == null) {
            LOGGER.error("can not find file {} in db", fileId);
            throw new FileFoundFailException("can not find file in db", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        //generate code
        GeneralConfig config = new GeneralConfig();
        config.setApiPackage("jar");
        config.setArtifactId("org.edgegallery");
        config.setInvokerPackage("edgegallerys");
        config.setModelPackage("edgegallerysdk");
        config.setArtifactVersion(capabilities.get(0).getVersion());
        config.setGroupId("org.edgegallery");
        config.setOutput(InitConfigUtil.getWorkSpaceBaseDir());
        config.setProjectName(capabilities.get(0).getHost());
        config.setInputSpec(uploadedFile.getFilePath());
        String sdkPath = InitConfigUtil.getWorkSpaceBaseDir() + config.getOutput() + capabilities.get(0).getHost();

        try {

            List<String> commandList = RuntimeUtil.buildCommand(lan, config);
            String ret = RuntimeUtil.execCommand(commandList);
            if (ret.endsWith("SUCCESS")) {
                LOGGER.info("codegenSDK {} successful", config.getProjectName());
            }
        } catch (Exception e) {
            LOGGER.error("failed to build project {}", e.getMessage());
            throw new DeveloperException("Failed to build project", ResponseConsts.RET_BUILD_SDK_FAIL);
        }

        try {
            CompressFileUtils
                .compressToTgzAndDeleteSrc(sdkPath, InitConfigUtil.getWorkSpaceBaseDir() + config.getOutput(),
                    config.getProjectName());
        } catch (IOException e) {
            LOGGER.error("Failed to compress project {}", e.getMessage());
            throw new FileOperateException("Failed to compress project", ResponseConsts.RET_COPRESS_FILE_FAIL);
        }

        File tar = new File(sdkPath + ".tgz");

        try {
            byte[] fileData = FileUtils.readFileToByteArray(tar);
            LOGGER.info("get sample code file success");
            DeveloperFileUtils.deleteTempFile(tar);
            return fileData;
        } catch (IOException e) {
            LOGGER.error("get sample code file failed : {}", e.getMessage());
            throw new FileOperateException("Failed to get sample code file ", ResponseConsts.RET_DOWNLOAD_FILE_FAIL);
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
            result.setTemp(false);
            result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
            uploadedFileMapper.saveFile(result);
        } catch (IOException e) {
            LOGGER.error("Failed to save file.");
            return null;
        }
        LOGGER.info("upload file success {}", fileName);
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
            throw new EntityNotFoundException("Can not find file", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        // get temp file
        String tempFilePath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + srcId;
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists() || tempFile.isDirectory()) {
            uploadedFileMapper.updateFileStatus(srcId, true);
            LOGGER.error("Can not find file, please upload again.");
            throw new FileFoundFailException("Can not find file", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        // move file
        File desFile = new File(DeveloperFileUtils.getAbsolutePath(applicationId) + file.getFileName());
        try {
            DeveloperFileUtils.moveFile(tempFile, desFile);
            String filePath = BusinessConfigUtil.getWorkspacePath() + applicationId + File.separator + file
                .getFileName();
            uploadedFileMapper.updateFilePath(srcId, filePath);
        } catch (IOException e) {
            LOGGER.error("move icon file failed {}", e.getMessage());
            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new FileOperateException("Move icon file failed.", ResponseConsts.RET_MOVE_FILE_FAIL);
        }
    }

    private File generateTgz(List<String> apiFileIds) {
        File tempDir = DeveloperFileUtils.createTempDir("mec_sample_code");
        // add sample resources code
        File sampleResource = new File(tempDir, InitConfigUtil.getSampleCodeDir());
        try {
            DeveloperFileUtils.deleteAndCreateDir(sampleResource);
        } catch (IOException e) {
            String msg = "create sample code dir failed!";
            LOGGER.error("create sample code dir failed! occur {}", e.getMessage());
            throw new FileOperateException(msg, ResponseConsts.RET_CREATE_FILE_FAIL);
        }

        for (String apiFileId : apiFileIds) {
            if (!apiFileId.matches(REGEX_UUID)) {
                LOGGER.error("The input is not in UUID format.");
                throw new IllegalRequestException("The input is not in UUID format.",
                    ResponseConsts.RET_REQUEST_FORMAT_ERROR);
            }
        }
        // add sample api file
        List<String> apiJsonList = new ArrayList<>();
        for (String apiFileId : apiFileIds) {
            UploadedFile apifile = uploadedFileMapper.getFileById(apiFileId);
            if (apifile == null) {
                LOGGER.error("can not find file {} in db", apiFileId);
                throw new FileFoundFailException("can not find api file.", ResponseConsts.RET_FILE_NOT_FOUND);
            }
            String fileRealPath = InitConfigUtil.getWorkSpaceBaseDir() + apifile.getFilePath();
            String apiJson;
            try {
                apiJson = DeveloperFileUtils.readFileToString(new File(fileRealPath));
            } catch (IOException e) {
                LOGGER.error("read api file to string exception: {}", e.getMessage());
                throw new FileFoundFailException("can not find api file.", ResponseConsts.RET_FILE_NOT_FOUND);
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
                    throw new DeveloperException("Yaml deserialization failed.", ResponseConsts.RET_LOAD_YAML_FAIL);
                }
            }
            apiJsonList.add(apiJson);
        }

        SampleCodeServer generateCode = new SampleCodeServer();
        return generateCode.analysis(apiJsonList);
    }

}
