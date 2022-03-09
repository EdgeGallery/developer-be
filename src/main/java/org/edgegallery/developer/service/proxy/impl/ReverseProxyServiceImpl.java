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

package org.edgegallery.developer.service.proxy.impl;

import static org.edgegallery.developer.util.HttpClientUtil.getUrlPrefix;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.lcm.ConsoleResponse;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.reverseproxy.ReverseProxy;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.edgegallery.developer.util.AesUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
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

@Service("reverseProxyService")
public class ReverseProxyServiceImpl implements ReverseProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyService.class);

    @Autowired
    private MepHostService mepHostService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private VMAppOperationService vmAppOperationService;

    @Autowired
    private VMAppVmService vmAppVmService;

    @Autowired
    private PkgSpecService pkgSpecService;

    private static Gson gson = new Gson();

    @Value("${developer.ip:}")
    private String developerIp;

    @Value("${developer.protocol:}")
    private String protocol;

    @Value("${developer.cbbport:}")
    private String cbbPort;

    @Value("${client.client-id:}")
    private String clientId;

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
        MepHost mepHost = mepHostService.getHost(hostId);
        String mecHostIp = mepHost.getMecHostIp();
        String mecHostProtocol = mepHost.getMecHostProtocol();
        String lcmIp = mepHost.getLcmIp();
        String nextHopProtocol = null;
        String nextHopIp = null;

        if (StringUtils.isNotEmpty(lcmIp) && !lcmIp.equals(developerIp)) {
            LOGGER.info("mec host ip {} is different with lcm ip {}", mecHostIp, lcmIp);
            nextHopProtocol = mepHost.getLcmProtocol();
            nextHopIp = lcmIp;
        }

        ReverseProxy reverseProxy = new ReverseProxy(mecHostProtocol, mecHostIp, hostConsolePort, nextHopProtocol,
            nextHopIp, 1);
        sendHttpRequest(getReverseProxyBaseUrl(), token, HttpMethod.POST, reverseProxy);
        LOGGER.info("succeed in adding reverse proxy, param is: {}", reverseProxy);
    }

    @Override
    public void deleteReverseProxy(String hostId, int hostConsolePort, String token) {
        MepHost mepHost = mepHostService.getHost(hostId);
        String mecHostIp = mepHost.getMecHostIp();
        String url = new StringBuffer(getReverseProxyBaseUrl()).append("/dest-host-ip/").append(mecHostIp)
            .append("/dest-host-port/").append(mepHost.getMecHostPort()).toString();
        sendHttpRequest(url, token, HttpMethod.DELETE, null);
        LOGGER.info("succeed in deleting reverse proxy, dest host ip is: {}, dest host port is: {}", mecHostIp,
            mepHost.getMecHostPort());
    }

    @Override
    public String getVmConsoleUrl(String applicationId, String vmId, String userId, String token) {
        Application application = applicationService.getApplication(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostService.getHost(hostId);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        if (instantiateInfo == null) {
            LOGGER.error("failed to get vnc console url, instantiate info does not exist.");
            throw new DeveloperException("failed to get vnc console url");
        }
        String basePath = getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        String getVncUrlResult = HttpClientUtil
            .getVncUrl(basePath, userId, instantiateInfo.getDistributedMecHost(), instantiateInfo.getVmInstanceId(),
                token);
        LOGGER.info("get vm workLoad status:{}", getVncUrlResult);
        if (StringUtils.isEmpty(getVncUrlResult)) {
            LOGGER.error("failed to get vnc console url by lcm");
            throw new DeveloperException("failed to get vnc console url");
        }
        ConsoleResponse consoleResponse = gson
            .fromJson(getVncUrlResult, new TypeToken<ConsoleResponse>() { }.getType());
        String vncUrl = consoleResponse.getConsole().getUrl();
        if (mepHost.getVimType() == EnumVimType.FusionSphere) {
            return genProxyUrl(vncUrl, mepHost.getMecHostIp(), mepHost.getMecHostPort());
        }
        String url = new StringBuffer(getReverseProxyBaseUrl()).append("/dest-host-ip/").append(mepHost.getMecHostIp())
            .append("/dest-host-port/").append(mepHost.getMecHostPort()).toString();
        String resp = sendHttpRequest(url, token, HttpMethod.GET, null);

        ReverseProxy reverseProxy = gson.fromJson(resp, ReverseProxy.class);

        return genProxyUrl(vncUrl, developerIp, reverseProxy.getLocalPort());
    }

    @Override
    public SshResponseInfo getVmSshResponseInfo(String applicationId, String vmId, String userId, String xsrfValue) {
        Application application = applicationService.getApplication(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostService.getHost(hostId);
        VirtualMachine vm = vmAppVmService.getVm(applicationId, vmId);
        if (vm.getVmInstantiateInfo() == null) {
            LOGGER.error("failed to get ssh console url, instantiate info does not exist.");
            throw new DeveloperException("failed to get ssh console url, instantiate info does not exist.");
        }

        PkgSpec pkgSpec = pkgSpecService.getPkgSpecById(application.getPkgSpecId());
        String defaultNetworkName = AppdConstants.NETWORK_NAME_PREFIX + pkgSpec.getSpecifications().getAppdSpecs()
            .getNetworkNameSpecs().getNetworkNameN6();
        Map<String, String> vmInputParams = InputParameterUtil.getParams(mepHost.getNetworkParameter());
        String networkName = vmInputParams.getOrDefault("APP_Plane03_Network", defaultNetworkName);
        LOGGER.info("defaultNetworkName:{}, networkName:{}", defaultNetworkName, networkName);
        List<PortInstantiateInfo> portInstantiateInfos = vm.getVmInstantiateInfo().getPortInstanceList();
        String networkIp = "";
        for (PortInstantiateInfo networkInfo : portInstantiateInfos) {
            if (networkInfo.getNetworkName().equals(networkName)) {
                networkIp = networkInfo.getIpAddress();
            }
        }
        String username = vm.getVmCertificate().getPwdCertificate().getUsername();
        String password = vm.getVmCertificate().getPwdCertificate().getPassword();
        LOGGER.info("ip:{}", networkIp);
        LOGGER.info("username:{}", username);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), 30209);
        SshResponseInfo sshResponseInfo = HttpClientUtil
            .sendWebSshRequest(basePath, networkIp, 22, username, password, xsrfValue);
        if (sshResponseInfo == null) {
            LOGGER.error("send vm WebSsh request fail.");
            throw new DeveloperException("send vm WebSsh request fail.");
        }
        if (StringUtils.isEmpty(sshResponseInfo.getId())) {
            LOGGER.error(" WebSsh info input error:{}", sshResponseInfo.getStatus());
            throw new DeveloperException("WebSsh info input error");
        }
        sshResponseInfo.setSshAddress(basePath);

        return sshResponseInfo;
    }

    @Override
    public SshResponseInfo getContainerSshResponseInfo(String applicationId, String userId, String xsrfValue) {
        ApplicationDetail application = applicationService.getApplicationDetail(applicationId);
        if (application.getContainerApp().getInstantiateInfo() == null) {
            LOGGER.error("failed to get ssh console url, container instantiate info does not exist.");
            throw new DeveloperException("failed to get ssh console url, container instantiate info does not exist.");
        }
        String hostId = application.getContainerApp().getMepHostId();
        MepHost mepHost = mepHostService.getHost(hostId);
        String username = AesUtil.decode(clientId, mepHost.getMecHostUserName());
        LOGGER.info("port:{}", mepHost.getMecHostPort());
        LOGGER.info("ip:{}", mepHost.getMecHostIp());
        LOGGER.info("username:{}", username);
        String password = AesUtil.decode(clientId, mepHost.getMecHostPassword());
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), 30209);
        SshResponseInfo sshResponseInfo = HttpClientUtil
            .sendWebSshRequest(basePath, mepHost.getMecHostIp(), mepHost.getMecHostPort(), username, password,
                xsrfValue);
        if (sshResponseInfo == null) {
            LOGGER.error("send container WebSsh request fail.");
            throw new DeveloperException("send container WebSsh request fail.");
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
        String regex = "https?://(.+):(\\d+)/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String vncUrl = url.replace(matcher.group(1), proxyHostIp)
                .replace(matcher.group(2), String.valueOf(proxyHostPort));
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
                reverseProxyBaseUrl = new StringBuffer(protocol).append("://localhost:").append(cbbPort)
                    .append("/commonservice/cbb/v1/reverseproxies").toString();
            }
        } catch (InterruptedException e) {
            LOGGER.error("failed to get the lock", e);
            Thread.currentThread().interrupt();
            throw new DeveloperException("failed to get reverse proxy base url");
        } finally {
            lock.unlock();
        }
        return reverseProxyBaseUrl;
    }
}
