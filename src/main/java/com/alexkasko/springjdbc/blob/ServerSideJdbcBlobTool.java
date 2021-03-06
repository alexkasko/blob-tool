package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Types;

/**
 * JDBC BLOB implementation that uses {@code java.io.OutputStream java.sql.Blob#setBinaryStream(long pos)}
 * method to get output stream to remote BLOB. Among popular RDBMSes this method implemented properly (true server-side)
 * only in Oracle (Postgres seems to has it too, but we already use native LO API for it).
 *
 * @author alexkasko
 * Date: 4/27/12
 */
public class ServerSideJdbcBlobTool extends AbstractJdbcBlobTool {

    /**
     * Shortcut constructor with default SQL
     *
     * @param dataSource data source
     * @param compressor compressor
     */
    public ServerSideJdbcBlobTool(DataSource dataSource, Compressor compressor) {
        super(dataSource, compressor);
    }

    /**
     * @param dataSource    data source
     * @param compressor    compressor
     * @param generateIdSQL SQL to generate BLOB ID, default: {@code select nextval('blob_storage_id_seq')}
     * @param insertSQL     SQL to insert blob into database, default: {@code insert into blob_storage(id, data) values(:id, :data)}
     * @param loadSQL       SQL to load blob data from database, default: {@code select data from blob_storage where id = :id}
     * @param deleteSQL     SQL to delete BLOB from database, default: {@code delete from blob_storage where id = :id}
     */
    public ServerSideJdbcBlobTool(DataSource dataSource, Compressor compressor, String generateIdSQL, String insertSQL, String loadSQL, String deleteSQL) {
        super(dataSource, compressor, generateIdSQL, insertSQL, loadSQL, deleteSQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStreamBlob create() throws BlobException {
        try {
            long id = jt.getJdbcOperations().queryForLong(generateIdSQL);
            Connection conn = DataSourceUtils.doGetConnection(dataSource);
            Blob blob = conn.createBlob();
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id, Types.BIGINT);
            params.addValue("data", blob, Types.BLOB);
            jt.update(insertSQL, params);
            Blob created = jt.queryForObject(loadSQL, params, Blob.class);
            OutputStream blobStream = created.setBinaryStream(1);
            return new ServerSideOutputStreamBlob(id, blobStream, compressor);
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot create blob", e);
        }
    }
}
