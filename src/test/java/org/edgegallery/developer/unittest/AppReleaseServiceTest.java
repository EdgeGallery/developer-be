package org.edgegallery.developer.unittest;

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.AppPkgStructure;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.AppReleaseService;
import org.edgegallery.developer.service.ProjectService;
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
public class AppReleaseServiceTest {

    @Autowired
    private AppReleaseService appReleaseService;

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
    public void testGetPkgStruById() {
        Either<FormatRespDto, AppPkgStructure> stru = appReleaseService.getPkgStruById(projectId, "csarId");
        Assert.assertTrue(stru.isLeft());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruByNullProId() {
        Either<FormatRespDto, AppPkgStructure> stru = appReleaseService.getPkgStruById("", "csarId");
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruByNullPid() {
        Either<FormatRespDto, AppPkgStructure> stru = appReleaseService.getPkgStruById(null, "csarId");
        Assert.assertTrue(stru.isLeft());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruByNullCsarId() {
        Either<FormatRespDto, AppPkgStructure> stru = appReleaseService.getPkgStruById(projectId, "");
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentById() {
        Either<FormatRespDto, String> stru = appReleaseService.getPkgContentByFileName(projectId,"fileName");
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentByNullProId() {
        Either<FormatRespDto, String> stru = appReleaseService.getPkgContentByFileName("", "fileName");
        Assert.assertTrue(stru.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentByNullName() {
        Either<FormatRespDto, String> stru = appReleaseService.getPkgContentByFileName(projectId, "");
        Assert.assertTrue(stru.isLeft());
    }


}
