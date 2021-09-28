package org.edgegallery.developer.service.uploadfile;

import java.util.List;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    ResponseEntity<byte[]> getFile(String fileId, String userId, String type);

    UploadedFile getApiFile(String fileId, String userId);

    UploadedFile uploadMdFile(String userId, MultipartFile uploadFile);

    UploadedFile uploadPicFile(String userId, MultipartFile uploadFile);

    UploadedFile uploadApiFile(String userId, MultipartFile uploadFile);

    UploadedFile uploadConfigFile(String userId, MultipartFile uploadFile);

    ResponseEntity<byte[]> downloadSampleCode(List<String> apiFileIds);

    AppPkgStructure getSampleCodeStru(List<String> apiFileIds);

    String getSampleCodeContent(String fileName);

    ResponseEntity<byte[]> getSdkProject(String fileId, String lan);

    UploadedFile saveFileToLocal(MultipartFile uploadFile, String userId);

    void moveFileToWorkSpaceById(String srcId, String applicationId);
}
