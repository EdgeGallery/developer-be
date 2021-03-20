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

import com.spencerwi.either.Either;
import java.util.List;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.DeployService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class DeployServiceTest {

    @Autowired
    private DeployService deployService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYamlContentSuccess() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> res = deployService.getDeployYamlContent("ad66d1b6-5d29-487b-9769-be48b62aec2e");
        Assert.assertNotNull(res);
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYamJsonSuccess() throws Exception {
        Either<FormatRespDto, List<String>> res = deployService.getDeployYamJson("ad66d1b6-5d29-487b-9769-be48b62aec2h");
        Assert.assertNotNull(res);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSaveYamlWithNUllJson() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> res = deployService
            .saveDeployYaml("", "projectId", "userId", "upload");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSaveYamlSuccess() throws Exception {
        String jsonStr = "[{\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"name\":\"positioning-service\",\"namespace\":\"namespace\",\"labels\":{\"app\":\"positioning-service\"}},\"spec\":{\"containers\":[{\"name\":\"positioning\",\"image\":\"swr.ap-southeast-1.myhuaweicloud.com/edgegallery/positioning_service:1.0\",\"imagePullPolicy\":\"IfNotPresent\",\"env\":[{\"name\":\"ENABLE_WAIT\",\"value\":\"true\"}],\"ports\":[{\"containerPort\":9998}]}]}},{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"name\":\"positioning-service\",\"namespace\":\"namespace\",\"labels\":{\"svc\":\"positioning-service\"}},\"spec\":{\"ports\":[{\"port\":9997,\"targetPort\":9997,\"protocol\":\"TCP\",\"nodePort\":32115}],\"selector\":{\"app\":\"positioning-service\"},\"type\":\"NodePort\"}}]";
        Either<FormatRespDto, HelmTemplateYamlPo> res = deployService
            .saveDeployYaml(jsonStr, "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e", "f24ea0a2-d8e6-467c-8039-94f0d29bac43",
                "upload");
        Assert.assertTrue(res.isRight());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateDeployYamlSuccess() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> res = deployService.updateDeployYaml("ad66d1b6-5d29-487b-9769-be48b62aec2e","content");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYamlContentFail() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> res = deployService.getDeployYamlContent("ad66d1b6-5d29-487b-9769-be48b62aec2f");
        Assert.assertTrue(res.isLeft());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYamJsonFail() throws Exception {
        Either<FormatRespDto, List<String>> res = deployService.getDeployYamJson("ad66d1b6-5d29-487b-9769-be48b62aec2f");
        Assert.assertTrue(res.isLeft());
    }

}
