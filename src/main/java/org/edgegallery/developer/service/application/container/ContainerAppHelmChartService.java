package org.edgegallery.developer.service.application.container;

import java.util.List;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.springframework.web.multipart.MultipartFile;
import com.spencerwi.either.Either;

public interface ContainerAppHelmChartService {

    Either<FormatRespDto, HelmTemplateYamlRespDto> uploadHelmTemplateYaml(MultipartFile helmTemplateYaml, String userId, String projectId, String configType);

    Either<FormatRespDto, List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(String userId, String projectId);

    Either<FormatRespDto, String> deleteHelmTemplateYamlByFileId(String fileId);
}
