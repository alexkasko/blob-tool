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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;

import java.io.IOException;


/**
* User: alexey
* Date: 4/25/12
*/

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = PostgresBlobToolTest.Config.class)
public class PostgresBlobToolTest {

    @Inject
    private BlobTestService service;

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
    @ComponentScan(basePackageClasses = PostgresBlobToolTest.class)
    static class Config {

        @Value("${blobtool.hostname}") private String hostname;
        @Value("${blobtool.port}") private int port;
        @Value("${blobtool.database}") private String database;
        @Value("${blobtool.user}") private String user;
        @Value("${blobtool.password}") private String password;

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
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUrl("jdbc:postgresql://" + hostname + ":" + port + "/" + database);
            ds.setUsername(user);
            ds.setPassword(password);
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
            return new PostgresBlobTool(dataSource(), compressor());
        }
    }
}


