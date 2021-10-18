package org.edgegallery.developer.service.application.common;

public enum EnumExportImageStatus {

    EXPORT_IMAGE_STATUS_TIMEOUT("timeout"),

    EXPORT_IMAGE_STATUS_ERROR("error"),

    EXPORT_IMAGE_STATUS_FAILED("killed"),

    EXPORT_IMAGE_STATUS_SUCCESS("active");

    private String name;

    EnumExportImageStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
