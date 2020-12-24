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
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.application.plugin.PluginService;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.dao.ProjectDao;
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
public class ProjectDaoTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private ProjectDao projectDao;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Test
    public void testDeleteProject() {
        Either<FormatRespDto, Boolean> res = projectDao.deleteProject("test", "test1");
        Assert.assertTrue(res.isRight());
    }

    @Test
    public void testDeleteProjectBad() {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85r";
        Either<FormatRespDto, Boolean> res = projectDao.deleteProject(userId, projectId);
        Assert.assertTrue(res.isRight());
    }

    @Test
    public void testDeleteProjectBad1() {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85t";
        Either<FormatRespDto, Boolean> res = projectDao.deleteProject(userId, projectId);
        Assert.assertTrue(res.isRight());
    }

    @Test
    public void testDeleteProjectBad2() {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85l";
        Either<FormatRespDto, Boolean> res = projectDao.deleteProject(userId, projectId);
        Assert.assertTrue(res.isRight());
    }

    @Test
    public void testDeleteProjectBad3() {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85y";
        Either<FormatRespDto, Boolean> res = projectDao.deleteProject(userId, projectId);
        Assert.assertTrue(res.isRight());
    }

}
