package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;

import java.io.*;

import static com.alexkasko.springjdbc.blob.Utils.closeQuietly;
import static java.io.File.createTempFile;

/**
 * {@link OutputStreamBlob} implementation for RDBMses those don't have proper
 * "writable" server side blobs.
 *
 * @author alexkasko
 * Date: 3/15/13
 */
class TempFileOutputStreamBlob implements OutputStreamBlob {
    private final long id;
    private final File file;
    private final OnClosePersister persister;
    private final OutputStream outputStream;

    /**
     * Constructor
     *
     * @param id BLOB ID
     * @param compressor compressor instance
     * @param persister persister instance
     */
    TempFileOutputStreamBlob(long id, Compressor compressor, OnClosePersister persister) {
        if (id <= 0) throw new BlobException("Provided id must be positive");
        if (null == compressor) throw new BlobException("Provided compressor is null");
        if (null == persister) throw new BlobException("Provided persister is null");
        try {
            this.id = id;
            this.persister = persister;
            this.file = createTempFile(TempFileOutputStreamBlob.class.getName(), Long.toString(id));
            this.file.deleteOnExit();
            this.outputStream = compressor.wrapCompress(new FileOutputStream(file));
        } catch (IOException e) {
            throw new BlobException("Error creating temporary file for blob, id: [" + id + "]", e);
        }
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
        InputStream is = null;
        try {
            closeQuietly(outputStream);
            is = new FileInputStream(file);
            persister.persist(id, is, file.length());
        } catch (FileNotFoundException e) {
            throw new BlobException("Error persisting blob, id: [" + id + "]", e);
        } finally {
            closeQuietly(is);
            this.file.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TempFileOutputStreamBlob");
        sb.append("{id=").append(id);
        sb.append(", file=").append(file.getAbsolutePath());
        sb.append('}');
        return sb.toString();
    }
}
