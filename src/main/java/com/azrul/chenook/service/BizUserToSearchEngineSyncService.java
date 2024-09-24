/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class BizUserToSearchEngineSyncService {

    private final Keycloak keycloak;
    private final BizUserService userService;
    private final SearchEngine searchEngine;
    private final String keycloakRealm;
    private final Integer syncBatchSize;
    private final String typesenseUsersAlias;

    public BizUserToSearchEngineSyncService(
            @Autowired Keycloak keycloak,
            @Autowired BizUserService userService,
            @Autowired SearchEngine searchEngine,
            @Value("${chenook.keycloak.realm}") String keycloakRealm,
            @Value("${typesense.users.alias}") String typesenseUsersAlias,
            @Value("${chenook.keycloak.typesense.usersync.batchsize}") Integer syncBatchSize
    ) {
        this.keycloak = keycloak;
        this.userService = userService;
        this.keycloakRealm = keycloakRealm;
        this.syncBatchSize = syncBatchSize;
        this.searchEngine = searchEngine;
        this.typesenseUsersAlias = typesenseUsersAlias;
    }

    
        
        
//        int allUsersCount = keycloak.realm(keycloakRealm).users().count();
//        Integer pages = (int)Math.ceil((double)allUsersCount/syncBatchSize);
//        
//        for (int p=0;p<pages;p++){
//            List<UserRepresentation> pagedUsers = keycloak.realm(keycloakRealm).users().list(p*syncBatchSize,syncBatchSize-1);
//            userService.reloadUsers(pagedUsers);
//        }

//            List<UserRepresentation> allUsers = keycloak
//                    .realm(keycloakRealm)
//                    .users()
//                    .list();
//                    .stream()
//                    .filter(u -> !this.keycloakUsername.equals(u.getUsername())
//                    ) // filter system user
//                    .collect(Collectors.toMap(UserRepresentation::getUsername, u -> mapperService.map(u)));
//    }

}
