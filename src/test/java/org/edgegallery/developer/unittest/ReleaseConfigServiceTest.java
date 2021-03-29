/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import com.spencerwi.either.Either;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.model.CapabilitiesDetail;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.ReleaseConfigService;
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
public class ReleaseConfigServiceTest {

    @Autowired
    private ReleaseConfigService releaseConfigService;

    @Autowired
    private ProjectService projectService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    private Gson gson = new Gson();

    private String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfig() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.saveConfig(projectId, new ReleaseConfig());
        Assert.assertTrue(stru.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithProjectError() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.saveConfig("", new ReleaseConfig());
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithCsarError() {
        ReleaseConfig releaseConfig = new ReleaseConfig();
        releaseConfig.setCapabilitiesDetail(new CapabilitiesDetail());
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.saveConfig(projectId, releaseConfig);
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithFileError() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.saveConfig(null, new ReleaseConfig());
        Assert.assertTrue(stru.isLeft());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateRelConfig() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.modifyConfig("200dfab1-3c30-4fc7-a6ca-ed6f0620a85d", new ReleaseConfig());
        Assert.assertTrue(stru.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateRelConfigWithIdError() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.modifyConfig("", new ReleaseConfig());
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateRelConfigWithIdError1() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.modifyConfig("aaaa", new ReleaseConfig());
        Assert.assertTrue(stru.isLeft());
        Assert.assertEquals(400, stru.getLeft().getEnumStatus().getStatusCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateRelConfigWithCsarError() {
        ReleaseConfig releaseConfig = new ReleaseConfig();
        releaseConfig.setCapabilitiesDetail(new CapabilitiesDetail());
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.modifyConfig(projectId, releaseConfig);
        Assert.assertTrue(stru.isLeft());
        Assert.assertEquals(400, stru.getLeft().getEnumStatus().getStatusCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetRelConfig() {
        AccessUserUtil.setUser("f24ea0a2-d8e6-467c-8039-94f0d29bac43", "test-user");
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.getConfigById("200dfab1-3c30-4fc7-a6ca-ed6f0620a85d");
        Assert.assertTrue(stru.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void should_failed_when_use_userid_A_to_get_project_of_userB() {
        AccessUserUtil.setUser("otheruid-d8e6-467c-8039-94f0d29bac43", "test-user");
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.getConfigById("200dfab1-3c30-4fc7-a6ca-ed6f0620a85e");
        Assert.assertTrue(stru.isRight());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetRelConfigWithIdError() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.getConfigById("");
        Assert.assertTrue(stru.isLeft());
        Assert.assertEquals(400, stru.getLeft().getEnumStatus().getStatusCode());
    }

}
