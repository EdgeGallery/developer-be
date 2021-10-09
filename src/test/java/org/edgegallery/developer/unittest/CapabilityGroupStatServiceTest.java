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

import java.io.IOException;
import java.util.List;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;
import org.edgegallery.developer.service.capability.CapabilityGroupStatService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class CapabilityGroupStatServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityGroupStatServiceTest.class);

    @Autowired
    private CapabilityGroupStatService statService;

    @Test
    public void testFindAllSuccess() throws IOException {
        List<CapabilityGroupStat> response = statService.findAll();
        Assert.assertNotNull(response);
    }

    @Test
    public void testFindByTypeSuccess() throws IOException {
        List<CapabilityGroupStat> response = statService.findByType("OPENMEP");
        Assert.assertNotNull(response);
    }



}
