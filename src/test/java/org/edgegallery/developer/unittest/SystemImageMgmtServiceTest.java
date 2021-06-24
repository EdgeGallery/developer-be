package org.edgegallery.developer.unittest;

import com.spencerwi.either.Either;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.model.workspace.MepHost;
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

import java.util.List;

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
    public void testGetAll() {
        AccessUserUtil.setUser("e111f3e7-90d8-4a39-9874-ea6ea6752ee5", "tenant");
        MepGetSystemImageReq mepGetSystemImageReq = new MepGetSystemImageReq();
        MepSystemQueryCtrl queryCtrl = new MepSystemQueryCtrl();
        mepGetSystemImageReq.setQueryCtrl(queryCtrl);
        Either<FormatRespDto, MepGetSystemImageRes> res = systemImageMgmtService
                .getSystemImages(mepGetSystemImageReq);
        Assert.assertNotNull(res);
    }

}
