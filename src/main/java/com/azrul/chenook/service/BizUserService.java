/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.BizUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.typesense.api.Client;
import org.typesense.api.Document;
import org.typesense.api.Documents;
import org.typesense.model.CollectionSchema;
import org.typesense.model.ImportDocumentsParameters;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;
import org.typesense.model.SearchResultHit;

/**
 *
 * @author azrul
 */
@Service
public class BizUserService {

    private final MapperService mapperService;
    private final ObjectMapper objectToKeyValueMaper = new ObjectMapper();
    private final String keycloakRealm;
    //private final SearchEngine searchEngine;
    private final Integer allUsersMaxCount;
    private final Keycloak keycloak;
    private final String clientId;
    private final Integer queryBatchSize;
    
    private Pattern userPattern =  Pattern.compile("(?i:(?<=uid=)).*?(?=,[A-Za-z]{0,2}=|$)", Pattern.CASE_INSENSITIVE);

    public BizUserService(
            @Autowired MapperService mapperService,
            @Autowired Keycloak keycloak,
            @Value("${chenook.keycloak.realm}") String keycloakRealm,
            @Value("${chenook.keycloak.client-id}") String clientId,
            @Value("${chenook.keycloak.query-batch-size}") Integer queryBatchSize,
            @Value("${typesense.users.allUsersMaxCount}") Integer allUsersMaxCount
    ) {
        this.mapperService = mapperService;
        this.allUsersMaxCount = allUsersMaxCount;
        this.keycloakRealm = keycloakRealm;
        this.clientId = clientId;
        this.keycloak = keycloak;
        this.queryBatchSize = queryBatchSize;
        //this.searchEngine=searchEngine;
    }

//    public void reloadUsers(List<UserRepresentation> userReps){
//        try {
//            objectToKeyValueMaper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            Set<BizUser> users = mapperService.mapBizUsers(userReps);
//            List<Map<String,Object>> usersAsMaps = new ArrayList<>();
//            for (BizUser u:users){
//                Map<String, Object> map = objectToKeyValueMaper.convertValue(u, new TypeReference<Map<String, Object>>() {});
//                map.put("id",u.getUsername());
//                usersAsMaps.add(map);
//            }
//           
//            
//            ImportDocumentsParameters importDocumentsParameters = new ImportDocumentsParameters();
//            importDocumentsParameters.action("create");
//            searchEngine.get4Users().import_(usersAsMaps, importDocumentsParameters);
//        } catch (Exception ex) {
//            Logger.getLogger(BizUserService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    public BizUser getUser(String username){
//        try {
//            objectToKeyValueMaper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            var userMap = searchEngine.get4Users(username).retrieve();
//            return objectToKeyValueMaper.convertValue(userMap, BizUser.class);
//        } catch (Exception ex) {
//            Logger.getLogger(BizUserService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
    
    public BizUser getUser(String username){
        List<UserRepresentation> users = keycloak.realm(keycloakRealm).users().searchByUsername(username, true);
        
        if (users.isEmpty() || users.size()>1){
            return null;
        }
        UserRepresentation userRep = users.iterator().next();
        List<RoleRepresentation> roles = keycloak.realm(keycloakRealm).users().get(userRep.getId()).roles().clientLevel(clientId).listAll();
        BizUser bizUser = mapperService.map(userRep);
        for (var r:roles){
            bizUser.getClientRoles().add(r.getName());
        }
        List<String> lmanager = userRep.getAttributes().get("manager");
        if (lmanager!=null && !lmanager.isEmpty()){
            String manager = lmanager.get(0);
            if (manager.contains("uid=")){//ldap exprreession
                Matcher matcher =userPattern.matcher(manager);
                if (matcher.find()){
                    bizUser.setManager(matcher.group(0));
                }
            }else{
                bizUser.setManager(manager);
            }
        }
        return bizUser;
//        UserResource ur = keycloak
//                .realm(keycloakRealm)
//                .users()
//                .get(username);
//        return mapperService.map(ur.toRepresentation());
    }
    
    public List<BizUser> getUsersByRole(String role) {
        int p = 0;
        List<BizUser> bizUsers = new ArrayList<>();

        while (true) {
            List<UserRepresentation> pagedUsers = keycloak.realm(keycloakRealm).clients().get(clientId).roles().get(role).getUserMembers(p * queryBatchSize, queryBatchSize);
            Set<BizUser> users = mapperService.mapBizUsers(pagedUsers);
            bizUsers.addAll(users);
            p++;
            if (pagedUsers.size() < queryBatchSize) {
                break;
            }
        }
        return bizUsers;
    }
    
 

    public String getFullName(String username) {
        BizUser user = getUser(username);
        return user.getFirstName() + " " + user.getLastName();
    }

//    public Set<BizUser> getUsers(Set<String> usernames){
//        try {
//            objectToKeyValueMaper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            Set<BizUser> users = new HashSet<>();
//            String usernameFilter = "id:[" + String.join(", ",usernames) +  "]";
//            SearchParameters searchParameters = new SearchParameters()
//                                        .q("*")
//                                        .filterBy(usernameFilter);
//            
//            SearchResult searchResult = searchEngine.get4Users().search(searchParameters);
//            for (SearchResultHit hit:searchResult.getHits()){
//                users.add(objectToKeyValueMaper.convertValue(hit.getDocument(), BizUser.class));
//            }
//            return users;
//        } catch (Exception ex) {
//            Logger.getLogger(BizUserService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//    
//    public Set<BizUser> searchUsers(String queryString, Set<String> filteredOutUsernames){
//        try {
//            objectToKeyValueMaper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            Set<BizUser> users = new HashSet<>();
//            SearchParameters searchParameters = new SearchParameters()
//                                        .q(queryString)
//                                        .filterBy("id:!=["+ String.join(", ",filteredOutUsernames)+"]")
//                                        .queryBy("username,firstName,lastName,email")
//                                        .sortBy("username:asc")
//                                        .limit(allUsersMaxCount);
//            
//            SearchResult searchResult = searchEngine.get4Users().search(searchParameters);
//            for (SearchResultHit hit:searchResult.getHits()){
//                users.add(objectToKeyValueMaper.convertValue(hit.getDocument(), BizUser.class));
//            }
//            return users;
//        } catch (Exception ex) {
//            Logger.getLogger(BizUserService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//    
}
