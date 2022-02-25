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

package org.edgegallery.developer.service.application.container;

import java.util.List;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.application.container.ModifyFileContentDto;
import org.springframework.web.multipart.MultipartFile;

public interface ContainerAppHelmChartService {

    /**
     * upload container application deploy yaml.
     *
     * @param applicationId applicationId
     * @param helmTemplateYaml deploy yam
     * @return
     */
    HelmChart uploadHelmChartFile(String applicationId, MultipartFile helmTemplateYaml);

    /**
     * upload container application deploy yaml(multiple).
     *
     * @param applicationId applicationId
     * @param filePaths every deploy yaml path
     * @return
     */
    HelmChart uploadHelmChartFile(String applicationId, String... filePaths);

    /**
     * get generate or upload tgz file list.
     *
     * @param applicationId applicationId
     * @return
     */
    List<HelmChart> getHelmChartList(String applicationId);

    /**
     * get upload file structure.
     *
     * @param applicationId applicationId
     * @param helmChartId file id
     * @return
     */
    HelmChart getHelmChartById(String applicationId, String helmChartId);

    /**
     * delete upload deploy yaml with file id.
     *
     * @param applicationId applicationId
     * @param helmChartId file id
     * @return
     */
    Boolean deleteHelmChartById(String applicationId, String helmChartId);

    /**
     * delete upload deploy yaml with application id.
     *
     * @param applicationId applicationId
     * @return
     */
    Boolean deleteHelmChartByAppId(String applicationId);

    /**
     * download upload deploy yaml.
     *
     * @param applicationId applicationId
     * @param helmChartId file id
     * @return
     */
    byte[] downloadHelmChart(String applicationId, String helmChartId);

    /**
     * get file content in generate or upload tgz file.
     *
     * @param applicationId applicationId
     * @param helmChartId file id
     * @param filePath file path
     * @return
     */
    String getFileContentByFilePath(String applicationId, String helmChartId, String filePath);

    /**
     * update file content in generate or upload tgz file.
     *
     * @param applicationId applicationId
     * @param helmChartId file id
     * @param content needed update content
     * @return
     */
    Boolean modifyFileContent(String applicationId, String helmChartId, ModifyFileContentDto content);
}
