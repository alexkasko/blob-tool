package com.alexkasko.springjdbc.compress;

import org.iq80.snappy.SnappyInputStream;
import org.iq80.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BLOB compressor implementation, uses very fast <a href="https://github.com/dain/snappy">Snappy</a> compression method
 *
 * @author alexkasko
 * Date: 4/14/12
 */
public class SnappyCompressor extends AbstractCompressor {
    /**
     * {@inheritDoc}
     */
    @Override
    protected OutputStream wrapCompressInternal(OutputStream out) throws IOException {
        return new SnappyOutputStream(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InputStream wrapDecompressInternal(InputStream in) throws IOException {
        return new SnappyInputStream(in);
    }
}
