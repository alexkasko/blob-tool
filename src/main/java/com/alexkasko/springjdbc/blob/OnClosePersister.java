package com.alexkasko.springjdbc.blob;

import java.io.InputStream;

/**
 * Interface for persisting {@link DetachedBlob} data into DB
 *
 * @author alexkasko
 * Date: 3/15/13
 */
interface OnClosePersister {
    /**
     * Copies accumulated BLOB data into database
     *
     * @param id BLOB ID
     * @param data BLOB data
     */
    void persist(long id, InputStream data, long dataLength);
}
