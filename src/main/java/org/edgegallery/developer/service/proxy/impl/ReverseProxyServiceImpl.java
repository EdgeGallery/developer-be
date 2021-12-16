package org.edgegallery.developer.service.proxy.impl;

import static org.edgegallery.developer.util.HttpClientUtil.getUrlPrefix;


import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.resource.mephost.MepHostMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.lcm.ConsoleResponse;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.reverseproxy.ReverseProxy;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class ReverseProxyServiceImpl implements ReverseProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyService.class);

    @Autowired
    private MepHostMapper mepHostMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private VMAppOperationService vmAppOperationService;

    @Autowired
    private VMAppVmService vmAppVmService;

    private static Gson gson = new Gson();

    @Value("${developer.ip:}")
    private String developerIp;

    @Value("${developer.protocol:}")
    private String protocol;

    @Value("${developer.cbbport:}")
    private String cbbPort;

    private String reverseProxyBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    private Lock lock = new ReentrantLock();

    /**
     * add reverse proxy
     *
     * @param hostId
     * @param hostConsolePort vnc port
     */
    @Override
    public void addReverseProxy(String hostId, int hostConsolePort, String token) {
        MepHost mepHost = mepHostMapper.getHost(hostId);
        String mecHostIp = mepHost.getMecHostIp();
        String lcmIp = mepHost.getLcmIp();
        String nextHopProtocol = null;
        String nextHopIp = null;

        if (StringUtils.isNotEmpty(lcmIp) && !lcmIp.equals(developerIp)) {
            LOGGER.info("mec host ip {} is different with lcm ip {}", mecHostIp, lcmIp);
            nextHopProtocol = mepHost.getLcmProtocol();
            nextHopIp = lcmIp;
        }

        ReverseProxy reverseProxy = new ReverseProxy(mecHostIp, hostConsolePort, nextHopProtocol, nextHopIp, 1);
        sendHttpRequest(getReverseProxyBaseUrl(), token, HttpMethod.POST, reverseProxy);
        LOGGER.info("succeed in adding reverse proxy, param is: {}", reverseProxy.toString());
    }

    @Override
    public void deleteReverseProxy(String hostId, int hostConsolePort, String token) {
        MepHost mepHost = mepHostMapper.getHost(hostId);
        String mecHostIp = mepHost.getMecHostIp();
        String url = new StringBuffer(getReverseProxyBaseUrl()).append("/dest-host-ip/").append(mecHostIp)
            .append("/dest-host-port/").append(hostConsolePort).toString();
        sendHttpRequest(url, token, HttpMethod.DELETE, null);
        LOGGER.info("succeed in deleting reverse proxy, dest host ip is: {}, dest host port is: {}", mecHostIp,
            hostConsolePort);
    }

    @Override
    public String getVmConsoleUrl(String applicationId, String vmId, String userId, String token) {
        Application application = applicationMapper.getApplicationById(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostMapper.getHost(hostId);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        if (instantiateInfo == null) {
            LOGGER.error("failed to get vnc console url, instantiate info does not exist.");
            throw new DeveloperException("failed to get vnc console url");
        }
        String basePath = getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        String workLoadStatus = HttpClientUtil
            .getWorkloadStatus(basePath, instantiateInfo.getAppInstanceId(), userId, token);
        LOGGER.info("get vm workLoad status:{}", workLoadStatus);
        VmInstantiateWorkload vmInstantiateWorkload = gson.fromJson(workLoadStatus, VmInstantiateWorkload.class);
        if (vmInstantiateWorkload == null || !Consts.HTTP_STATUS_SUCCESS_STR.equals(vmInstantiateWorkload.getCode())) {
            LOGGER.error("failed to get vnc console url, http request error happened.");
        String getVncUrlResult = HttpClientUtil
            .getVncUrl(basePath, userId, instantiateInfo.getDistributedMecHost(), instantiateInfo.getVmInstanceId(),
                token);
        LOGGER.info("get vm workLoad status:{}", getVncUrlResult);
        if (StringUtils.isEmpty(getVncUrlResult)) {
            LOGGER.error("failed to get vnc console url by lcm");
            throw new DeveloperException("failed to get vnc console url");
        }
        ConsoleResponse consoleResponse = gson.fromJson(getVncUrlResult, new TypeToken<ConsoleResponse>() {
        }.getType());
        String vncUrl = consoleResponse.getConsole().getUrl();
        String url = new StringBuffer(getReverseProxyBaseUrl()).append("/dest-host-ip/").append("192.168.1.156")
            .append("/dest-host-port/").append(Consts.DEFAULT_OPENSTACK_VNC_PORT).toString();
        String resp = sendHttpRequest(url, token, HttpMethod.GET, null);
        ReverseProxy reverseProxy = gson.fromJson(resp, ReverseProxy.class);

        return genProxyUrl(vncUrl, developerIp, reverseProxy.getLocalPort());
    }

    @Override
    public SshResponseInfo getVmSshResponseInfo(String applicationId, String vmId, String userId, String xsrfValue) {
        Application application = applicationMapper.getApplicationById(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostMapper.getHost(hostId);
        VirtualMachine vm = vmAppVmService.getVm(applicationId, vmId);
        if (vm.getVmInstantiateInfo() == null) {
            LOGGER.error("failed to get ssh console url, instantiate info does not exist.");
            throw new DeveloperException("failed to get ssh console url");
        }

        List<PortInstantiateInfo> portInstantiateInfos = vm.getVmInstantiateInfo().getPortInstanceList();
        String networkIp = portInstantiateInfos.get(0).getIpAddress();
        String username = vm.getVmCertificate().getPwdCertificate().getUsername();
        String password = vm.getVmCertificate().getPwdCertificate().getPassword();
        LOGGER.info("ip:{}", networkIp);
        LOGGER.info("username:{}", username);
        String basePath = HttpClientUtil
            .getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), 30209);
        SshResponseInfo sshResponseInfo = HttpClientUtil
            .sendWebSshRequest(basePath, networkIp, 22, username, password, xsrfValue);
        if (sshResponseInfo == null) {
            LOGGER.error("send WebSsh request fail.");
            throw new DeveloperException("failed to get ssh console url");
        }
        if (StringUtils.isEmpty(sshResponseInfo.getId())) {
            LOGGER.error(" WebSsh info input error:{}", sshResponseInfo.getStatus());
            throw new DeveloperException("WebSsh info input error");
        }
        sshResponseInfo.setSshAddress(basePath);

        return sshResponseInfo;
    }

    private String sendHttpRequest(String url, String token, HttpMethod method, ReverseProxy body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            LOGGER.error("Failed to send http request", e);
        }
        LOGGER.error("Failed to send http request, url is : {}, method is : {}", url, method);
        throw new DeveloperException("Failed to send http request");
    }

    private String genProxyUrl(String url, String proxyHostIp, int proxyHostPort) {
        String regex = "(\\d{1,3}(.\\d{1,3}){3}):(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            int count = matcher.groupCount();
            String vncUrl = url.replace(matcher.group(1), proxyHostIp)
                .replace(matcher.group(3), String.valueOf(proxyHostPort));
            LOGGER.debug("vnc url : {}, origin url : {}", vncUrl, url);
            return vncUrl;
        }
        LOGGER.error("failed to generate proxy url from {}", url);
        throw new DeveloperException("failed to generate proxy url");
    }

    private String getReverseProxyBaseUrl() {
        if (reverseProxyBaseUrl != null) {
            return reverseProxyBaseUrl;
        }
        try {
            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                if (reverseProxyBaseUrl != null) {
                    return reverseProxyBaseUrl;
                }

                reverseProxyBaseUrl = new StringBuffer(protocol).append("://localhost:")
                    .append(cbbPort).append("/commonservice/cbb/v1/reverseproxies").toString();
            }
        } catch (InterruptedException e) {
            LOGGER.error("failed to get the lock", e);
            throw new DeveloperException("failed to get reverse proxy base url");
        } finally {
            lock.unlock();
        }
        return reverseProxyBaseUrl;
    }
}
