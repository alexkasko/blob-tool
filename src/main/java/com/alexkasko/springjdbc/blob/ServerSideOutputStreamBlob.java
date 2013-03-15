package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;

import java.io.OutputStream;

import static com.alexkasko.springjdbc.blob.Utils.closeQuietly;

/**
 * Output stream connected to persistent compressed BLOB in remote database
 *
 * @author alexkasko
 * Date: 3/15/13
 */
class ServerSideOutputStreamBlob implements OutputStreamBlob {
    private final long id;
    private final OutputStream outputStream;

    /**
     * Constructor
     *
     * @param id BLOB id
     * @param outputStream stream to write data into
     * @param compressor compressor instance
     */
    ServerSideOutputStreamBlob(long id, OutputStream outputStream, Compressor compressor) {
        if(id <= 0) throw new BlobException("Provided id must be positive");
        if(null == outputStream) throw new BlobException("Provided outputStream is null");
        if(null == compressor) throw new BlobException("Provided compressor is null");
        this.id = id;
        this.outputStream = compressor.wrapCompress(outputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream outputStream() {
        return outputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        closeQuietly(outputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServerSideOutputStreamBlob");
        sb.append("{id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
