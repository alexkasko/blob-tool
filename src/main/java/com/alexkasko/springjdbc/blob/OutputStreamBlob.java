package com.alexkasko.springjdbc.blob;

import java.io.Closeable;
import java.io.OutputStream;

/**
 * Interface for PostgreSQL-like BLOBs, can be used as {@link java.io.OutputStream} on data write.
 *
 * @author alexkasko
 * Date: 11/27/12
 */
public interface OutputStreamBlob extends Closeable {
    /**
     * Returns BLOB unique ID from database
     *
     * @return BLOB ID
     */
    long getId();

    /**
     * BLOB data stream to write into
     *
     * @return BLOB data stream
     */
    OutputStream outputStream();

    /**
     * Closes BLOB releasing database resources connected to it (if any)
     * Must not throw any exception.
     */
    @Override
    void close();
}
