package org.edgegallery.developer.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.lcm.MecHostBody;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.HostService;
import org.edgegallery.developer.util.CustomResponseErrorHandler;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.InputParameterUtil;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class HostServiceImpl implements HostService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostServiceImpl.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static int VNC_PORT = 22;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * getALlHosts.
     *
     * @return
     */
    @Override
    public Page<MepHost> getAllHosts(String userId, String name, String ip, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<MepHost> pageInfo = new PageInfo<>(hostMapper.getHostsByCondition(userId, name, ip));
        LOGGER.info("Get all hosts success.");
        return new Page<>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * selectALlHosts.
     *
     * @return
     */
    @Override
    public Page<MepHost> selectAllHosts(String os, String architecture, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<MepHost> pageInfo = new PageInfo<>(hostMapper.selectHostsByCondition(os, architecture));
        LOGGER.info("Get all hosts success.");
        return new Page<>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    @Override
    public Either<FormatRespDto, Boolean> createHost(MepCreateHost host, String token) {
        MepHost mepHost = hostMapper.getHostsByMecHost(host.getMecHost());
        if (mepHost != null) {
            LOGGER.info("mecHost have exit:{}", host.getMecHost());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "mecHost have exit"));
        }
        if (StringUtils.isBlank(host.getUserId()) || !isAdminUser()) {
            LOGGER.error("Create host failed, userId is empty or not admin");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "userId is empty or not admin"));
        }
        if ("OpenStack".equals(host.getOs())) {
            Map<String, String> getParams = InputParameterUtil.getParams(host.getParameter());
            if (!getParams.containsKey("app_mp1_ip") || !getParams.containsKey("app_n6_ip") || !getParams
                .containsKey("app_internet_ip")) {
                LOGGER.error("Network params config error");
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Network params config error"));
            }
        }
        // health check
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
            boolean uploadRes = uploadFileToLcm(host.getProtocol(), host.getLcmIp(), host.getPort(),
                uploadedFile.getFilePath(), host.getMecHost(), token);
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
    @Override
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
    @Override
    @Transactional
    public Either<FormatRespDto, Boolean> updateHost(String hostId, MepCreateHost host, String token) {
        // health check
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
            boolean uploadRes = uploadFileToLcm(host.getProtocol(), host.getLcmIp(), host.getPort(),
                uploadedFile.getFilePath(), host.getMecHost(), token);
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
    @Override
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
    @Override
    public Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId) {
        List<MepHostLog> hostLogList = hostLogMapper.getHostLogByHostId(hostId);
        LOGGER.info("Get host logs success.");
        return Either.right(hostLogList);
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        LOGGER.info("user auth:{}", currUserAuth);
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

    private boolean addMecHostToLcm(MepCreateHost host) {
        MecHostBody body = new MecHostBody();
        body.setAffinity(host.getArchitecture());
        body.setCity(host.getAddress());
        body.setMechostIp(host.getMecHost());
        body.setMechostName(host.getName());
        if (host.getOs().equals("OpenStack") || host.getOs().equals("FusionSphere")) {
            body.setVim("OpenStack");
        } else {
            body.setVim("K8s");
        }
        body.setOrigin("developer");
        // add headers
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

    private boolean uploadFileToLcm(String protocol, String lcmIp, int port, String filePath, String mecHost,
        String token) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        RestTemplate restTemplate = RestTemplateBuilder.create();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("configFile", new FileSystemResource(file));
        body.add("hostIp", mecHost);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String url = getUrlPrefix(protocol, lcmIp, port) + Consts.APP_LCM_UPLOAD_FILE;
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

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }
}
