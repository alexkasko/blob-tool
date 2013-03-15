package com.alexkasko.springjdbc.blob;

/**
 * Front-end for BLOB operations.
 *
 * @author alexkasko
 * Date: 4/13/12
 */
public interface BlobTool {
    /**
     * Creates BLOB and returns object to write data into
     *
     * @return BLOB stream to write to, it must be closed by the caller
     * @throws BlobException on BLOB error
     */
    OutputStreamBlob create() throws BlobException;

    /**
     * Loads BLOB from database
     *
     * @param id BLOB ID
     * @return BLOB data stream, must be closed by the caller
     * @throws BlobException on BLOB error
     */
    InputStreamBlob load(long id) throws BlobException;

    /**
     * Loads BLOB from database in "detached" mode - copies compressed BLOB data
     * into temporary file and closes database BLOB
     *
     * @param id BLOB ID
     * @return BLOB data in detached (temp-file) mode
     * @throws BlobException on BLOB error
     */
    DetachedBlob detach(long id) throws BlobException;

    /**
     * Deletes BLOB from datbase
     *
     * @param id BLOB ID
     * @throws BlobException on BLOB error
     */
    void delete(long id) throws BlobException;
}
