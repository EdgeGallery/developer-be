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

package org.edgegallery.developer.unittest;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.util.samplecode.SampleCodeServer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class CreateSampleCodeTest {

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateSampleCodeFromYaml() throws IOException, DomainException {
        SampleCodeServer service = new SampleCodeServer();

        String[] yamlFiles = {"testdata/yaml/user-mgmt-be-v1.yaml", "testdata/yaml/projects-v1.yaml"};
        List<String> jsons = new ArrayList<>();
        for (String yamlPath : yamlFiles) {
            try (InputStream input = CreateSampleCodeTest.class.getClassLoader().getResourceAsStream(yamlPath)) {
                Yaml yaml = new Yaml(new SafeConstructor());
                Map<String, Object> loaded;
                try {
                    loaded = yaml.load(input);
                } catch (DomainException e) {
                    throw new DomainException("Yaml deserialization failed");
                }
                String apiJson = new Gson().toJson(loaded);
                jsons.add(apiJson);
            }
        }
        File tar = service.analysis(jsons);
        Assert.assertTrue(tar.exists());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateSampleCodeFromJson() throws IOException {
        SampleCodeServer service = new SampleCodeServer();
        String[] jsonFiles = {"testdata/json/Location API HOST.json"};
        List<String> jsons = new ArrayList<>();
        for (String jsonPath : jsonFiles) {
            try {
                URL url = CreateSampleCodeTest.class.getClassLoader().getResource(jsonPath);
                String apiJson = FileUtils.readFileToString(new File(url.toURI()), "UTF-8");
                jsons.add(apiJson);
            } catch (URISyntaxException e) {
                Assert.fail("read test file error.");
            }
        }
        File tar = service.analysis(jsons);
        Assert.assertTrue(tar.exists());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void should_successful_when_gen_code_from_openapi_file() throws IOException {
        SampleCodeServer service = new SampleCodeServer();
        String[] jsonFiles = {"testdata/json/540e0817-f6ea-42e5-8c5b-cb2daf9925a3"};
        List<String> jsons = new ArrayList<>();
        for (String jsonPath : jsonFiles) {
            try {
                URL url = CreateSampleCodeTest.class.getClassLoader().getResource(jsonPath);
                String apiJson = FileUtils.readFileToString(new File(url.toURI()), "UTF-8");
                jsons.add(apiJson);
            } catch (URISyntaxException e) {
                Assert.fail("read test file error.");
            }
        }
        File tar = service.analysis(jsons);
        Assert.assertTrue(tar.exists());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void should_successful_when_gen_code_from_yaml_file() throws DomainException {
        SampleCodeServer service = new SampleCodeServer();
        String[] yamlFiles = {"testdata/yaml/9f1f13a0-8554-4dfa-90a7-d2765238fca7"};
        List<String> jsons = new ArrayList<>();
        for (String yamlPath : yamlFiles) {
            try {
                InputStream input = CreateSampleCodeTest.class.getClassLoader().getResourceAsStream(yamlPath);
                Yaml yaml = new Yaml(new SafeConstructor());
                Map<String, Object> loaded = yaml.load(input);
                String apiJson = new Gson().toJson(loaded);
                jsons.add(apiJson);
            } catch (DomainException e) {
                throw new DomainException("Yaml deserialization failed");
            }
        }

        File tar = service.analysis(jsons);
        Assert.assertTrue(tar.exists());
    }
}
