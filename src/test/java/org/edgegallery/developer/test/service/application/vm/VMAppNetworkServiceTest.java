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

import java.util.List;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
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
public class VMAppNetworkServiceTest {

    private final String PRESET_APPLICATION_ID = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";

    private final String PRESET_APP1_ID = "3f11715f-b59e-4c23-965b-b7f9c34c20d1";

    private final String PRESET_NETWORK1_ID = "560e554c-7ef4-4f21-b2b5-e33fa64aa069";

    private final String PRESET_NETWORK1_NAME = "MEC_APP_Public";

    private final String PRESET_NETWORK2_ID = "560e554c-7ef4-4f21-b2b5-e33fa64aa068";

    private final String PRESET_VM_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d757";

    private final String PRESET_NETWORK_NAME = "MEC_APP_Public";

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppNetworkServiceTest.class);

    @Autowired
    private VMAppVmService vmAppVmService;

    @Autowired
    private VMAppNetworkService vmAppNetworkService;

    @Test
    public void testCreateNetworkSuccess() {
        Network network = new Network();
        network.setId("test_network_id");
        network.setName("test_network");
        network.setDescription("This is test network");
        Network networkCreated = vmAppNetworkService.createNetwork(PRESET_APPLICATION_ID, network);
        Assert.assertEquals("test_network", networkCreated.getName());
    }

    @Test
    public void testQueryAllNetworksSuccess() {
        List<Network> networkList = vmAppNetworkService.getAllNetwork(PRESET_APPLICATION_ID);
        Assert.assertNotNull(networkList);
    }

    @Test
    public void testQueryNetworkByIdSuccess() {
        Network network = vmAppNetworkService.getNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK1_ID);
        Assert.assertNotNull(network);
    }

    @Test
    public void testModifyVMSuccess() {
        Network network = vmAppNetworkService.getNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK1_ID);
        Assert.assertEquals(PRESET_NETWORK_NAME, network.getName());
        network.setName("new network name");
        network.setDescription("new network description");
        boolean res = vmAppNetworkService.modifyNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK1_ID, network);
        Assert.assertTrue(res);
        Network modifiedNetwork = vmAppNetworkService.getNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK1_ID);
        Assert.assertEquals("new network name", modifiedNetwork.getName());
    }

    @Test
    public void testDeleteNetworkSuccess() {
        boolean res = vmAppNetworkService.deleteNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK2_ID);
        Assert.assertTrue(res);
    }

    @Test
    public void testDeleteNetworkFailed() {
        VirtualMachine vm = vmAppVmService.getVm(PRESET_APPLICATION_ID, PRESET_VM_ID);
        boolean network1Used = false;
        for (VMPort port : vm.getPortList()) {
            if (PRESET_NETWORK1_NAME.equals(port.getNetworkName())) {
                network1Used = true;
            }
        }
        Assert.assertTrue(network1Used);
        try {
            vmAppNetworkService.deleteNetwork(PRESET_APPLICATION_ID, PRESET_NETWORK1_ID);
        } catch (DeveloperException e) {
            Assert.assertEquals("Network is used by vm port. Cannot be deleted", e.getMessage());
        }
    }

    @Test
    public void testDeleteAppNetworksSuccess() {
        boolean res = vmAppNetworkService.deleteNetworkByAppId(PRESET_APP1_ID);
        Assert.assertTrue(res);
    }

    @Test
    public void testDeleteAppNetworksFailed() {
        VirtualMachine vm = vmAppVmService.getVm(PRESET_APPLICATION_ID, PRESET_VM_ID);
        boolean network1Used = false;
        for (VMPort port : vm.getPortList()) {
            if (PRESET_NETWORK1_NAME.equals(port.getNetworkName())) {
                network1Used = true;
            }
        }
        Assert.assertTrue(network1Used);
        try {
            vmAppNetworkService.deleteNetworkByAppId(PRESET_APPLICATION_ID);
        } catch (DeveloperException e) {
            Assert.assertEquals("Network is used by vm port. Cannot be deleted", e.getMessage());
        }
    }
}
