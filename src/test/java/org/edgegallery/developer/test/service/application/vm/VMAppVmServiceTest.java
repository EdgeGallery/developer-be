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

package org.edgegallery.developer.test.service.application.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.model.application.vm.PwdCertificate;
import org.edgegallery.developer.model.application.vm.VMCertificate;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
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
public class VMAppVmServiceTest {

    private final String PRESET_APPLICATION_ID = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";

    private final String PRESET_VM1_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d757";

    private final String PRESET_VM2_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d758";

    private final String PRESET_VM_NAME = "appvm1";

    private final long PRESET_VM_PORT_SIZE = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppVmServiceTest.class);

    @Autowired
    private VMAppVmService vmAppVmService;

    @Test
    public void testCreateVMSuccess() {
        VirtualMachine vm = new VirtualMachine();
        vm.setId("6a75a2bd-9811-432f-bbe8-2813aa97d888");
        vm.setName("test vm");
        vm.setFlavorId("3ef2bea0-5e23-4fab-952d-cc9e6741dbe7");
        vm.setImageId(1);
        VMCertificate vmCertificate = new VMCertificate();
        vmCertificate.setCertificateType(VMCertificate.CERTIFICATE_TYPE_PWD);
        PwdCertificate pwdCertificate = new PwdCertificate();
        pwdCertificate.setUsername("testEG");
        pwdCertificate.setPassword("testEG");
        vmCertificate.setPwdCertificate(pwdCertificate);
        vm.setVmCertificate(vmCertificate);
        VMPort port1 = new VMPort();
        port1.setId(UUID.randomUUID().toString());
        port1.setName("port1");
        port1.setNetworkName("MEC_APP_Public");
        vm.getPortList().add(port1);
        VMPort port2 = new VMPort();
        port2.setId(UUID.randomUUID().toString());
        port2.setName("port2");
        port2.setNetworkName("MEC_APP_Private");
        vm.getPortList().add(port2);
        VirtualMachine vmCreated = vmAppVmService.createVm(PRESET_APPLICATION_ID, vm);
        Assert.assertNotNull(vmCreated);
    }

    @Test
    public void testQueryAllVMsSuccess() {
        List<VirtualMachine> vmLst = vmAppVmService.getAllVm(PRESET_APPLICATION_ID);
        Assert.assertNotNull(vmLst);
    }

    @Test
    public void testQueryVMByIdSuccess() {
        VirtualMachine vm = vmAppVmService.getVm(PRESET_APPLICATION_ID, PRESET_VM1_ID);
        Assert.assertNotNull(vm);
    }

    @Test
    public void testModifyVMSuccess() {
        VirtualMachine vm = vmAppVmService.getVm(PRESET_APPLICATION_ID, PRESET_VM1_ID);
        Assert.assertEquals(PRESET_VM_NAME, vm.getName());
        Assert.assertEquals(PRESET_VM_PORT_SIZE, vm.getPortList().size());
        vm.setName("new vm name");
        List<VMPort> vmPorts = new ArrayList<>();
        VMPort port1 = new VMPort();
        port1.setId(UUID.randomUUID().toString());
        port1.setName("port1");
        port1.setNetworkName("MEC_APP_Public");
        vmPorts.add(port1);
        vm.setPortList(vmPorts);
        boolean res = vmAppVmService.modifyVm(PRESET_APPLICATION_ID, PRESET_VM1_ID, vm);
        Assert.assertTrue(res);
        VirtualMachine modifiedVM = vmAppVmService.getVm(PRESET_APPLICATION_ID, PRESET_VM1_ID);
        Assert.assertEquals("new vm name", modifiedVM.getName());
        Assert.assertEquals(1, modifiedVM.getPortList().size());
    }

    @Test
    public void testDeleteVMSuccess() {
        boolean res = vmAppVmService.deleteVm(PRESET_APPLICATION_ID, PRESET_VM2_ID);
        Assert.assertTrue(res);
    }
}
