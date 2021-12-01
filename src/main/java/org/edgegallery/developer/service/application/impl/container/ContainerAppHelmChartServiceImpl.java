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

package org.edgegallery.developer.service.application.impl.container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.HarborException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.application.container.ContainerAppImageInfoMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.application.container.ModifyFileContentDto;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.application.container.ContainerAppOperationService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.LoadK8sYamlHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("containerAppHelmChartService")
public class ContainerAppHelmChartServiceImpl implements ContainerAppHelmChartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppHelmChartServiceImpl.class);

    @Autowired
    private ContainerAppImageInfoMapper containerAppImageInfoMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private HelmChartMapper helmChartMapper;

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @Autowired
    private AppConfigurationService appConfigurationService;

    @Autowired
    private ContainerAppOperationService containerAppOperationService;

    @Autowired
    private ImageConfig imageConfig;

    public HelmChart uploadHelmChartFile(String applicationId, String... filePaths) {
        if (filePaths.length < 1) {
            LOGGER.error("The input file is empty.");
            return null;
        }
        if (!verifyFileType(filePaths)) {
            LOGGER.error("Failed to verify the input files.");
            return null;
        }
        // use the first fileName to be the dir name and package name.
        File firstFile = new File(filePaths[0]);
        try {
            IContainerFileHandler containerFileHandler = LoadContainerFileFactory.createLoader(firstFile.getName());
            assert containerFileHandler != null;
            if (containerFileHandler instanceof LoadK8sYamlHandlerImpl) {
                ((LoadK8sYamlHandlerImpl) containerFileHandler).setImageConfig(imageConfig);
            }
            containerFileHandler.load(filePaths);

            // default dependency mep service.
            containerFileHandler.setHasMep(true);

            // create charts-file(.tgz) and export it to the outPath.
            String helmChartsPackagePath = containerFileHandler.exportHelmChartsPackage();
            LOGGER.info("helmChartsPackagePath:{}", helmChartsPackagePath);

            String fileId = UUID.randomUUID().toString();
            String helmChartsName = new File(helmChartsPackagePath).getName();
            // use the first fileName to create the dir
            moveFileToWorkSpace(helmChartsPackagePath, fileId, helmChartsName);

            //save fileId
            saveFileRecord(fileId, helmChartsName);

            String tgzPath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + fileId
                + File.separator + helmChartsName;
            LOGGER.info("tgzPath:{}", tgzPath);

            //get image
            List<String> imageList = ContainerAppHelmChartUtil.getImageFromHelmFile(tgzPath);
            if (CollectionUtils.isEmpty(imageList)) {
                LOGGER.error("No image information was found in the yaml file under the template folder!");
                throw new HarborException("no images found from tgz file!",
                    ResponseConsts.RET_GET_HARBOR_IMAGE_LIST_FAIL);
            }
            // verify image exist
            boolean ret = ContainerAppHelmChartUtil.checkImageExist(imageList);
            if (!ret) {
                LOGGER.error("The image information in yaml file is not found in harbor repo!");
                throw new HarborException("some images are not found in harbor repo!",
                    ResponseConsts.RET_GET_HARBOR_IMAGE_LIST_FAIL);
            }

            // create a file id, and update
            HelmChart helmChart = new HelmChart();
            helmChart.setId(UUID.randomUUID().toString());
            helmChart.setHelmChartFileId(fileId);
            helmChart.setName(helmChartsName);
            helmChart.setApplicationId(applicationId);
            helmChart.setHelmChartFileList(containerFileHandler.getCatalog());
            helmChart.setCreateTime(new Date());
            int res = helmChartMapper.createHelmChart(applicationId, helmChart);
            if (res < 1) {
                LOGGER.error("Failed to save helm chart!");
                throw new DataBaseException("Failed to save helm chart!", ResponseConsts.RET_CERATE_DATA_FAIL);
            }
            return helmChart;
        } catch (IOException e) {
            LOGGER.error("Failed to read the helmchart file. msg:{}", e.getMessage());
            return null;
        }
    }

    private boolean verifyFileType(String[] filePaths) {
        long yamlCount = Arrays.stream(filePaths)
            .filter(item -> item.toLowerCase().endsWith(".yaml") || item.toLowerCase().endsWith(".yml")).count();
        long tgzCount = Arrays.stream(filePaths).filter(item -> item.toLowerCase().endsWith(".tgz")).count();
        if (yamlCount + tgzCount < filePaths.length) {
            LOGGER.error("Only YAML and TGZ file types are supported. Please check the input file types.");
            return false;
        }
        if (yamlCount != 0 && tgzCount != 0) {
            LOGGER.error("Do not support input tgz and yaml at once.");
            return false;
        }
        return true;
    }

    public HelmChart uploadHelmChartFile(String applicationId, MultipartFile helmTemplateYaml) {
        try {
            String filePath = saveLoadedFileToTempDir(helmTemplateYaml);
            return uploadHelmChartFile(applicationId, filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to read the helmchart file. msg:{}", e.getMessage());
            return null;
        }
    }

    private String saveLoadedFileToTempDir(MultipartFile helmTemplateYaml) throws IOException {
        Path tempDir = Files.createTempDirectory("eg-dev-");
        String path = tempDir.toString();
        File loadFile = new File(path + File.separator + helmTemplateYaml.getOriginalFilename());
        helmTemplateYaml.transferTo(loadFile);
        return loadFile.getCanonicalPath();
    }

    @Override
    public List<HelmChart> getHelmChartList(String applicationId) {
        if (StringUtils.isEmpty(applicationId)) {
            LOGGER.error("applicationId is empty!");
            throw new IllegalRequestException("applicationId is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        return helmChartMapper.getHelmChartsByAppId(applicationId);
    }

    @Override
    public HelmChart getHelmChartById(String applicationId, String helmChartId) {
        if (StringUtils.isEmpty(applicationId)) {
            LOGGER.error("applicationId is empty!");
            throw new IllegalRequestException("applicationId is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isEmpty(helmChartId)) {
            LOGGER.error("helm chart id is empty!");
            throw new IllegalRequestException("helm chart id is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        HelmChart chart = helmChartMapper.getHelmChartById(helmChartId);
        if (chart == null || !chart.getApplicationId().equals(applicationId)) {
            LOGGER.error("the query HelmChart is empty!");
            throw new EntityNotFoundException("the query HelmChart is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        ContainerAppInstantiateInfo containerAppInstantiateInfo = containerAppOperationService
            .getInstantiateInfo(applicationId);
        if (containerAppInstantiateInfo != null) {
            chart.setContainerAppInstantiateInfo(containerAppInstantiateInfo);
        }
        return chart;
    }

    @Override
    public Boolean deleteHelmChartById(String applicationId, String helmChartId) {
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(helmChartId)) {
            LOGGER.error("applicationId or helmChartId is empty!");
            throw new IllegalRequestException("applicationId or helmChartId is empty!",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("the query Application is empty!");
            throw new EntityNotFoundException("the query Application is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HelmChart helmChart = helmChartMapper.getHelmChartById(helmChartId);
        if (helmChart == null) {
            LOGGER.error("query HelmChart is empty!");
            return true;
        }
        //delete helm chart file or k8s yaml
        String helmChartFileId = helmChart.getHelmChartFileId();
        if (StringUtils.isEmpty(helmChartFileId)) {
            LOGGER.error("helmChartFileId is empty!");
            throw new DataBaseException("helmChartFileId is empty!", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        UploadFile uploadFile = uploadFileMapper.getFileById(helmChartFileId);
        File helmChartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
        FileUtil.deleteFile(helmChartFile);
        // delete data
        int ret = helmChartMapper.deleteHelmChart(helmChartId, helmChart.getHelmChartFileId());
        if (ret < 1) {
            LOGGER.error("delete helm chart file failed!");
            throw new DataBaseException("delete helm chart file failed!", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean deleteHelmChartByAppId(String applicationId) {
        if (StringUtils.isEmpty(applicationId)) {
            LOGGER.error("applicationId or helmChartId is empty!");
            throw new IllegalRequestException("applicationId is empty!", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("the query Application is empty!");
            throw new EntityNotFoundException("the query Application is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        List<HelmChart> helmChartList = helmChartMapper.getHelmChartsByAppId(applicationId);
        if (CollectionUtils.isEmpty(helmChartList)) {
            LOGGER.error("the application {} did not upload any helm chart files", applicationId);
            return true;
        }
        for (HelmChart helmChart : helmChartList) {
            //delete helm chart file or k8s yaml
            String helmChartFileId = helmChart.getHelmChartFileId();
            if (StringUtils.isEmpty(helmChartFileId)) {
                LOGGER.error("helmChartFileId is empty!");
                return false;
            }
            UploadFile uploadFile = uploadFileMapper.getFileById(helmChartFileId);
            File helmChartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
            FileUtil.deleteFile(helmChartFile);
            // delete data
            int ret = helmChartMapper.deleteHelmChart(helmChart.getId(), helmChart.getHelmChartFileId());
            if (ret < 1) {
                LOGGER.error("delete helm chart file failed!");
                return false;
            }
        }
        return true;
    }

    @Override
    public byte[] downloadHelmChart(String applicationId, String helmChartId) {
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(helmChartId)) {
            LOGGER.error("applicationId or helmChartId is empty!");
            throw new IllegalRequestException("applicationId or helmChartId is empty!",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        HelmChart helmChart = helmChartMapper.getHelmChartById(helmChartId);
        if (helmChart == null || !helmChart.getApplicationId().equals(applicationId)) {
            LOGGER.error("the query HelmChart is empty!");
            throw new EntityNotFoundException("the query HelmChart is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        String helmChartFileId = helmChart.getHelmChartFileId();
        if (StringUtils.isEmpty(helmChartFileId)) {
            LOGGER.error("helmChartFileId is empty!");
            throw new DataBaseException("helmChartFileId is empty!", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        byte[] ret = null;
        UploadFile uploadFile = uploadFileMapper.getFileById(helmChartFileId);
        File helmChartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
        try {
            ret = FileUtils.readFileToByteArray(helmChartFile);
        } catch (IOException e) {
            LOGGER.error("get helm chart file failed : {}", e.getMessage());
            throw new FileOperateException("get helm chart file failed!", ResponseConsts.RET_DOWNLOAD_FILE_FAIL);
        }
        return ret;
    }

    @Override
    public String getFileContentByFilePath(String applicationId, String helmChartId, String filePath) {
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(helmChartId)) {
            LOGGER.error("applicationId or helmChartId is empty!");
            throw new IllegalRequestException("applicationId or helmChartId is empty!",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        HelmChart helmChart = helmChartMapper.getHelmChartById(helmChartId);
        if (helmChart == null || !helmChart.getApplicationId().equals(applicationId)) {
            LOGGER.error("the query HelmChart is empty!");
            throw new EntityNotFoundException("the query HelmChart is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String helmChartFileId = helmChart.getHelmChartFileId();

        if (StringUtils.isEmpty(helmChartFileId)) {
            LOGGER.error("helmChartFileId is empty!");
            throw new DataBaseException("helmChartFileId is empty!", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        String content = "";
        UploadFile uploadFile = uploadFileMapper.getFileById(helmChartFileId);
        checkUploadFileExist(uploadFile, helmChartFileId);
        File helmChartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
        checkHelmFileExist(helmChartFile, helmChartFileId);
        try {
            String helmPath = helmChartFile.getCanonicalPath();
            IContainerFileHandler containerFileHandler = LoadContainerFileFactory.createLoader(helmPath);
            assert containerFileHandler != null;
            containerFileHandler.load(helmPath);
            content = containerFileHandler.getContentByInnerPath(filePath);
        } catch (IOException e) {
            LOGGER.error("read file under {} path occur {}", filePath, e.getMessage());
            return null;
        }
        return content;
    }

    private void checkHelmFileExist(File file, String helmChartFileId) {
        if (!file.exists()) {
            LOGGER.error("helm file {} not found", helmChartFileId);
            throw new FileFoundFailException("helm chart file not found", ResponseConsts.RET_FILE_NOT_FOUND);
        }
    }

    private void checkUploadFileExist(UploadFile file, String helmChartFileId) {
        if (file == null) {
            LOGGER.error("upload file {} not found", helmChartFileId);
            throw new FileFoundFailException("query uploadFile is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    @Override
    public Boolean modifyFileContent(String applicationId, String helmChartId, ModifyFileContentDto contentDto) {
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(helmChartId)) {
            LOGGER.error("applicationId or helmChartId is empty!");
            throw new IllegalRequestException("applicationId or helmChartId is empty!",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (contentDto == null) {
            LOGGER.error("param ModifyFileContentDto is null!");
            throw new IllegalRequestException("param ModifyFileContentDto is null!",
                ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        HelmChart helmChart = helmChartMapper.getHelmChartById(helmChartId);
        if (helmChart == null || !helmChart.getApplicationId().equals(applicationId)) {
            LOGGER.error("the query HelmChart is empty!");
            throw new EntityNotFoundException("the query HelmChart is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String helmChartFileId = helmChart.getHelmChartFileId();
        if (StringUtils.isEmpty(helmChartFileId)) {
            LOGGER.error("helmChartFileId is empty!");
            throw new DataBaseException("helmChartFileId is empty!", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        boolean ret = false;
        UploadFile uploadFile = uploadFileMapper.getFileById(helmChartFileId);
        checkUploadFileExist(uploadFile, helmChartFileId);
        File helmChartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
        checkHelmFileExist(helmChartFile, helmChartFileId);
        try {
            IContainerFileHandler containerFileHandler = LoadContainerFileFactory
                .createLoader(helmChartFile.getCanonicalPath());
            assert containerFileHandler != null;
            containerFileHandler.load(helmChartFile.getCanonicalPath());
            ret = containerFileHandler.modifyFileByPath(contentDto.getInnerFilePath(), contentDto.getContent());
            String fileNewPath = containerFileHandler.exportHelmChartsPackage();
            com.google.common.io.Files.move(new File(fileNewPath), new File(helmChartFile.getCanonicalPath()));
        } catch (IOException e) {
            LOGGER.error("write file under {} path occur {}", contentDto.getInnerFilePath(), e.getMessage());
            return false;
        }
        return ret;
    }

    private void saveFileRecord(String fileId, String fileName) {
        UploadFile result = new UploadFile();
        result.setFileName(fileName);
        result.setFileId(fileId);
        result.setUserId(AccessUserUtil.getUserId());
        result.setUploadDate(new Date());
        result.setTemp(false);
        result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId + File.separator + fileName);
        int ret = uploadFileMapper.saveFile(result);
        if (ret < 1) {
            LOGGER.error("save file record to db failed!");
            throw new DataBaseException("save file record to db failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    private void moveFileToWorkSpace(String fromPath, String fileId, String fileName) {
        try {
            String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + fileId
                + File.separator;
            String fileRealPath = upLoadDir + fileName;
            File dir = new File(upLoadDir);
            if (!dir.isDirectory()) {
                boolean isSuccess = dir.mkdirs();
                if (!isSuccess) {
                    LOGGER.error("create upload dir fail!");
                    throw new FileOperateException("create upload dir fail!", ResponseConsts.RET_CREATE_FILE_FAIL);
                }
            }
            com.google.common.io.Files.move(new File(fromPath), new File(fileRealPath));
        } catch (IOException e) {
            LOGGER.error("write upload file failed!");
            throw new FileOperateException("write upload file failed!", ResponseConsts.RET_WRITE_FILE_FAIL);
        }
    }

}
