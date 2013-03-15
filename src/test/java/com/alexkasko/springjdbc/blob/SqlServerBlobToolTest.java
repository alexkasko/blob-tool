package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import com.alexkasko.springjdbc.compress.GzipCompressor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;


/**
 * User: alexey
 * Date: 4/25/12
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = SqlServerBlobToolTest.Config.class)
// beware: http://sumitpal.wordpress.com/2010/08/26/different-things-i-tried-to-solve-sql-error-io-error-connection-reset/
public class SqlServerBlobToolTest {

    @Inject
    private BlobTestService service;
    @Inject
    private DataSource dataSource;

    @Test
    public void dummy() {
//      test is disable by default
    }

//    @Test
    public void test() throws IOException {
        long id = service.create();
        service.read(id);
        service.detach(id);
        service.delete(id);
    }

//    @Test(expected = BlobException.class)
    public void testDelete() throws IOException {
        long id = service.create();
        service.delete(id);
        service.read(id);
    }

    @Configuration
    @EnableTransactionManagement
    @ComponentScan(basePackageClasses = SqlServerBlobToolTest.class)
    static class Config {

        @Value("${blobtool.hostname}")
        private String hostname;
        @Value("${blobtool.port}")
        private int port;
        @Value("${blobtool.database}")
        private String database;
        @Value("${blobtool.user}")
        private String user;
        @Value("${blobtool.password}")
        private String password;

        @Bean
        static PropertySourcesPlaceholderConfigurer properties() {
            PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
            Resource[] resources = new Resource[]{new DefaultResourceLoader().getResource("classpath:/postgres-test.properties")};
            pspc.setLocations(resources);
            return pspc;
        }

        @Bean
        public javax.sql.DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
            ds.setUrl("jdbc:jtds:sqlserver://" + hostname + ":" + port + "/" + database);
            ds.setUsername(user);
            ds.setPassword(password);
            JdbcTemplate jt = new JdbcTemplate(ds);
            updateQuietly(jt, "drop table blob_storage_id_seq_table");
            jt.update("create table blob_storage_id_seq_table(id bigint identity)");
            updateQuietly(jt, "drop procedure blob_storage_id_seq_fun");
            jt.update("create procedure blob_storage_id_seq_fun as begin insert into blob_storage_id_seq_table default values; select ident_current('blob_storage_id_seq_table'); end;");
            updateQuietly(jt, "drop table blob_storage");
            jt.update("create table blob_storage(id bigint primary key, data image)");
            return ds;
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        public Compressor compressor() {
            return new GzipCompressor();
        }

        @Bean
        public BlobTool blobTool() {
            return new TempFileJdbcBlobTool(dataSource(), compressor(), "exec blob_storage_id_seq_fun",
                    "insert into blob_storage(id, data) values(:id, :data)",
                    "select data from blob_storage where id = :id",
                    "delete from blob_storage where id = :id");
        }

        private static void updateQuietly(JdbcTemplate jt, String sql) {
            try {
                jt.update(sql);
            } catch (Exception e) {
                // quiet, please
            }
        }
    }
}


