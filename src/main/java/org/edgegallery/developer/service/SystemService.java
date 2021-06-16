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

package org.edgegallery.developer.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.lcm.MecHostBody;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.CustomResponseErrorHandler;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("systemService")
public class SystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static int VNC_PORT = 22;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * getALlHosts.
     *
     * @return
     */
    public Page<MepHost> getAllHosts(String userId, String name, String ip, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo pageInfo = new PageInfo<MepHost>(hostMapper.getHostsByCondition(userId, name, ip));
        LOGGER.info("Get all hosts success.");
        return new Page<MepHost>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> createHost(MepCreateHost host, String token) {
        if (StringUtils.isBlank(host.getUserName())) {
            LOGGER.error("Create host failed, username is empty");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "username is empty"));
        }
        if (StringUtils.isBlank(host.getPassword())) {
            LOGGER.error("Create host failed, password is empty");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "password is empty"));
        }
        if (StringUtils.isBlank(host.getUserId())) {
            LOGGER.error("Create host failed, userId is empty");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "userId is empty"));
        }
        //health check
        String healRes = HttpClientUtil.getHealth(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (healRes == null) {
            String msg = "health check faild,current ip or port cann't be used!";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // add mechost to lcm
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host.getLcmIp(), host.getPort(), uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        host.setHostId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setVncPort(VNC_PORT);
        int ret = hostMapper.createHost(host);
        if (ret > 0) {
            LOGGER.info("Crete host {} success ", host.getHostId());
            return Either.right(true);
        }
        LOGGER.error("Create host failed ");
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not create a host."));
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> deleteHost(String hostId) {
        int res = hostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "delete failed.");
            return Either.left(error);
        }
        LOGGER.info("Delete host {} success", hostId);
        return Either.right(true);
    }

    /**
     * updateHost.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> updateHost(String hostId, MepCreateHost host, String token) {
        //health check
        String healRes = HttpClientUtil.getHealth(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (healRes == null) {
            String msg = "health check faild,current ip or port cann't be used!";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // add mechost to lcm
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host.getLcmIp(), host.getPort(), uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        MepHost currentHost = hostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }

        host.setHostId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = hostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(true);
        }
        LOGGER.error("Update host {} failed", hostId);
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not update the host"));
    }

    /**
     * getHost.
     *
     * @return
     */
    public Either<FormatRespDto, MepHost> getHost(String hostId) {
        MepHost host = hostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return Either.right(host);
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }
    }

    /**
     * getHostLogByHostId.
     *
     * @param hostId hostId
     * @return
     */
    public Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId) {
        List<MepHostLog> hostLogList = hostLogMapper.getHostLogByHostId(hostId);
        LOGGER.info("Get host logs success.");
        return Either.right(hostLogList);
    }

    /**
     * createCapabilityGroup.
     *
     * @param capabilityGroup capabilityGroup
     * @return
     */
    @Transactional
    public Either<FormatRespDto, OpenMepCapabilityGroup> createCapabilityGroup(OpenMepCapabilityGroup capabilityGroup) {
        capabilityGroup.setGroupId(UUID.randomUUID().toString());
        if (StringUtils.isEmpty(capabilityGroup.getDescriptionEn())) {
            capabilityGroup.setDescriptionEn(capabilityGroup.getDescription());
        }
        if (StringUtils.isEmpty(capabilityGroup.getIconFileId())) {
            capabilityGroup.setIconFileId(capabilityGroup.getIconFileId());
        }
        if (StringUtils.isEmpty(capabilityGroup.getAuthor())) {
            capabilityGroup.setAuthor(capabilityGroup.getAuthor());
        }

        if (StringUtils.isEmpty(capabilityGroup.getOneLevelNameEn())) {
            capabilityGroup.setOneLevelNameEn(capabilityGroup.getOneLevelName());
        }
        if (StringUtils.isEmpty(capabilityGroup.getTwoLevelNameEn())) {
            capabilityGroup.setTwoLevelNameEn(capabilityGroup.getTwoLevelName());
        }
        capabilityGroup.setSelectCount(0);
        capabilityGroup.setUploadTime(new Date());
        int ret = openMepCapabilityMapper.saveGroup(capabilityGroup);
        if (ret <= 0) {
            LOGGER.error("save group {} failed!", capabilityGroup.getGroupId());
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability-group failed"));
        }

        for (OpenMepCapabilityDetail capability : capabilityGroup.getCapabilityDetailList()) {
            if (StringUtils.isBlank(capability.getApiFileId())) {
                LOGGER.error("Create {} detail failed, api file id is null", capabilityGroup.getGroupId());
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Api file id is wrong"));
            }
            if (StringUtils.isBlank(capability.getGuideFileId())) {
                LOGGER.error("Create {} detail failed, guide file id is null", capabilityGroup.getGroupId());
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "guide file id is wrong"));
            }
            capability.setGroupId(capabilityGroup.getGroupId());
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            capability.setUploadTime(time.format(new Date()));
            capability.setDetailId(UUID.randomUUID().toString());
            int result = openMepCapabilityMapper.saveCapability(capability);
            if (result > 0) {
                LOGGER.info("Create {} detail success", capabilityGroup.getGroupId());
                // update api file to un temp
                int api = uploadedFileMapper.updateFileStatus(capability.getApiFileId(), false);
                int guide = uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), false);
                int guideEn = uploadedFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
                if (api <= 0 || guide <= 0 || guideEn <= 0) {
                    String msg = "update api or guide or guide-en file status occur db error";
                    LOGGER.error(msg);
                    return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, msg));
                }
            } else {
                LOGGER.error("save capability {} failed!", capability.getDetailId());
                return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability-detail failed"));
            }
        }
        LOGGER.info("Create capability group {} success", capabilityGroup.getGroupId());
        return Either.right(capabilityGroup);

    }

    /**
     * deleteCapabilityByUserIdAndGroupId.
     */
    public Either<FormatRespDto, Boolean> deleteCapabilityByUserIdAndGroupId(String groupId) {
        List<OpenMepCapabilityDetail> capabilityDetailList = openMepCapabilityMapper.getDetailByGroupId(groupId);
        if (!CollectionUtils.isEmpty(capabilityDetailList)) {
            for (OpenMepCapabilityDetail capabilityDetail : capabilityDetailList) {
                int res = openMepCapabilityMapper.deleteCapability(capabilityDetail.getDetailId());
                if (res < 1) {
                    LOGGER.info("{} can not find", capabilityDetail.getDetailId());
                } else {
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getApiFileId(), true);
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getGuideFileId(), true);
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getGuideFileIdEn(), true);
                    LOGGER.info("Delete capability detail {} success", capabilityDetail.getDetailId());
                }
            }
        }
        int res = openMepCapabilityMapper.deleteGroup(groupId);
        if (res < 1) {
            LOGGER.info("{} can not find", groupId);
        } else {
            LOGGER.info("Delete group {} success", groupId);
        }
        return Either.right(true);
    }

    /**
     * getAllCapabilityGroups.
     */
    public Page<OpenMepCapabilityGroup> getAllCapabilityGroups(String userId, String twoLevelName,
        String twoLevelNameEn, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo pageInfo = new PageInfo<OpenMepCapabilityGroup>(
            openMepCapabilityMapper.getOpenMepListByCondition(userId, twoLevelName, twoLevelNameEn));
        LOGGER.info("Get all capability groups success.");
        return new Page<OpenMepCapabilityGroup>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * getCapabilityByGroupId.
     */
    public Either<FormatRespDto, OpenMepCapabilityGroup> getCapabilityByGroupId(String groupId) {
        OpenMepCapabilityGroup group = openMepCapabilityMapper.getOpenMepCapabilitiesByGroupId(groupId);
        if (group != null) {
            List<OpenMepCapabilityDetail> details = group.getCapabilityDetailList();
            if (details != null) {
                Iterator<OpenMepCapabilityDetail> iterator = details.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getDetailId() == null) {
                        iterator.remove();
                    }
                }
            }
            LOGGER.info("Get capability by {} success", groupId);
            return Either.right(group);
        }
        LOGGER.error("Can not get capability by {}", groupId);
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get capabilities by group failed"));
    }

    private boolean uploadFileToLcm(String hostIp, int port, String filePath, String token) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        RestTemplate restTemplate = RestTemplateBuilder.create();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("configFile", new FileSystemResource(file));
        body.add("hostIp", hostIp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String url = getUrlPrefix("https", hostIp, port) + Consts.APP_LCM_UPLOAD_FILE;
            LOGGER.info(" upload file url is {}", url);
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("upload file lcm log:{}", response);
        } catch (Exception e) {
            LOGGER.error("Failed to upload file lcm, exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to upload file lcm, filePath is {}", filePath);
        return false;
    }

    private boolean addMecHostToLcm(MepCreateHost host) {
        MecHostBody body = new MecHostBody();
        body.setAffinity(host.getArchitecture());
        body.setCity(host.getAddress());
        body.setMechostIp(host.getMecHost());
        body.setMechostName(host.getName());
        body.setVim(host.getOs());
        body.setOrigin("developer");
        //add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort()) + Consts.APP_LCM_ADD_MECHOST;
        LOGGER.info("add mec host url:{}", url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("add mec host to lcm log:{}", response);
        } catch (CustomException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getBody());
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed add mec host to lcm");
        return false;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }
}
