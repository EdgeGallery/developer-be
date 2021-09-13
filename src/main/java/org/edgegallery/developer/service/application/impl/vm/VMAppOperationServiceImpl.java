package org.edgegallery.developer.service.application.impl.vm;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMInstantiateInfoMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchOperation;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VmAppOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("vmAppOperationService")
public class VMAppOperationServiceImpl extends AppOperationServiceImpl implements VmAppOperationService {

    @Autowired
    VMInstantiateInfoMapper vmInstantiateInfoMapper;

    @Autowired
    ImageExportInfoMapper imageExportInfoMapper;

    @Override
    public Either<FormatRespDto, Boolean> instantiateVmApp(String applicationId, String vmId) {
        VMLaunchOperation actionCollection = new VMLaunchOperation(null, null);
        IActionIterator iterator = actionCollection.getActionIterator();
        while(iterator.hasNext()){
            IAction action = iterator.nextAction();
            int result  = action.execute();
            if(result != 0){
                break;
            }
        }
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> uploadFileToVm(String applicationId, String vmId, HttpServletRequest request,
        Chunk chunk) {
        return null;
    }

    @Override
    public ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier) {
        return null;
    }


    @Override
    public Either<FormatRespDto, Boolean> generatePackage(String applicationId) {
        return null;
    }

    public VMInstantiateInfo getInstantiateInfo(String vmId) {
        VMInstantiateInfo vmInstantiateInfo = vmInstantiateInfoMapper.getVMInstantiateInfo(vmId);
        vmInstantiateInfo.setPortInstanceList(vmInstantiateInfoMapper.getPortInstantiateInfoByVMId(vmId));
        return vmInstantiateInfo;
    }

    public ImageExportInfo getImageExportInfo(String vmId) {
        return imageExportInfoMapper.getImageExportInfoInfoByVMId(vmId);
    }

}
