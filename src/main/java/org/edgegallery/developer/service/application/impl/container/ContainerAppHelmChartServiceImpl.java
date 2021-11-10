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

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.application.container.ContainerAppImageInfoMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.container.ContainerAppImageInfo;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.UploadFileUtil;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
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
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private AppConfigurationService appConfigurationService;

    public boolean uploadContainerFile(MultipartFile helmTemplateYaml, String applicationId) {
        try {
            String filePath = saveLoadedFileToTempDir(helmTemplateYaml);
            IContainerFileHandler containerFileHandler = LoadContainerFileFactory.createLoader(filePath);
            assert containerFileHandler != null;
            containerFileHandler.load(filePath);

            // default dependency mep service.
            containerFileHandler.setHasMep(true);

            // create charts-file(.tgz) and export it to the outPath.
            String helmCharts = containerFileHandler.exportHelmCharts("");

            String fileId = ContainerAppHelmChartUtil
                .writeContentToFile(Files.readAllBytes(new File(helmCharts).toPath()));

            // create a file id, and update
            HelmChart helmChart = new HelmChart();
            helmChart.setId(UUID.randomUUID().toString());
            helmChart.setHelmChartFileId(fileId);
            helmChart.setName(helmTemplateYaml.getName());
            int res = helmChartMapper.createHelmChart(applicationId, helmChart);
            if (res < 1) {
                LOGGER.error("Failed to save helm chart!");
                throw new DataBaseException("Failed to save helm chart!", ResponseConsts.RET_CERATE_DATA_FAIL);
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String saveLoadedFileToTempDir(MultipartFile helmTemplateYaml) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), "eg-dev-");
        assert tempFile.mkdirs();
        String path = tempFile.getCanonicalPath();
        File loadFile = new File(path + File.separator + helmTemplateYaml.getName());
        helmTemplateYaml.transferTo(loadFile);
        return loadFile.getCanonicalPath();
    }

    @Override
    public Boolean uploadHelmChartYaml(MultipartFile helmTemplateYaml, String applicationId) {
        //replace namespace
        String content = ContainerAppHelmChartUtil.replaceNamesapce(helmTemplateYaml);
        // write content file
        String fileId = ContainerAppHelmChartUtil.writeContentToFile(content);
        //save file to table
        String fileName = helmTemplateYaml.getOriginalFilename();
        saveFileRecord(fileId, fileName);
        //determine whether the file conforms to yaml format
        List<Map<String, Object>> mapList = ContainerAppHelmChartUtil.verifyYamlFormat(content);
        //Verify whether there is image, service and MEP agent in the file
        List<String> requiredItems = Lists.newArrayList("image", "service", "mep-agent");
        ContainerAppHelmChartUtil.verifyHelmTemplate(mapList, requiredItems);
        if (!CollectionUtils.isEmpty(requiredItems) && requiredItems.size() >= 2) {
            LOGGER.error("Failed to verify helm template yaml!");
            throw new FileOperateException("failed to validate yaml scheme!", ResponseConsts.RET_FILE_FORMAT_ERROR);
        }
        // handle image and save uploaded file
        File tempFile;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
            helmTemplateYaml.transferTo(tempFile);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("transfer multifile to file failed {}", e.getMessage());
            throw new FileOperateException("transfer file failed!", ResponseConsts.RET_TRANSFER_FILE_FAIL);
        }
        if (!CollectionUtils.isEmpty(requiredItems) && requiredItems.size() == 1 && requiredItems.get(0)
            .equals("mep-agent")) {
            return handleImageAndHelmchart(tempFile, applicationId, helmTemplateYaml, fileId);
        }
        return handleImageAndHelmchart(tempFile, applicationId, helmTemplateYaml, fileId);
    }

    @Override
    public Boolean createHelmCharts(MultipartFile helmChartFile, String applicationId) {
        return null;
    }

    @Override
    public List<HelmChart> getHelmChartList(String applicationId) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalRequestException("applicationId is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        return helmChartMapper.getHelmChartsByAppId(applicationId);
    }

    @Override
    public HelmChart getHelmChartById(String applicationId, String id) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalRequestException("applicationId is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isEmpty(id)) {
            throw new IllegalRequestException("helm chart id is empty", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            throw new EntityNotFoundException("the query Application is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HelmChart chart = helmChartMapper.getHelmChartById(id);
        if (chart == null) {
            throw new EntityNotFoundException("the query HelmChart is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        return chart;
    }

    @Override
    public Boolean deleteHelmChartById(String applicationId, String id) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalRequestException("applicationId is empty!", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isEmpty(id)) {
            throw new IllegalRequestException("helm chart id is empty!", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            throw new EntityNotFoundException("the query Application is empty", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HelmChart helmChart = helmChartMapper.getHelmChartById(id);
        if (helmChart == null) {
            throw new EntityNotFoundException("query HelmChart is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        //delete data
        int ret = helmChartMapper.deleteFileAndImage(id, helmChart.getHelmChartFileId(), applicationId);
        if (ret < 1) {
            throw new DataBaseException("delete helm chart file failed!", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

    private void saveFileRecord(String fileId, String fileName) {
        UploadedFile result = new UploadedFile();
        result.setFileName(fileName);
        result.setFileId(fileId);
        result.setUserId(AccessUserUtil.getUserId());
        result.setUploadDate(new Date());
        result.setTemp(false);
        result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
        int ret = uploadedFileMapper.saveFile(result);
        if (ret < 1) {
            throw new DataBaseException("save file record to db failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    private boolean handleImageAndHelmchart(File newFile, String applicationId, MultipartFile oldFile,
        String newFileId) {
        // save image
        boolean isSaved = saveImage(newFile, applicationId, newFileId);
        if (!isSaved) {
            LOGGER.error("Failed to save Image!");
            throw new DataBaseException("Failed to save Image", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        //save helm chart
        HelmChart helmChart = new HelmChart();
        helmChart.setId(UUID.randomUUID().toString());
        helmChart.setName(oldFile.getOriginalFilename());
        helmChart.setHelmChartFileId(newFileId);
        int res = helmChartMapper.createHelmChart(applicationId, helmChart);
        if (res < 1) {
            LOGGER.error("Failed to save helm chart!");
            throw new DataBaseException("Failed to save helm chart!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    private boolean saveImage(File helmYaml, String applicationId, String newFileId) {
        //yamlRead aslist
        List<String> list = UploadFileUtil.readFileByLine(helmYaml);
        List<String> podImages = new ArrayList<>();
        //query image and save
        for (String str : list) {
            if (str.contains("image:")) {
                if (str.contains(".Values.imagelocation.domainname") || str.contains(".Values.imagelocation.project")) {
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
        ContainerAppImageInfo imageInfo = new ContainerAppImageInfo();
        imageInfo.setId(UUID.randomUUID().toString());
        imageInfo.setImageInfo(podImages.toString());
        imageInfo.setApplicationId(applicationId);
        imageInfo.setHelmChartFileId(newFileId);
        int res = containerAppImageInfoMapper.saveImageInfo(imageInfo);
        if (res <= 0) {
            return false;
        }
        return true;
    }
}
