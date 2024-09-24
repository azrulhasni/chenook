/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.BizUser;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class MapperService {

    private final ModelMapper basicMapper;

   // private final Pattern uidMatcher = Pattern.compile("(?<=uid=)([^,]+)");
    private Pattern userPattern =  Pattern.compile("(?i:(?<=uid=)).*?(?=,[A-Za-z]{0,2}=|$)", Pattern.CASE_INSENSITIVE);

    private final String LDAP_MANAGEER_ATTRIBUTE_NAME = "manager";
    

    public MapperService(
            @Autowired @Qualifier("BasicMapper") ModelMapper basicMapper) {
        this.basicMapper = basicMapper;
        
        this.basicMapper.createTypeMap(UserRepresentation.class, BizUser.class)
                            .addMappings(mapper -> mapper.skip(BizUser::setId));
    }

    public BizUser map(OidcUser oidcUser) {
//        BizUser user = new BizUser();
//        user.setEmail(oidcUser.getEmail());
//        user.setFirstName(oidcUser.getGivenName());
//        user.setLastName(oidcUser.getFamilyName());
//        user.setUsername(oidcUser.getPreferredUsername());
//        return user;
         BizUser bizUser = new BizUser();
        
        List<String> roles = oidcUser
                .getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .map(String::toLowerCase)
                .map(a -> a.replace("role_", ""))
                .collect(Collectors.toList());
        bizUser.setUsername(oidcUser.getPreferredUsername());
        bizUser.setClientRoles(roles);
        bizUser.setEmail(oidcUser.getEmail());
        bizUser.setEnabled(Boolean.TRUE);
        bizUser.setFirstName(oidcUser.getGivenName());
        
        bizUser.setLastName(oidcUser.getFamilyName());
        String manager = oidcUser.getAttribute("manager");
        setManager(manager, bizUser);
        return bizUser;
    }
    
     public void setManager(String manager, BizUser bizUser) {
         if (manager == null) {
            return;
        }
        if (manager.contains("uid=")){//ldap exprreession
            Matcher matcher =userPattern.matcher(manager);
            if (matcher.find()){
                bizUser.setManager(matcher.group(0));
            }
        }else{
            bizUser.setManager(manager);
        }
    }

    public BizUser map(UserRepresentation userRep) {
        BizUser user = basicMapper
                .getTypeMap(UserRepresentation.class, BizUser.class)
                .addMappings(mapper -> mapper.skip(BizUser::setId))
                .map(userRep);

        return user;
    }

    public Set<BizUser> mapBizUsers(List<UserRepresentation> userReps) {
        return userReps.stream()
                .map(ur -> {
                    BizUser user = basicMapper
                            .getTypeMap(UserRepresentation.class, BizUser.class)
                            .addMappings(mapper -> mapper.skip(BizUser::setId))
                            .map(ur);
                    String manager = ur.firstAttribute(LDAP_MANAGEER_ATTRIBUTE_NAME);
                    setManager(manager,user);
                    return user;
                })
                .collect(Collectors.toSet());
    }

}
