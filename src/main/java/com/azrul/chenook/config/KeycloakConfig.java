/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author azrul
 */
@Configuration
public class KeycloakConfig {
    @Value("${chenook.keycloak.username}")
    private String keycloakUsername;
    
    @Value("${chenook.keycloak.password}")
    private String keycloakPassword;
    
    @Value("${chenook.keycloak.url}")
    private String keycloakUrl;
    
    @Value("${chenook.keycloak.client-id}")
    private String keycloakClientId;
    
    @Value("${chenook.keycloak.client-name}")
    private String keycloakClientName;
    
    @Value("${chenook.keycloak.realm}")
    private String keycloakRealm;

    @Bean
    Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(keycloakRealm)
                .clientId(keycloakClientName)
                .grantType(OAuth2Constants.PASSWORD)
                .username(keycloakUsername)
                .password(keycloakPassword)
                .build();
    }
}
