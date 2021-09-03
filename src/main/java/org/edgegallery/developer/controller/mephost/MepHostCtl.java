package org.edgegallery.developer.controller.mephost;

import io.swagger.annotations.Api;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "sandbox")
@RequestMapping("/mec/developer/v2/mephosts")
@Api(tags = "sandbox")
public class MepHostCtl {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private MepHostService sandboxService;

}
