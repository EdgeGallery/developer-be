package org.edgegallery.developer.unittest;

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.DeveloperApplicationTests;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.modifyConfig(projectId, new ReleaseConfig());
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
    public void testGetRelConfig() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.getConfigById("200dfab1-3c30-4fc7-a6ca-ed6f0620a85e");
        Assert.assertTrue(stru.isRight());
        Assert.assertNotNull(stru.getRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetRelConfigWithIdError() {
        Either<FormatRespDto, ReleaseConfig> stru = releaseConfigService.getConfigById("");
        Assert.assertTrue(stru.isLeft());
        Assert.assertEquals(400, stru.getLeft().getEnumStatus().getStatusCode());
    }

}
