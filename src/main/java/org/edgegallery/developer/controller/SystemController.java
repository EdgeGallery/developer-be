package org.edgegallery.developer.controller;

import io.swagger.annotations.Api;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "system")
@RequestMapping("/mec/developer/v1/system")
@Api(tags = "system")
public class SystemController {
    /**
     * todo
     * 沙箱管理接口
     * 能力中心管理接口
     * 项目管理接口
     */

}
