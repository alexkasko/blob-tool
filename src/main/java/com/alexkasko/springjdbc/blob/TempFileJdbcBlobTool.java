package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Client-side implementation of BLOB tool for databases, that don't support
 * {@code java.io.OutputStream java.sql.Blob#setBinaryStream(long pos)} method properly
 * (implement it on client in-memory or in similar fashion with temp file).
 * This implementation creates BLOB as compressed temp file and transfer data from it after BLOB closed by client.
 * Data is inserted from file into database using JDBC's streaming
 * {@code void java.sql.PreparedStatement#setBinaryStream(int parameterIndex, java.io.InputStream x, long length)}
 * method. So file data never goes fully into memory outside of JDBC driver,
 * but may go into memory inside some creepy JDBC implementation.
 * Not well suited for highload applications, may be used with H2 database to simulate PostgreSQL-like BLOBs in tests.
 *
 * @author alexkasko
 * Date: 4/27/12
 */
public class TempFileJdbcBlobTool extends AbstractJdbcBlobTool {

    /**
     * Shortcut constructor with default SQL
     *
     * @param dataSource data source
     * @param compressor compressor
     */
    public TempFileJdbcBlobTool(DataSource dataSource, Compressor compressor) {
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
    public TempFileJdbcBlobTool(DataSource dataSource, Compressor compressor, String generateIdSQL, String insertSQL, String loadSQL, String deleteSQL) {
        super(dataSource, compressor, generateIdSQL, insertSQL, loadSQL, deleteSQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStreamBlob create() throws BlobException {
        try {
            long id = jt.getJdbcOperations().queryForLong(generateIdSQL);
            return new TempFileOutputStreamBlob(id, compressor, new Persister());
        } catch (Exception e) {
            throw e instanceof BlobException ? (BlobException) e : new BlobException("Cannot create blob", e);
        }
    }

    private class Persister implements OnClosePersister {

        @Override
        public void persist(long id, InputStream data, long dataLength) {
            jt.getJdbcOperations().update(new InsertPS(id, data, dataLength));
        }

        private class InsertPS implements PreparedStatementCreator {
            private final long id;
            private final InputStream data;
            private final long length;

            private InsertPS(long id, InputStream data, long length) {
                this.id = id;
                this.data = data;
                this.length = length;
            }

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                // substitute named params, see NamedParameterJdbcTemplate#getPreparedStatementCreator(String sql, SqlParameterSource paramSource)
                ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(insertSQL);
                MapSqlParameterSource paramSource = new MapSqlParameterSource();
                paramSource.addValue("id", id, Types.BIGINT);
                paramSource.addValue("data", data, Types.BLOB);
                String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
                PreparedStatement stmt = con.prepareStatement(sqlToUse);
                stmt.setLong(1, (Long) params[0]);
                if(length < Integer.MAX_VALUE) {
                    stmt.setBinaryStream(2, (InputStream) params[1], (int) length);
                } else {
                    //warn: JTDS doesn't support long sized blobs
                    stmt.setBinaryStream(2, (InputStream) params[1], length);
                }
                return stmt;
            }
        }
    }
}
