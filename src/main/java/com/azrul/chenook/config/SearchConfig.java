package com.azrul.chenook.config;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = {"com.azrul.smefinancing.search.repository","com.azrul.chenook.search.repository"})
@ComponentScan(basePackages = {"com.azrul.search.*"})
public class SearchConfig extends ElasticsearchConfiguration {

    @Value("${http.server.ssl.trust-store}")
    private Resource trustStore;

    @Value("${http.server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${spring.data.elasticsearch.username}")
    private String elasticSearchUsername;

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String[] elasticSearchClusterNodes;

    @Value("${spring.data.elasticsearch.password}")
    private String elasticSearchPassword;

    @Override
    public ClientConfiguration clientConfiguration() {
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                    .build();
            return ClientConfiguration.builder()
                    .connectedTo(elasticSearchClusterNodes)
                    .usingSsl(sslContext)
                    .withBasicAuth(elasticSearchUsername, elasticSearchPassword)
                    .build();
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException ex) {
            Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
