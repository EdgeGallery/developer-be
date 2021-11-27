package org.edgegallery.developer.service.proxy.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.resource.mephost.MepHostMapper;
import org.edgegallery.developer.mapper.reverseproxy.ReverseProxyMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.reverseproxy.ReverseProxy;
import org.edgegallery.developer.model.lcm.VmInfo;
import org.edgegallery.developer.model.lcm.VmInstantiateWorkload;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.RuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReverseProxyServiceImpl implements ReverseProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyService.class);

    private static final String BASE_CONFIG_FILE = "/reverse_proxy/nginx.conf";

    private static final String NGINX_CONFIG_FILE_SUFFIX = ".conf";

    private static final String NGINX_CONFIG_DIR = "/etc/nginx/conf.d/";

    private static final int MIN_REVERSE_PROXY_PORT = 30111;

    private static final int MAX_REVERSE_PROXY_PORT = 30120;

    @Autowired
    private ReverseProxyMapper reverseProxyMapper;

    @Autowired
    private MepHostMapper mepHostMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private VMAppOperationService vmAppOperationService;

    private static Gson gson = new Gson();

    @Value("${developer.ip:}")
    private String developerIp;

    /**
     * add reverse proxy
     * @param hostId
     * @param hostConsolePort vnc port
     */
    @Override
    public void addReverseProxy(String hostId, int hostConsolePort) {
        MepHost mepHost = mepHostMapper.getHost(hostId);
        String mepHostIp = mepHost.getMecHostIp();
        int proxyHostPort = getUsableProxyPort();

        // add nginx config first
        addReverseProxyConfigFile(proxyHostPort, mepHostIp, hostConsolePort);
        reloadNginxConfig();
        ReverseProxy reverseProxy = new ReverseProxy(UUID.randomUUID().toString(), hostId,
                hostConsolePort, proxyHostPort, ReverseProxy.TYPE_OPENSTACK_VNC);

        // if insert data failed, nginx config should be roll back.
        if (reverseProxyMapper.createReverseProxy(reverseProxy) != 1) {
            LOGGER.error("failed to insert reverse proxy data, nginx config will be roll back! proxy data : {}",
                    reverseProxy.toString());
            deleteReverseProxyConfigFile(proxyHostPort);
            reloadNginxConfig();
            throw new DeveloperException("failed to insert reverse proxy data");
        }

    }

    @Override
    public void deleteReverseProxy(String hostId) {
        List<ReverseProxy> reverseProxyList = reverseProxyMapper.getProxiesByDestHostId(hostId);
        if (reverseProxyList.isEmpty()) {
            LOGGER.info("the proxy was already deleted, hostId : {}", hostId);
            return;
        }

        // delete database first
        reverseProxyMapper.deleteProxyByDestHostId(hostId);

        // delete nginx config
        for (ReverseProxy reverseProxy : reverseProxyList) {
            deleteReverseProxyConfigFile(reverseProxy.getProxyPort());
        }
        reloadNginxConfig();
    }

    @Override
    public String getVmConsoleUrl(String applicationId, String vmId, String userId, String token) {
        Application application = applicationMapper.getApplicationById(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostMapper.getHost(hostId);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        if (instantiateInfo==null) {
            LOGGER.error("failed to get vnc console url, instantiate info does not exist.");
            throw new DeveloperException("failed to get vnc console url");
        }
        String workLoadStatus = HttpClientUtil.getWorkloadStatus(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
                mepHost.getLcmPort(), instantiateInfo.getAppInstanceId(), userId, token);
        LOGGER.info("get vm workLoad status:{}", workLoadStatus);
        Type vmInfoType = new TypeToken<VmInstantiateWorkload>() { }.getType();
        VmInstantiateWorkload vmInstantiateWorkload = gson.fromJson(workLoadStatus, vmInfoType);
        if (vmInstantiateWorkload == null || !Consts.HTTP_STATUS_SUCCESS_STR.equals(vmInstantiateWorkload.getCode())) {
            LOGGER.error("failed to get vnc console url, http request error happened.");
            throw new DeveloperException("failed to get vnc console url");
        }

        List<VmInfo> vmInfos = vmInstantiateWorkload.getData();
        if (vmInfos == null || vmInfos.isEmpty()) {
            LOGGER.error("failed to get vnc console url, http request error happened.");
            throw new DeveloperException("failed to get vnc console url");
        }
        String vndUrl = vmInfos.get(0).getVncUrl();

        List<ReverseProxy> reverseProxyList = reverseProxyMapper.getProxiesByDestHostId(hostId);
        if (reverseProxyList == null || reverseProxyList.isEmpty()){
            LOGGER.error("reverse proxy has not been configured. hostId : {}, aplicationId : {}",
                hostId, applicationId);
            throw new DeveloperException("reverse proxy has not been configured.");
        }

        return genProxyUrl(vndUrl, developerIp, reverseProxyList.get(0).getProxyPort());
    }

    private String genProxyUrl(String url, String proxyHostIp, int proxyHostPort) {
        String regex = "(\\d{1,3}(.\\d{1,3}){3}):(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            int count = matcher.groupCount();
            String vncUrl = url.replace(matcher.group(1), proxyHostIp).replace(matcher.group(3),
                    String.valueOf(proxyHostPort));
            LOGGER.debug("vnc url : {}, origin url : {}", vncUrl, url);
            return vncUrl;
        }
        LOGGER.error("failed to generate proxy url from {}", url);
        throw new DeveloperException("failed to generate proxy url");
    }

    private void reloadNginxConfig() {
        List<String> command = Arrays.asList("nginx", "-s", "reload");
        try {
            String cmdResult = RuntimeUtil.execCommand(command);
            if (!cmdResult.contains("SUCCESS")) {
                LOGGER.error("failed to reload nginx config, cmd result : {}", cmdResult);
                throw new DeveloperException("failed to reload nginx config");
            }
        } catch (IOException e) {
            LOGGER.error("failed to reload nginx config.", e);
            throw new DeveloperException("failed to reload nginx config");
        }
    }

    private int getUsableProxyPort() {
        Set<Integer> usedPorts = reverseProxyMapper.getAllVncProxyPorts();
        for (int i = MIN_REVERSE_PROXY_PORT; i <= MAX_REVERSE_PROXY_PORT; i++) {
            if (!usedPorts.contains(i)) {
                LOGGER.debug("proxy port {} is usable.", i);
                return i;
            }
        }
        LOGGER.error("no usable proxy port left.");
        throw new DeveloperException("there is no usable proxy port left.");
    }

    private void addReverseProxyConfigFile(int proxyPort, String hostIp, int hostConsolePort){
        try {
            InputStream is = ReverseProxyServiceImpl.class.getResourceAsStream(BASE_CONFIG_FILE);
            Charset charset = Charset.forName(Consts.FILE_ENCODING);
            String content = StreamUtils.copyToString(is, charset);
            String url = new StringBuffer("http://").append(hostIp).append(":").append(hostConsolePort).toString();
            String nginxConfig = String.format(content, proxyPort, url);
            File file = new File(NGINX_CONFIG_DIR + proxyPort + NGINX_CONFIG_FILE_SUFFIX);
            FileUtils.writeStringToFile(file, nginxConfig, charset);
            LOGGER.info("reverse proxy config file {} is created.", file.getName());
        } catch(IOException e) {
            LOGGER.error("failed to make reverse proxy conf", e);
            throw new DeveloperException("failed to make reverse proxy conf.");
        }
    }

    private void deleteReverseProxyConfigFile(int proxyPort) {
        File file = new File(NGINX_CONFIG_DIR + proxyPort + NGINX_CONFIG_FILE_SUFFIX);
        if (file.exists() && !file.delete()) {
            LOGGER.error("failed to delete reverse config file {}.", file.getName());
            throw new DeveloperException("failed to delete config file.");
        }
        LOGGER.info("reverse proxy file {} is deleted.", file.getName());
    }


}
