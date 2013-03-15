package com.alexkasko.springjdbc.compress;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for different BLOB compression methods
 *
 * @author  alexkasko
 * Date: 4/14/12
 * @see com.alexkasko.springjdbc.blob.BlobTool
 */
public interface Compressor {

    /**
     * Must compress provided stream
     *
     * @param out stream to compress
     * @return compressed stream
     * @throws com.alexkasko.springjdbc.blob.BlobException on any exception
     */
    OutputStream wrapCompress(OutputStream out);

    /**
     * Must decompress provided stream
     *
     * @param in stream to decompress
     * @return decompressed stream
     * @throws com.alexkasko.springjdbc.blob.BlobException on any exception
     */
    InputStream wrapDecompress(InputStream in);
}
