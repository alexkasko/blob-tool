package com.alexkasko.springjdbc.blob;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.junit.Assert.assertArrayEquals;

/**
 * User: alexey
 * Date: 4/25/12
 */
public interface BlobTestService {
    long create() throws IOException;

    void read(long id) throws IOException;

    void detach(long id) throws IOException;

    void delete(long id);
}

@Service
class BlobTestServiceImpl implements BlobTestService {
    private static final int DATA_LENGTH = 1024*1024;
    private static final byte[] DATA = random();
    @Inject
    private BlobTool blobTool;

    @Override
    @Transactional
    public long create() throws IOException {
        InputStream is = new ByteArrayInputStream(DATA);
        OutputStreamBlob blob = null;
        try {
            blob = blobTool.create();
            copyLarge(is, blob.outputStream());
            return blob.getId();
        } finally {
            closeQuietly(blob);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void read(long id) throws IOException {
        InputStreamBlob blob = null;
        try {
            blob = blobTool.load(id);
            byte[] readData = toByteArray(blob.inputStream());
            assertArrayEquals("Read fail", DATA, readData);
        } finally {
            closeQuietly(blob);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void detach(long id) throws IOException {
        DetachedBlob blob = null;
        try {
            blob = blobTool.detach(id);
            byte[] readData = toByteArray(blob.inputStream());
            assertArrayEquals("Detached fail", DATA, readData);
        } finally {
            closeQuietly(blob);
        }
    }

    @Override
    @Transactional
    public void delete(long id) {
        blobTool.delete(id);
    }

    private static byte[] random() {
        Random random = new Random(42);
        byte[] res = new byte[DATA_LENGTH];
        random.nextBytes(res);
        return res;
    }
}
