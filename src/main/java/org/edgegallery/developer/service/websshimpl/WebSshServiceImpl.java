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

package org.edgegallery.developer.service.websshimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.SshConnectInfo;
import org.edgegallery.developer.model.WebSshData;
import org.edgegallery.developer.model.deployyaml.PodStatusInfo;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.WebSshService;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.webssh.constant.ConstantPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSshServiceImpl implements WebSshService {
    //StoresshConnection informationmap
    private Map<String, Object> sshMap = new ConcurrentHashMap<>();

    private Map<String, String> userIdMap = new ConcurrentHashMap<>();

    @Value("${vm.username:}")
    private String vmUsername;

    @Value("${vm.password:}")
    private String vmPassword;

    @Value("${vm.port:}")
    private String vmPort;

    private int port;

    private String ip;

    private String username;

    private String password;

    private Logger logger = LoggerFactory.getLogger(WebSshServiceImpl.class);

    //Thread Pool
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static Gson gson = new Gson();

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Autowired
    private HostMapper hostMapper;

    @Override
    public void initConnection(WebSocketSession session) {
        JSch jsch = new JSch();
        SshConnectInfo sshConnectInfo = new SshConnectInfo();
        sshConnectInfo.setjSch(jsch);
        sshConnectInfo.setWebSocketSession(session);
        String uuid = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        //Will thissshPut the connection informationmapin
        sshMap.put(uuid, sshConnectInfo);
    }

    @Override
    public void recvHandle(String buffer, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebSshData webSshData = null;
        try {
            webSshData = objectMapper.readValue(buffer, WebSshData.class);
        } catch (IOException e) {
            logger.error("Json转换异常");
            logger.error("异常信息:{}", e.getMessage());
            return;
        }
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webSshData.getOperate())) {
            //Find the one you just savedsshConnection object
            SshConnectInfo sshConnectInfo = (SshConnectInfo) sshMap.get(userId);
            //Start thread asynchronous processing
            WebSshData finalWebSshData = webSshData;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        connectToSsh(sshConnectInfo, finalWebSshData, session);
                    } catch (JSchException | IOException e) {
                        logger.error("webssh连接异常");
                        logger.error("异常信息:{}", e.getMessage());
                        close(session);
                    }
                }
            });
        } else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webSshData.getOperate())) {
            String command = webSshData.getCommand();
            SshConnectInfo sshConnectInfo = (SshConnectInfo) sshMap.get(userId);
            if (sshConnectInfo != null) {
                try {
                    transToSsh(sshConnectInfo.getChannel(), command);
                } catch (IOException e) {
                    logger.error("webssh连接异常");
                    logger.error("异常信息:{}", e.getMessage());
                    close(session);
                }
            }
        } else {
            logger.error("不支持的操作");
            close(session);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        String userId = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        SshConnectInfo sshConnectInfo = (SshConnectInfo) sshMap.get(userId);
        if (sshConnectInfo != null) {
            //Disconnect
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            //mapRemove
            sshMap.remove(userId);
        }
    }

    private void connectToSsh(SshConnectInfo sshConnectInfo, WebSshData webSshData, WebSocketSession webSocketSession)
        throws JSchException, IOException {

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        //Obtainjsch'S conversation
        //ObtainuserIDwithprojectId
        String userId = webSshData.getUserId();
        String projectId = webSshData.getProjectId();
        String uuid = String.valueOf(webSocketSession.getAttributes().get(ConstantPool.USER_UUID_KEY));
        userIdMap.put(userId, uuid);
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
            List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
            if (CollectionUtils.isEmpty(testConfigList)) {
                logger.info("This project has not test config.");
                return;
            }
            ProjectTestConfig testConfig = testConfigList.get(0);
            if (testConfig.getHosts() == null) {
                return;
            }
            Type type = new TypeToken<List<MepHost>>() { }.getType();
            List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
            MepHost host = hostMapper.getHost(hosts.get(0).getHostId());
            this.port = host.getVncPort();
            this.ip = host.getLcmIp();
            this.username = host.getUserName();
            this.password = host.getPassword();
        } else {
            VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
            if (vmCreateConfig == null) {
                logger.info("This project has not vm create config.");
                return;
            }
            if (vmCreateConfig.getStatus() != EnumVmCreateStatus.SUCCESS) {
                logger.info("the vm is creating or create fail.");
                return;
            }
            Type hostType = new TypeToken<MepHost>() { }.getType();
            MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), hostType);
            Map<String, String> vmInputParams = InputParameterUtil.getParams(host.getParameter());

            String networkName = vmInputParams.getOrDefault("network_name_n6", "mec_network_n6");
            Type type = new TypeToken<List<VmInfo>>() { }.getType();
            List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);
            List<NetworkInfo> networkInfos = vmInfo.get(0).getNetworks();
            String networkIp = "";
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo.getName().equals(networkName)) {
                    networkIp = networkInfo.getIp();
                }
            }
            logger.info("shh info: {},{},{}", networkIp, vmPort, vmUsername);
            this.port = Integer.parseInt(vmPort);
            this.ip = networkIp;
            this.username = vmUsername;
            this.password = vmPassword;
        }
        Session session = sshConnectInfo.getjSch().getSession(this.username, this.ip, this.port);
        session.setConfig(config);
        //set password
        session.setPassword(this.password);
        //connection  overtime time30s
        session.connect(30000);

        //Turn onshellaisle
        Channel channel = session.openChannel("shell");

        //Channel connection overtime time60s
        channel.connect(60000);

        //Set upchannel
        sshConnectInfo.setChannel(channel);
        if (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
            List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
            ProjectTestConfig testConfig = testConfigList.get(0);
            if (testConfig == null || StringUtils.isEmpty(testConfig.getPods())) {
                logger.warn("testconfig is null or don't have pods!");
                return;
            }
            String pods = testConfig.getPods();
            Type type = new TypeToken<PodStatusInfos>() { }.getType();
            PodStatusInfos podStatusInfos = gson.fromJson(pods, type);
            List<PodStatusInfo> list = podStatusInfos.getPods();
            if (CollectionUtils.isEmpty(list)) {
                logger.warn("pods is empty!");
                return;
            }
            String podName = list.get(0).getPodname();
            if (StringUtils.isEmpty(podName)) {
                logger.warn("podName in pods is empty!");
                return;
            }
            String[] eventsInfo = list.get(0).getPodEventsInfo();
            if (eventsInfo == null || eventsInfo.length == 0) {
                logger.warn("eventsInfo in pods is empty!");
                return;
            }
            String namespace = "";
            for (String event : eventsInfo) {
                if (event.contains("Successfully")) {
                    String[] events = event.split(" ");
                    String[] names = events[2].split("/");
                    namespace = names[0];
                }
            }
            if (namespace.equals("") && !list.get(0).getPodstatus().equals("Running")) {
                logger.warn("namespace in pods is empty!");
                return;
            }
            String enterPodCommand = "kubectl exec -it " + podName + " -n " + namespace + " -- sh";
            transToSsh(channel, enterPodCommand);
            transToSsh(channel, "\r");
        } else {
            //Forward message
            transToSsh(channel, "\r");
        }

        //Read the information flow returned by the terminal
        InputStream inputStream = channel.getInputStream();
        BufferedReader br = null;
        InputStreamReader ir = null;
        try {
            //Loop reading
            // byte[] buffer = new byte[1024];
            // int i = 0;
            br = new BufferedReader(ir);
            //If there is no data to come，The thread will always be blocked in this place waiting for data。
            String readContent;
            while ((readContent = br.readLine()) != null) {
                //  logger.warn(inputStream);
                logger.warn("read byte array to String: {}", readContent);
                // byte[] readBuffer = Arrays.copyOfRange(buffer, 0, i);
                // logger.warn("read byte array length: {}", readBuffer.length);
                // String toStr = new String(readBuffer, StandardCharsets.UTF_8);
                // logger.warn("read byte array to String: {}", toStr);
                //sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
                sendMessage(webSocketSession, readContent.getBytes(StandardCharsets.UTF_8));
            }

        } finally {
            //Close the session after disconnecting
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
            }
            if (br != null) {
                br.close();
            }
            if (ir != null) {
                ir.close();
            }
        }

    }

    private void transToSsh(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes("UTF-8"));
            outputStream.flush();
        }
    }

    @Override
    public Map<String, Object> getSshMap() {
        return sshMap;
    }

    @Override
    public Map<String, String> getUserIdMap() {
        return userIdMap;
    }
}
