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

package org.edgegallery.developer.util.samplecode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.samplecode.jsondata.JsonApiBaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleCodeServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleCodeServer.class);

    private static final String SAMPLE_HTTP_FILE_REQUEST_PATH = "sample_code_template/HttpPostFileRequest.java";

    private static final String SAMPLE_HTTP_REQUEST_PATH = "sample_code_template/HttpRequest.java";

    private static final String SAMPLE_CLASS_PATH = "sample_code_template/HelloMec.java";

    private static final String PACKAGE_NAME = "sample_code";

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * read json and generate sample code, if you have the yaml file, at firstly from yaml to json.<b/>
     * the returned file is saved in template dir.
     *
     * @param apiJsons input json, this is api interface
     * @return sample code package, it is a tgz file. if can not generate sample code, there will be null.
     */
    public File analysis(List<String> apiJsons) {
        try {
            File tempDir = DeveloperFileUtils.createTempDir("mec_sample_code");
            File srcDir = new File(tempDir, "src");
            DeveloperFileUtils.deleteAndCreateDir(srcDir);

            // create package dir
            File packageDir = new File(srcDir, "org/edgegallery/sample");
            DeveloperFileUtils.deleteAndCreateDir(packageDir);

            int index = 0;
            for (String apiJson : apiJsons) {
                JsonApiBaseBean api = gson.fromJson(apiJson, JsonApiBaseBean.class);
                api.replaceAllRefs();
                String className = String.format("ApiSampleCode%03d", ++index);
                String sampleClass = createSampleClass(api, className);
                File sampleCodeFile = new File(packageDir, className + ".java");
                FileUtils.writeStringToFile(sampleCodeFile, sampleClass, "UTF-8");
            }

            // copy request class from resource to template dir
            copyDependentFile(packageDir, SAMPLE_HTTP_REQUEST_PATH, SAMPLE_HTTP_FILE_REQUEST_PATH);

            // to package
            return CompressFileUtils.compressToTgzAndDeleteSrc(srcDir.getCanonicalPath(),
                tempDir.getCanonicalPath(), PACKAGE_NAME);
        } catch (IOException e) {
            LOGGER.error("Failed to create sample code.");
        }
        return null;
    }

    private void copyDependentFile(File outDir, String... filePaths) throws IOException {
        for (String path : filePaths) {
            try (InputStream in = SampleCodeServer.class.getClassLoader().getResourceAsStream(path);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                URL httpRequestUrl = SampleCodeServer.class.getClassLoader().getResource(path);
                File httpRequest = new File(httpRequestUrl.getFile());
                File outHttpRequest = new File(outDir, httpRequest.getName());
                String httpRequestStr = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                FileUtils.writeStringToFile(outHttpRequest, httpRequestStr, "UTF-8");
            }
        }
    }

    private String createSampleClass(JsonApiBaseBean api, String className) {
        String classTemplate = getClassTemplate();
        if (classTemplate == null) {
            return null;
        }

        List<SampleData> allApiPaths = api.getAllApiPaths();
        StringBuilder buf = new StringBuilder();
        int index = 0;
        for (SampleData sampleData : allApiPaths) {
            buf.append("\n\n");
            buf.append(sampleData.toSampleCode(++index));
        }
        return String.format(classTemplate, className, buf.toString());
    }

    private String getClassTemplate() {
        try (InputStream in = SampleCodeServer.class.getClassLoader().getResourceAsStream(SAMPLE_CLASS_PATH);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            LOGGER.error("Failed to get sample class file.");
            return null;
        }
    }
}
