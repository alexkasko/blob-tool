package com.alexkasko.springjdbc.compress;

import com.alexkasko.springjdbc.blob.BlobException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common compressor methods implementation
 *
 * @author alexkasko
 * Date: 4/28/12
 */
public abstract class AbstractCompressor implements Compressor {
    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream wrapCompress(OutputStream out) {
        if(null == out) throw new BlobException("Provided OutputStream is null");
        try {
            return wrapCompressInternal(out);
        } catch (Exception e) {
            if(e instanceof BlobException) throw (BlobException) e;
            throw new BlobException("Error on compressing", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream wrapDecompress(InputStream in) {
        if(null == in) throw new BlobException("Provided InputStream is null");
        try {
            return wrapDecompressInternal(in);
        } catch (Exception e) {
            if(e instanceof BlobException) throw (BlobException) e;
            throw new BlobException("Error on decompressing", e);
        }
    }

    /**
     * Must compress provided stream
     *
     * @param out stream to compress
     * @return compressed stream
     * @throws Exception
     */
    protected abstract OutputStream wrapCompressInternal(OutputStream out) throws Exception;

    /**
     * Must decompress provided stream
     *
     * @param in stream to decompress
     * @return decompressed stream
     * @throws Exception
     */
    protected abstract InputStream wrapDecompressInternal(InputStream in) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
