package com.alexkasko.springjdbc.compress;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * No-op BLOB compressor implementation, returns provided streams untouched
 *
 * @author alexkasko
 * Date: 4/14/12
 */
public class NoCompressor implements Compressor {
    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream wrapCompress(OutputStream out) {
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream wrapDecompress(InputStream in) {
        return in;
    }
}
