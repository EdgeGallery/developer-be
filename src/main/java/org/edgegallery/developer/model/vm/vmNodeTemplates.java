//package org.edgegallery.developer.model.vm;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//import com.fasterxml.jackson.annotation.JsonInclude;
//
//@NoArgsConstructor
//@ToString
//@Getter
//@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class vmNodeTemplates {
//
//    private SimpleVNF Simple_VNF;
//    private VmCompute EMS_VDU1;
//    private VmComputePoint0 EMS_VDU1_CP0;
//    private VmComputePoint1 EMS_VDU1_CP1;
//    private VmComputePoint2 EMS_VDU1_CP22;
//    private MecAppMp1 MEC_APP_MP1;
//    private MecAppN6 MEC_APP_N6;
//    private MecAppInternet MEC_APP_INTERNET;
//    @NoArgsConstructor
//    @ToString
//    @Getter
//    @Setter
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class SimpleVNF {
//        private String type = "tosca.nodes.nfv.VNF";
//        private SimpleProperties properties;
//
//    }
//
//    @NoArgsConstructor
//    @ToString
//    @Getter
//    @Setter
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class SimpleProperties {
//        private String vnfd_id = "eulerforTR6_iso_arm_zhi_new1";
//        private String vnfd_version = "v1.0";
//        private String provider = "eastcom";
//        private String product_name = "vSPCLNLWYEMS";
//        private String software_version = "v1.0.0";
//        private String product_info_name = "vSPCLNLWYEMS";
//        private String product_info_description = "EASTCOM vSPCLNLWY EMS";
//        private String flavour_id = "default";
//        private String flavour_description = "default flavor";
//        private Boolean ve_vnfm_vnf_enable = false;
//        private Boolean ve_vnfm_em_enable = false;
//
//    }
//
//    @NoArgsConstructor
//    @ToString
//    @Getter
//    @Setter
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class VmCompute {
//        private String type = "tosca.nodes.nfv.VNF";
//        private EMSCapabilities capabilities;
//        private EMSProperties properties;
//
//    }
//
//
//}
//
