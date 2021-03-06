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

package org.edgegallery.developer.service.csar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

@Getter
@Setter
public class CreateCsarFileHanlder implements ICsarFileHanlder {

    private static final String[] loopFiles = {
        "pod.yaml", "service.yaml", "configmap.yaml"
    };

    private static final String START = "---IMAGE_LOOP_START---";

    private static final String END = "---IMAGE_LOOP_END---";

    private String filePath;

    private Map<String, String> fillData = new HashMap<>();

    /**
     * execute.
     *
     * @return
     */
    @Override
    public void execute() throws IOException {
        try {
            File f = new File(filePath);
            String fileToString = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : fillData.entrySet()) {
                fileToString = fileToString.replaceAll(entry.getKey(), entry.getValue());
            }
            FileUtils.writeStringToFile(f, fileToString, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new IOException("replace file exception");
        }
    }
}
