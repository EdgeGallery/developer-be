package org.edgegallery.developer.model.instantiate;

public enum EnumAppInstantiateStatus {
    PACKAGE_GENERATING("Generating Package"),
    PACKAGE_GENERATE_FAILED("Generate Package Failed"),
    PACKAGE_GENERATE_SUCCESS("Generate Package Success"),
    PACKAGE_DISTRIBUTING("Distributing Package"),
    PACKAGE_DISTRIBUTE_FAILED("Distribute Package Failed"),
    PACKAGE_DISTRIBUTE_SUCCESS("Distribute Package Success"),
    INSTANTIATING("Instantiating"),
    INSTANTIATE_FAILED("Instantiate Failed"),
    INSTANTIATE_SUCCESS("Instantiate Success"),
    SUCCESS("Success");
    private String name;

    EnumAppInstantiateStatus(String name) {
        this.name = name;
    }
}
