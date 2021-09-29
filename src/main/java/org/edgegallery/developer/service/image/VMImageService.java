package org.edgegallery.developer.service.image;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.edgegallery.developer.model.vmimage.VMImage;
import org.springframework.http.ResponseEntity;

public interface VMImageService {

    VMImageRes getVmImages(VMImageReq vmImageReq);

    Boolean createVmImage(VMImage vmImage);

    Boolean updateVmImage(VMImage vmImage, Integer imageId);

    Boolean deleteVmImage(Integer imageId);

    Boolean publishVmImage(Integer imageId);

    Boolean resetImageStatus(Integer imageId);

    ResponseEntity uploadVmImage(HttpServletRequest request, Chunk chunk, Integer imageId);

    List<Integer> checkUploadedChunks(Integer imageId, String identifier);

    ResponseEntity cancelUploadVmImage(Integer imageId, String identifier);

    ResponseEntity mergeVmImage(String fileName, String identifier, Integer imageId);

    ResponseEntity<byte[]> downloadVmImage(Integer imageId);

    Boolean imageSlim(Integer imageId);
}
