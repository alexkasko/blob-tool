package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;

import static org.springframework.util.StringUtils.hasText;

/**
 * Basic implementation of Postgres-flavoured BLOB operations for other databases using standard JDBC API.
 * Implementations may be true server-side (Oracle) or client-side with temp file.
 * To simulate Postgres BLOBs {@code blob_storage_id_seq} sequence and
 * {@code blob_storage(id bigint primary key, data blob)} must be created beforehand.
 *
 * @author alexkasko
 * Date: 4/13/12
 * @see BlobTool
 */

public abstract class AbstractJdbcBlobTool implements BlobTool {
    protected final DataSource dataSource;
    protected final NamedParameterJdbcTemplate jt;
    protected final Compressor compressor;
    protected final String generateIdSQL;
    protected final String insertSQL;
    protected final String loadSQL;
    protected final String deleteSQL;

    /**
     * Shortcut constructor with default SQL
     *
     * @param dataSource data source
     * @param compressor compressor
     */
    public AbstractJdbcBlobTool(DataSource dataSource, Compressor compressor) {
        this(dataSource, compressor,
                "select nextval('blob_storage_id_seq')",
                "insert into blob_storage(id, data) values(:id, :data)",
                "select data from blob_storage where id = :id",
                "delete from blob_storage where id = :id");
    }

    /**
     * @param dataSource data source
     * @param compressor compressor
     * @param generateIdSQL SQL to generate BLOB ID, default: {@code select nextval('blob_storage_id_seq')}
     * @param insertSQL SQL to insert blob into database, default: {@code insert into blob_storage(id, data) values(:id, :data)}
     * @param loadSQL SQL to load blob data from database, default: {@code select data from blob_storage where id = :id}
     * @param deleteSQL SQL to delete BLOB from database, default: {@code delete from blob_storage where id = :id}
     */
    public AbstractJdbcBlobTool(DataSource dataSource, Compressor compressor, String generateIdSQL, String insertSQL,
                                String loadSQL, String deleteSQL) {
        if(null == dataSource) throw new BlobException("Provided dataSource is null");
        if(null == compressor) throw new BlobException("Provided compressor is null");
        if(!hasText(generateIdSQL)) throw new BlobException("Provided generateIdSQL is blank");
        if(!hasText(insertSQL)) throw new BlobException("Provided insertSQL is blank");
        if(!hasText(loadSQL)) throw new BlobException("Provided loadSQL is blank");
        if(!hasText(deleteSQL)) throw new BlobException("Provided deleteSQL is blank");
        this.dataSource = dataSource;
        this.jt = new NamedParameterJdbcTemplate(dataSource);
        this.compressor = compressor;
        this.generateIdSQL = generateIdSQL;
        this.insertSQL = insertSQL;
        this.loadSQL = loadSQL;
        this.deleteSQL = deleteSQL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStreamBlob load(long id) throws BlobException {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id, Types.BIGINT);
            Blob loaded = jt.queryForObject(loadSQL, params, Blob.class);
            if (null == loaded) throw new BlobException("No blob found for id: [" + id + "]");
            return new ServerSideInputStreamBlob(id, loaded.getBinaryStream(), compressor);
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
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id, Types.BIGINT);
            Blob loaded = jt.queryForObject(loadSQL, params, Blob.class);
            if (null == loaded) throw new BlobException("No blob found for id: [" + id + "]");
            return new TempFileDetachedBlob(id, loaded.getBinaryStream(), compressor);
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
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id, Types.BIGINT);
            int count = jt.update(deleteSQL, params);
            if (1 != count) throw new BlobException("One row must have been deleted, but was: [" + count + "] " +
                    "for blob, id: [" + id + "]");
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot delete blob, id: [" + id + "]", e);
        }
    }
}
