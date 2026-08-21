package com.dex.insights.repository;

/** Raised when the source data cannot be read or fails validation at startup. */
public class DatasetLoadException extends RuntimeException {

    public DatasetLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatasetLoadException(String message) {
        super(message);
    }
}
