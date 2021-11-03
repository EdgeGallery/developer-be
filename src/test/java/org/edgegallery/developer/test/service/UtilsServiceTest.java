/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.test.service;

import com.spencerwi.either.Either;
import java.util.HashMap;
import java.util.Map;
import org.edgegallery.developer.test.DeveloperApplicationTests;
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
public class UtilsServiceTest {

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
