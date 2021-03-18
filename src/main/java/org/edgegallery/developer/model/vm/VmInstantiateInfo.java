package org.edgegallery.developer.model.vm;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmInstantiateInfo {

    private  String code;

    private String msg;

    private List<VmInfo> data;

}
