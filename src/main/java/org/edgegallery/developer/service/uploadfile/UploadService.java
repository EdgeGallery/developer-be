package org.edgegallery.developer.service.uploadfile;

import com.spencerwi.either.Either;
import java.util.List;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    Either<FormatRespDto, ResponseEntity<byte[]>> getFile(String fileId, String userId, String type);

    Either<FormatRespDto, UploadedFile> getApiFile(String fileId, String userId);

    Either<FormatRespDto, UploadedFile> uploadFile(String userId, MultipartFile uploadFile);

    Either<FormatRespDto, UploadedFile> uploadMdFile(String userId, MultipartFile uploadFile);

    Either<FormatRespDto, HelmTemplateYamlRespDto> uploadHelmTemplateYaml(MultipartFile helmTemplateYaml,
        String userId, String projectId, String configType);

    Either<FormatRespDto, List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(String userId, String projectId);

    Either<FormatRespDto, String> deleteHelmTemplateYamlByFileId(String fileId);

    Either<FormatRespDto, ResponseEntity<byte[]>> downloadSampleCode(List<String> apiFileIds);

    Either<FormatRespDto, AppPkgStructure> getSampleCodeStru(List<String> apiFileIds);

    Either<FormatRespDto, String> getSampleCodeContent(String fileName);

    Either<FormatRespDto, ResponseEntity<byte[]>> getSdkProject(String fileId, String lan);

    UploadedFile saveFileToLocal(MultipartFile uploadFile, String userId);

    void moveFileToWorkSpaceById(String srcId, String applicationId);
}
