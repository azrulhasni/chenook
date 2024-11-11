/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.BizUserRepository;
import com.azrul.chenook.views.common.components.PageNav;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class BizUserService<T extends WorkItem> {

    private final MapperService mapperService;
    private final String keycloakRealm;
    private final Keycloak keycloak;
    private final String clientId;
    private final Integer queryBatchSize;
    private final EntityManagerFactory emFactory;
    private final BizUserRepository bizUserRepo;

    public BizUserService(
            @Autowired MapperService mapperService,
            @Autowired Keycloak keycloak,
            @Autowired EntityManagerFactory emFactory,
            @Autowired BizUserRepository bizUserRepo,
            @Value("${chenook.keycloak.realm}") String keycloakRealm,
            @Value("${chenook.keycloak.client-id}") String clientId,
            @Value("${chenook.keycloak.query-batch-size}") Integer queryBatchSize
    ) {
        this.mapperService = mapperService;
        this.keycloakRealm = keycloakRealm;
        this.clientId = clientId;
        this.keycloak = keycloak;
        this.queryBatchSize = queryBatchSize;
        this.emFactory = emFactory;
        this.bizUserRepo = bizUserRepo;
    }

    public BizUser getUser(String username) {
        List<UserRepresentation> users = keycloak.realm(keycloakRealm).users().searchByUsername(username, true);

        if (users.isEmpty() || users.size() > 1) {
            return null;
        }
        UserRepresentation userRep = users.iterator().next();
        List<RoleRepresentation> roles = keycloak.realm(keycloakRealm).users().get(userRep.getId()).roles().clientLevel(clientId).listAll();
        BizUser bizUser = mapperService.map(userRep);
        for (var r : roles) {
            bizUser.getClientRoles().add(r.getName());
        }
        if (userRep.getAttributes() != null) {
            List<String> lmanager = userRep.getAttributes().get("manager");
            if (lmanager != null && !lmanager.isEmpty()) {
                String manager = lmanager.get(0);
                mapperService.setManager(manager, bizUser);
            }
        }
        return bizUser;
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

    public DataProvider<BizUser,Void> getOwnersByWork(T work, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<BizUser, Void>() {
            @Override
            protected Stream<BizUser> fetchFromBackEnd(Query<BizUser, Void> query) {

                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sortedField = pageNav.getSortField();
                SessionFactory sessionFactory = emFactory.unwrap(SessionFactory.class);
                AbstractEntityPersister persister = ((AbstractEntityPersister) ((MappingMetamodel) sessionFactory.getMetamodel()).getEntityDescriptor(WorkItem.class));
                String sorted = persister.getPropertyColumnNames(sortedField)[0];

                query.getPage();
                Page<BizUser> finapps = bizUserRepo.findOwnersByWork(work.getId(),
                        PageRequest.of(
                                pageNav.getPage() - 1,
                                pageNav.getMaxCountPerPage(),
                                Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<BizUser, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(BizUser item) {
                return item.getId().toString();
            }

        };
        return dp;
    }

    public Integer countWorkByOwner(T work) {
        Long count = bizUserRepo.countOwnersByWork(work.getId());
        return count.intValue();
    }
    
    public BizUser save(BizUser user){
        return bizUserRepo.save(user);
    }

}
