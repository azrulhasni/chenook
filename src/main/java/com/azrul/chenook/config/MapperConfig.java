/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.config;

import com.azrul.chenook.domain.BizUser;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author azrul
 */
@Configuration
public class MapperConfig {

  
    
     @Bean("BasicMapper")
    public ModelMapper getBasicMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC);
        return modelMapper;
    }
    
//     @Bean("BizUserMapper")
//    public ModelMapper getBizUserMapper() {
//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration()
//                .setFieldMatchingEnabled(true)
//                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC);
//        modelMapper.typeMap(UserRepresentation.class, BizUser.class).addMappings(mapper -> mapper.skip(BizUser::setId));
//               
//        return modelMapper;
//    }
   
}
