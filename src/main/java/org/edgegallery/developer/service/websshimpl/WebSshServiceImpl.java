package org.edgegallery.developer.service.websshimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.SshConnectInfo;
import org.edgegallery.developer.model.WebSshData;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.WebSshService;
import org.edgegallery.developer.util.webssh.constant.ConstantPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSshServiceImpl implements WebSshService {
    //存放ssh连接信息的map
    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();

    private static int PORT = 22;

    private String ip;

    private String username;

    private String password;

    private Logger logger = LoggerFactory.getLogger(WebSshServiceImpl.class);

    //线程池
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static Gson gson = new Gson();

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Override
    public void initConnection(WebSocketSession session) {
        JSch jSch = new JSch();
        SshConnectInfo sshConnectInfo = new SshConnectInfo();
        sshConnectInfo.setjSch(jSch);
        sshConnectInfo.setWebSocketSession(session);
        String uuid = String.valueOf(session.getAttributes().get(ConstantPool.USER_UUID_KEY));
        //将这个ssh连接信息放入map中
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
            //找到刚才存储的ssh连接对象
            SshConnectInfo sshConnectInfo = (SshConnectInfo) sshMap.get(userId);
            //启动线程异步处理
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
            //断开连接
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            //map中移除
            sshMap.remove(userId);
        }
    }

    private void connectToSsh(SshConnectInfo sshConnectInfo, WebSshData webSshData, WebSocketSession webSocketSession)
        throws JSchException, IOException {

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        //获取jsch的会话

        //获取userID和projectId
        String userId = webSshData.getUserId();
        String projectId = webSshData.getProjectId();
        System.out.println(userId + "--" + projectId);
        //        String username ="";
        //        String host = "";
        //        String password = "";

        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
            List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
            if (CollectionUtils.isEmpty(testConfigList)) {
                logger.info("This project has not test config.");
                return;
            }
            ProjectTestConfig testConfig = testConfigList.get(0);
            Type type = new TypeToken<List<MepHost>>() { }.getType();
            List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
            MepHost host = hosts.get(0);
            this.ip = host.getLcmIp();
            this.username = host.getUserName();
            this.password = host.getPassword();
        } else {
            List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
            if (CollectionUtils.isEmpty(vmCreateConfigs)) {
                logger.info("This project has not vm create config.");
                return;
            }
            VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);
            if (vmCreateConfig.getStatus() != EnumVmCreateStatus.SUCCESS) {
                logger.info("the vm is creating or create fail.");
                return;
            }
            Type type = new TypeToken<List<VmInfo>>() { }.getType();
            List<VmInfo> vmInfos = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);
            VmInfo vmInfo = vmInfos.get(0);
//            this.ip = vmInfo.getVncUrl();
            logger.info("the vm ip: 192.168.233.34.");
            this.ip = "192.168.233.34";
            this.username = "root";
            this.password = "123456";
        }
        Session session = sshConnectInfo.getjSch().getSession(this.username, this.ip, PORT);
        session.setConfig(config);
        //设置密码
        session.setPassword(this.password);
        //连接  超时时间30s
        session.connect(30000);

        //开启shell通道
        Channel channel = session.openChannel("shell");

        //通道连接 超时时间3s
        channel.connect(3000);

        //设置channel
        sshConnectInfo.setChannel(channel);

        //转发消息
        transToSsh(channel, "\r");

        //读取终端返回的信息流
        InputStream inputStream = channel.getInputStream();
        try {
            //循环读取
            byte[] buffer = new byte[1024];
            int i = 0;
            //如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }

        } finally {
            //断开连接后关闭会话
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
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
}
