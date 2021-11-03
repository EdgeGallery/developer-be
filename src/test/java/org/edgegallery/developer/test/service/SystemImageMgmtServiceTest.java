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
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SystemImageMgmtService;
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
public class SystemImageMgmtServiceTest {

    @Autowired
    private SystemImageMgmtService systemImageMgmtService;

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
    public void testGetSystemImage() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        MepGetSystemImageReq mepGetSystemImageReq = new MepGetSystemImageReq();
        MepSystemQueryCtrl queryCtrl = new MepSystemQueryCtrl();
        mepGetSystemImageReq.setQueryCtrl(queryCtrl);
        Either<FormatRespDto, MepGetSystemImageRes> res = systemImageMgmtService
                .getSystemImages(mepGetSystemImageReq);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateSystemImageRight() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee4", "admin");
        VmSystem vmSystem = new VmSystem();
        vmSystem.setSystemId(02);
        vmSystem.setSystemBit("64");
        vmSystem.setSystemDisk(40);
        vmSystem.setSystemName("testImage02");
        vmSystem.setOperateSystem("ubuntu");
        vmSystem.setVersion("16");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.createSystemImage(vmSystem);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateSystemImageNoSystemName() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee4", "admin");
        VmSystem vmSystem = new VmSystem();
        vmSystem.setSystemId(02);
        vmSystem.setSystemBit("64");
        vmSystem.setSystemDisk(40);
        vmSystem.setOperateSystem("ubuntu");
        vmSystem.setVersion("16");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.createSystemImage(vmSystem);
        Assert.assertTrue(res.getLeft().getErrorRespDto().getDetail().equalsIgnoreCase("Can not create a SystemImage."));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateSystemImageDuplicateSystemName() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        VmSystem vmSystem = new VmSystem();
        vmSystem.setSystemId(02);
        vmSystem.setSystemBit("64");
        vmSystem.setSystemDisk(40);
        vmSystem.setOperateSystem("ubuntu");
        vmSystem.setSystemName("testImage");
        vmSystem.setVersion("16");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.createSystemImage(vmSystem);
        Assert.assertTrue(res.getLeft().getErrorRespDto().getDetail().equalsIgnoreCase("SystemName can not duplicate."));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateSystemImageRight() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        VmSystem vmSystem = new VmSystem();
        vmSystem.setSystemId(12345);
        vmSystem.setSystemBit("64");
        vmSystem.setSystemDisk(40);
        vmSystem.setOperateSystem("ubuntu");
        vmSystem.setSystemName("testImage");
        vmSystem.setVersion("14");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.updateSystemImage(vmSystem,12345);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateSystemImageDuplicateSystemName() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        VmSystem vmSystem = new VmSystem();
        vmSystem.setSystemId(12345);
        vmSystem.setSystemBit("64");
        vmSystem.setSystemDisk(40);
        vmSystem.setOperateSystem("ubuntu");
        vmSystem.setSystemName("testImage01");
        vmSystem.setVersion("16");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.updateSystemImage(vmSystem,12345);
        Assert.assertTrue(res.getLeft().getErrorRespDto().getDetail().equalsIgnoreCase("SystemName can not duplicate."));;
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testPublishSystemImageRight() throws Exception {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.publishSystemImage(12345);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleleSystemImageRight() throws Exception {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee6", "admin", "ROLE_DEVELOPER_ADMIN");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.deleteSystemImage(12345);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleleSystemImageERROR() throws Exception {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        Either<FormatRespDto, Boolean> res = systemImageMgmtService.deleteSystemImage(32145);
        Assert.assertTrue(res.isLeft());
    }
}
