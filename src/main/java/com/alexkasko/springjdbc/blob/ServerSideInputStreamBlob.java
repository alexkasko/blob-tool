package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.alexkasko.springjdbc.blob.Utils.closeQuietly;
import static com.alexkasko.springjdbc.blob.Utils.copyLarge;

/**
 * Input stream connected to persistent compressed BLOB in remote database
 *
 * @author alexkasko
 * Date: 8/11/11
 */
class ServerSideInputStreamBlob implements InputStreamBlob {
    private final long id;
    private final InputStream inputStream;

    /**
     * @param id BLOB ID
     * @param inputStream decompressed blob data stream
     * @param compressor compressor instance
     */
    ServerSideInputStreamBlob(long id, InputStream inputStream, Compressor compressor) {
        if(id <= 0) throw new BlobException("Provided id must be positive");
        if(null == inputStream) throw new BlobException("Provided inputStream is null");
        if(null == compressor) throw new BlobException("Provided compressor is null");
        this.id = id;
        this.inputStream = compressor.wrapDecompress(inputStream);
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
    public InputStream inputStream() {
        return inputStream;
    }


    /**
     * {@inheritDoc}
     */
    public long readAndClose(OutputStream out) {
        try {
            return copyLarge(inputStream, out);
        } catch(IOException e) {
            throw new BlobException("Error on blob read, id: [" + id + "]", e);
        } finally {
            closeQuietly(inputStream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        closeQuietly(inputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServerSideInputStreamBlob");
        sb.append("{id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}

