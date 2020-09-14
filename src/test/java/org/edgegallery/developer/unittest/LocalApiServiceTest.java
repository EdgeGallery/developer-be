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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SwaggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class LocalApiServiceTest {
    @Autowired
    private SwaggerService swaggerService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllSwaggerUI() {
        Either<FormatRespDto, ResponseEntity<byte[]>> result = swaggerService.getFile("plugin");
        Assert.assertTrue(result.isRight());
        Either<FormatRespDto, ResponseEntity<byte[]>> badResult = swaggerService.getFile("plugin001");
        Assert.assertEquals(400, badResult.getLeft().getErrorRespDto().getCode());
    }

}
