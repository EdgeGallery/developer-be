/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.uploadfile.GeneralConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private RuntimeUtil() {
        throw new IllegalStateException("RuntimeUtil class");
    }

    /**
     * execCommand.
     */
    public static String execCommand(List<String> cmd) throws IOException {
        StringBuilder builder = new StringBuilder();
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = null;
        BufferedReader br = null;
        try {
            p = pb.start();
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "gbk"));
            String line = null;
            LOGGER.info("Invoke shell: {}", StringUtils.join(cmd, " "));
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                builder.append(line);
            }

            if (p.isAlive()) {
                p.waitFor();
            }
            builder.append("SUCCESS");//作为判断命令输出流是标准输出还是错误输出
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            builder.append(e.getMessage()).append("FAILED");
        } finally {
            if (br != null) {
                br.close();
            }
            if (p != null) {
                p.destroy();
            }

        }
        return builder.toString();
    }

    /**
     * buildCommand.
     */
    public static List<String> buildCommand(String lan, GeneralConfig config) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(InitConfigUtil.getSdkCodeDir() + "swagger-codegen-cli-3.0.21.jar");
        command.add("generate");
        command.add("-i");
        command.add("/usr/app" + config.getInputSpec());
        command.add("-l");
        command.add(lan);
        command.add("-o");
        command.add("/usr/app" + config.getOutput() + config.getProjectName());

        //Different configurations according to different languages
        switch (lan) {
            case "java":
                buildJavaCommand(command, config);
                break;
            case "go":
                break;
            default:
                break;
        }

        return command;
    }

    /**
     * buildJavaCommand.
     */
    private static void buildJavaCommand(List<String> command, GeneralConfig config) {
        command.add("--api-package");
        command.add(config.getApiPackage());
        command.add("--invoker-package");
        command.add(config.getInvokerPackage());
        //Generated data modeljavaFile package name
        command.add("--model-package");
        command.add(config.getModelPackage());

        command.add("--artifact-id");
        command.add(config.getArtifactId());
        command.add("--artifact-version");
        command.add(config.getArtifactVersion());
        command.add("--group-id");
        command.add(config.getGroupId());
    }

}
