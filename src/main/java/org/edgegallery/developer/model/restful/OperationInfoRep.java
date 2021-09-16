package org.edgegallery.developer.model.restful;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class OperationInfoRep {

    private String operationId;

    public OperationInfoRep(String operationId) {
        this.operationId = operationId;
    }

}
