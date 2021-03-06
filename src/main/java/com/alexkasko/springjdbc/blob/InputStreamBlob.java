package com.alexkasko.springjdbc.blob;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for "readable" BLOBs
 *
 * @author alexkasko
 * Date: 11/27/12
 */
public interface InputStreamBlob extends Closeable {

    /**
     * Returns BLOB unique ID from database
     *
     * @return BLOB ID
     */
    long getId();

    /**
     * BLOB data stream
     *
     * @return BLOB data stream
     */
    InputStream inputStream();

    /**
     * Shortcut method, reads all BLOB data into provided stream and close BLOB
     *
     * @param out stream to write data into
     * @return number ob bytes read
     * @throws BlobException on copy exception
     */
    long readAndClose(OutputStream out);

    /**
     * Closes BLOB releasing database resources connected to it (if any)
     * Must not throw any exception.
     */
    @Override
    void close();
}
