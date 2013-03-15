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
//@ContextConfiguration(classes = OracleBlobToolTest.Config.class)
public class OracleBlobToolTest {

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
    @ComponentScan(basePackageClasses = OracleBlobToolTest.class)
    static class Config {

        @Value("${blobtool.hostname}") private String hostname;
        @Value("${blobtool.port}") private int port;
        @Value("${blobtool.database}") private String database;
        @Value("${blobtool.user}") private String user;
        @Value("${blobtool.password}") private String password;

        @Bean
        static PropertySourcesPlaceholderConfigurer properties() {
            PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
            Resource[] resources = new Resource[]{new DefaultResourceLoader().getResource("classpath:/oracle-test.properties")};
            pspc.setLocations(resources);
            return pspc;
        }

        @Bean
        public javax.sql.DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("oracle.jdbc.OracleDriver");
            ds.setUrl("jdbc:oracle:thin:" + user + "@//" + hostname + ":" + port + "/" + database);
            ds.setUsername(user);
            ds.setPassword(password);
            JdbcTemplate jt = new JdbcTemplate(ds);
            updateQuietly(jt, "drop sequence blob_storage_id_seq");
            jt.update("create sequence blob_storage_id_seq");
            updateQuietly(jt, "drop table blob_storage");
            jt.update("create table blob_storage (id int primary key, data blob);");
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
            return new ServerSideJdbcBlobTool(dataSource(), compressor(),
                    "select blob_storage_id_seq.nextval from dual",
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


