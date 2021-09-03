package org.edgegallery.developer.controller.image;

import io.swagger.annotations.Api;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.service.image.VmImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "vmImage")
@RequestMapping("/mec/developer/v2/vmimages")
@Api(tags = "vmImage")
public class VmImageCtl {
    @Autowired
    private VmImageService vmImageService;

}
