package com.alexkasko.springjdbc.blob;

/**
 * BLOB data, detached from database. Default implementation stores it in
 * temporary file
 *
 * @author alexkasko
 * Date: 3/15/13
 */
public interface DetachedBlob extends InputStreamBlob {
    /**
     * Compressed size of BLOB data
     *
     * @return compressed size of BLOB data
     */
    long compressedSize();
}
