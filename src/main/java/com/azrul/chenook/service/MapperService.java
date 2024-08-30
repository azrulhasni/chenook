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
    
    private final Pattern uidMatcher = Pattern.compile("(?<=uid=)([^,]+)");
    private final String LDAP_MANAGEER_ATTRIBUTE_NAME="manager";

    public MapperService(
            @Autowired @Qualifier("BasicMapper") ModelMapper basicMapper) {
        this.basicMapper = basicMapper;
    }

  
    
    public BizUser map(OidcUser oidcUser){
        BizUser user = new BizUser();
        user.setEmail(oidcUser.getEmail());
        user.setFirstName(oidcUser.getGivenName());
        user.setLastName(oidcUser.getFamilyName());
        user.setUsername(oidcUser.getPreferredUsername());
        return user;
    }
    
    public BizUser map(UserRepresentation userRep){
        BizUser user = basicMapper.map(userRep, BizUser.class);
        return user;
    }
    
    public Set<BizUser> mapBizUsers(List<UserRepresentation> userReps){
        return userReps.stream()
                .map(ur -> {
                    BizUser user = basicMapper.map(ur, BizUser.class);
                    String manager = ur.firstAttribute(LDAP_MANAGEER_ATTRIBUTE_NAME);
                    if (StringUtils.isNotBlank(manager)){
                        Matcher matcher =uidMatcher.matcher(manager);
                        if (matcher.find(1)){
                            user.setManager(matcher.group(1));
                        }
                    }
                    return user;    
                })
                .collect(Collectors.toSet());
    }

    
}
