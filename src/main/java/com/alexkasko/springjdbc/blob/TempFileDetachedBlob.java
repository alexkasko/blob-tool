package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;

import java.io.*;

import static com.alexkasko.springjdbc.blob.Utils.closeQuietly;
import static com.alexkasko.springjdbc.blob.Utils.copyLarge;
import static java.io.File.createTempFile;

/**
 * Implementation of {@link DetachedBlob} using temporary file
 *
 * @author alexkasko
 * Date: 11/28/12
 */
class TempFileDetachedBlob implements DetachedBlob {

    private final long id;
    private final File file;
    private final InputStream inputStream;

    /**
     * Constructor
     *
     * @param id BLOB ID
     * @param compressedData compressed BLOB data
     * @param compressor compressor instance
     */
    TempFileDetachedBlob(long id, InputStream compressedData, Compressor compressor) {
        if(id <= 0) throw new BlobException("Provided id must be positive");
        if(null == compressor) throw new BlobException("Provided compressor is null");
        OutputStream os = null;
        try {
            this.id = id;
            this.file = createTempFile(TempFileDetachedBlob.class.getName(), Long.toString(id));
            this.file.deleteOnExit();
            os = new FileOutputStream(file);
            copyLarge(compressedData, os);
            os.close();
            this.inputStream = compressor.wrapDecompress(new FileInputStream(file));
        } catch (IOException e) {
            throw new BlobException("Error writing temporary file for blob, id: [" + id + "]", e);
        } finally {
            closeQuietly(os);
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
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readAndClose(OutputStream out) {
        try {
            return copyLarge(inputStream, out);
        } catch (IOException e) {
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
        file.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long compressedSize() {
        return file.length();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TempFileDetachedBlob");
        sb.append("{id=").append(id);
        sb.append(", file=").append(file.getAbsolutePath());
        sb.append('}');
        return sb.toString();
    }
}
