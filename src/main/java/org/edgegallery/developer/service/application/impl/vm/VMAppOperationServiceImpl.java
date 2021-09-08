package org.edgegallery.developer.service.application.impl.vm;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchActionCollection;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VmAppOperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("vmAppActionService")
public class VMAppOperationServiceImpl extends AppOperationServiceImpl implements VmAppOperationService {

    @Override
    public Either<FormatRespDto, Boolean> actionVm(String applicationId, String vmId) {
        VMLaunchActionCollection actionCollection = new VMLaunchActionCollection();
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
}
