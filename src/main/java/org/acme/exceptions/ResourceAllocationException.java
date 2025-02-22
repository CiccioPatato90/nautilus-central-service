package org.acme.exceptions;

public class ResourceAllocationException extends RuntimeException {
    public ResourceAllocationException(String message, Throwable cause) {
        super(message, cause);
    }
}