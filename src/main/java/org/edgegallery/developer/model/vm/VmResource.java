package org.edgegallery.developer.model.vm;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmResource {

    private List<VmRegulation> vmRegulationList;

    private List<VmSystem> vmSystemList;

    private List<VmNetwork> vmNetworkList;

}
