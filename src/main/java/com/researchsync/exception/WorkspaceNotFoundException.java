package com.researchsync.exception;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(String message) {
        super(message);
    }

    public WorkspaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
