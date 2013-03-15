package com.alexkasko.springjdbc.blob;

/**
 * Exception for BLOB related errors
 *
 * @author alexkasko
 *         Date: 4/14/12
 */
public class BlobException extends RuntimeException {
    private static final long serialVersionUID = 3272556746044949100L;

    /**
     * {@inheritDoc}
     */
    public BlobException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public BlobException(String message, Throwable cause) {
        super(message, cause);
    }
}
