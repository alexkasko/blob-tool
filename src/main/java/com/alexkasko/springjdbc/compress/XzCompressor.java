package com.alexkasko.springjdbc.compress;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BLOB compressor implementation, uses high ratio <a href="http://tukaani.org/xz/">XZ</a> compression method
 *
 * @author alexkasko
 * Date: 4/14/12
 */
public class XzCompressor extends AbstractCompressor {
    private final int level;

    /**
     * No-arg constructor, uses {@code level=3}
     */
    public XzCompressor() {
        this(3);
    }

    /**
     * Constructor, allows to set compression level
     *
     * @param level LZMA2 compression level
     */
    public XzCompressor(int level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutputStream wrapCompressInternal(OutputStream out) throws IOException {
        return new XZOutputStream(out, new LZMA2Options(level));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InputStream wrapDecompressInternal(InputStream in) throws IOException {
        return new XZInputStream(in);
    }
}
