package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link BlobTool} implementation for PostgreSQL
 *
 * @author alexkasko
 * Date: 3/15/13
 */
public class PostgresBlobTool implements BlobTool {
    private final DataSource dataSource;
    private final Compressor compressor;

    /**
     * Constructor
     *
     * @param dataSource data source
     * @param compressor compressor instance
     */
    public PostgresBlobTool(DataSource dataSource, Compressor compressor) {
        if(null == dataSource) throw new BlobException("Provided dataSource is null");
        if(null == compressor) throw new BlobException("Provided compressor is null");
        this.dataSource = dataSource;
        this.compressor = compressor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStreamBlob create() throws BlobException {
        try {
            Connection conn = DataSourceUtils.doGetConnection(dataSource);
            LargeObjectManager manager = extractManager(conn);
            long oid = manager.createLO(LargeObjectManager.WRITE);
            LargeObject lob = manager.open(oid, LargeObjectManager.WRITE);
            return new ServerSideOutputStreamBlob(oid, lob.getOutputStream(), compressor);
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot create blob", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStreamBlob load(long id) throws BlobException {
        try {
            Connection conn = DataSourceUtils.doGetConnection(dataSource);
            LargeObjectManager manager = extractManager(conn);
            LargeObject lob = manager.open(id, LargeObjectManager.READ);
            return new ServerSideInputStreamBlob(id, lob.getInputStream(), compressor);
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot load blob, id: [" + id + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetachedBlob detach(long id) throws BlobException {
        try {
            Connection conn = DataSourceUtils.doGetConnection(dataSource);
            LargeObjectManager manager = extractManager(conn);
            LargeObject lob = manager.open(id, LargeObjectManager.READ);
            return new TempFileDetachedBlob(id, lob.getInputStream(), compressor);
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot detach blob, id: [" + id + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(long id) throws BlobException {
        try {
            Connection conn = DataSourceUtils.doGetConnection(dataSource);
            LargeObjectManager manager = extractManager(conn);
            manager.delete(id);
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot delete blob, id: [" + id + "]", e);
        }
    }

    private static LargeObjectManager extractManager(Connection conn) {
        try {
            PGConnection pgConn = conn instanceof PGConnection ? (PGConnection) conn : conn.unwrap(PGConnection.class);
            return pgConn.getLargeObjectAPI();
        } catch (SQLException e) {
            throw new BlobException("Cannot unwrap native postgres connection from: [" + conn + "]", e);
        }
    }
}
