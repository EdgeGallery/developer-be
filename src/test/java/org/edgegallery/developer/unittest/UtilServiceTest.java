package org.edgegallery.developer.unittest;

import com.spencerwi.either.Either;
import java.util.HashMap;
import java.util.Map;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.UtilsService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class UtilServiceTest {

    @Autowired
    private UtilsService utilsService;

    @Test(expected = ResourceAccessException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStore() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("test","test");
        String userId = "user-id";
        String userName = "user-name";
        String token = "access-token";
        Either<FormatRespDto, String> either =  utilsService.storeToAppStore(map,userId,userName,token);
        Assert.assertTrue(either.isLeft());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStoreWithNullMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        String userId = "user-id";
        String userName = "user-name";
        String token = "access-token";
        Either<FormatRespDto, String> either =  utilsService.storeToAppStore(map,userId,userName,token);
        Assert.assertTrue(either.isLeft());

    }

}
