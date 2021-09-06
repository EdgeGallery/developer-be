package org.edgegallery.developer.service.application.impl.container;

import java.util.List;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.spencerwi.either.Either;
@Service("containerAppHelmChartService")
public class ContainerAppHelmChartServiceImpl implements ContainerAppHelmChartService {

    @Override
    public Either<FormatRespDto, HelmTemplateYamlRespDto> uploadHelmTemplateYaml(MultipartFile helmTemplateYaml,
        String userId, String projectId, String configType) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(String userId,
        String projectId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, String> deleteHelmTemplateYamlByFileId(String fileId) {
        return null;
    }
}
