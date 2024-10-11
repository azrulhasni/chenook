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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;


@Configuration
public class SearchConfig extends ElasticsearchConfiguration {
     @Value("${http.server.ssl.trust-store}")
    private Resource trustStore;

    @Value("${http.server.ssl.trust-store-password}")
    private String trustStorePassword;

	@Override
	public ClientConfiguration clientConfiguration() {
         try {
             final SSLContext sslContext = SSLContexts.custom()
                     .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                     .build();
             return ClientConfiguration.builder()
                     .connectedTo("localhost:9200")
                     .usingSsl(sslContext)
                     .withBasicAuth("elastic", "eqwabeWorxxjO+WXCsWO")
                     .build();
         } catch (IOException ex) {
             Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (KeyStoreException ex) {
             Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (CertificateException ex) {
             Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
         } catch (KeyManagementException ex) {
             Logger.getLogger(SearchConfig.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
	}
}