package com.alexkasko.springjdbc.blob;

import com.alexkasko.springjdbc.compress.Compressor;
import com.alexkasko.springjdbc.compress.GzipCompressor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = H2BlobToolTest.Config.class)
public class H2BlobToolTest {

    @Inject
    private BlobTestService service;
    @Inject
    private DataSource dataSource;

    @Before
    public void prepareBlobsTable() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.update("drop sequence if exists blob_storage_id_seq");
        jt.update("drop table if exists blob_storage");
        jt.update("create sequence blob_storage_id_seq");
        jt.update("create table blob_storage (id bigint primary key, data blob)");
    }

    @Test
    public void test() throws IOException {
        long id = service.create();
        service.read(id);
        service.detach(id);
        service.delete(id);
    }

    @Test(expected = BlobException.class)
    public void testDelete() throws IOException {
        long id = service.create();
        service.delete(id);
        service.read(id);
    }

    @Configuration
    @EnableTransactionManagement
    @ComponentScan(basePackageClasses = H2BlobToolTest.class)
    static class Config {
        @Bean
        public javax.sql.DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
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
            return new TempFileJdbcBlobTool(dataSource(), compressor());
        }
    }
}


