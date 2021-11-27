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

package org.edgegallery.developer.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.edgegallery.developer.model.uploadfile.FileUploadEntity;
import org.edgegallery.developer.model.reverseproxy.ScpConnectEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

@Configuration
public class ShhFileUploadUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShhFileUploadUtil.class);

    /**
     * uploadFile.
     */
    @Async
    public FileUploadEntity uploadFile(File file, String remoteFileName, ScpConnectEntity scpConnectEntity) {
        String code = null;
        String message = null;
        try {
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("please upload file is not null！");
            }
            if (remoteFileName == null || "".equals(remoteFileName.trim())) {
                throw new IllegalArgumentException("The new file name of the remote server cannot be empty!");
            }
            remoteUploadFile(scpConnectEntity, file, remoteFileName);
            code = "ok";
            message = remoteFileName;
        } catch (IllegalArgumentException e) {
            code = "Exception";
            message = e.getMessage();
        } catch (JSchException e) {
            code = "Exception";
            message = e.getMessage();
        } catch (IOException e) {
            code = "Exception";
            message = e.getMessage();
        } catch (Exception e) {
            throw e;
        } catch (Error e) {
            code = "Error";
            message = e.getMessage();
        }
        return new FileUploadEntity(code, message, null);
    }

    private void remoteUploadFile(ScpConnectEntity scpConnectEntity, File file, String remoteFileName)
        throws JSchException, IOException {
        LOGGER.info("start remote upload file.");
        Connection connection = null;
        ch.ethz.ssh2.Session session = null;
        SCPOutputStream scpo = null;
        FileInputStream fis = null;
        try {
            createDir(scpConnectEntity);
        } catch (JSchException e) {
            LOGGER.error("create directory failed, {}", e.getMessage());
            throw e;
        }

        try {
            connection = new Connection(scpConnectEntity.getUrl());
            connection.connect();
            if (!connection.authenticateWithPassword(scpConnectEntity.getUserName(), scpConnectEntity.getPassWord())) {
                LOGGER.error("connect failed on authentication.");
                throw new RuntimeException("connect failed on authentication.");
            }

            LOGGER.info("file length = " + file.length());
            session = connection.openSession();
            SCPClient scpClient = connection.createSCPClient();
            scpo = scpClient.put(remoteFileName, file.length(), scpConnectEntity.getTargetPath(), "0666");
            fis = new FileInputStream(file);
            byte[] buf = new byte[10240];
            int dataSize = fis.read(buf);
            while (dataSize != -1) {
                scpo.write(buf);
                scpo.flush();
                dataSize = fis.read(buf);
            }
        } catch (IOException e) {
            LOGGER.error("upload file failed, {}", e.getMessage());
            throw e;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("close fis failed, {}", e.getMessage());
                }
            }
            if (null != scpo) {
                try {
                    scpo.close();
                } catch (IOException e) {
                    LOGGER.error("close scpo failed, {}", e.getMessage());
                }
            }
            if (null != session) {
                session.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
    }

    private boolean createDir(ScpConnectEntity scpConnectEntity) throws JSchException {

        JSch jsch = new JSch();
        com.jcraft.jsch.Session sshSession = null;
        Channel channel = null;
        try {
            sshSession = jsch.getSession(scpConnectEntity.getUserName(), scpConnectEntity.getUrl(), 22);
            sshSession.setPassword(scpConnectEntity.getPassWord());
            sshSession.setConfig("StrictHostKeyChecking", "no");
            sshSession.connect();
            channel = sshSession.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            throw new JSchException("SFTP连接服务器失败" + e.getMessage());
        }
        ChannelSftp channelSftp = (ChannelSftp) channel;
        if (isDirExist(scpConnectEntity.getTargetPath(), channelSftp)) {
            channel.disconnect();
            channelSftp.disconnect();
            sshSession.disconnect();
            return true;
        } else {
            String[] pathArry = scpConnectEntity.getTargetPath().split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                try {
                    if (isDirExist(filePath.toString(), channelSftp)) {
                        channelSftp.cd(filePath.toString());
                    } else {
                        // Create a catalog
                        channelSftp.mkdir(filePath.toString());
                        // Enter and set as the current directory
                        channelSftp.cd(filePath.toString());
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                    throw new JSchException("SFTP无法正常操作服务器" + e.getMessage());
                }
            }
        }
        channel.disconnect();
        channelSftp.disconnect();
        sshSession.disconnect();
        return true;
    }

    private boolean isDirExist(String directory, ChannelSftp channelSftp) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpAttrs = channelSftp.lstat(directory);
            isDirExistFlag = true;
            return sftpAttrs.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }
}