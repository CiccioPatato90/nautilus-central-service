package org.acme.exceptions;

public class EmptyProjectList extends RuntimeException {
    public EmptyProjectList(String message) {
        super(message);
    }
}
