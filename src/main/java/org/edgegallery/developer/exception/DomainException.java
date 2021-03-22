package org.edgegallery.developer.exception;

public class DomainException extends RuntimeException {
    private static final long serialVersionUID = 1646444285623052477L;

    public DomainException(String message) {
        super(message);
    }
}
