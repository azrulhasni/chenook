/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.service;

/**
 *
 * @author azrul
 */

    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.ApproverLookup;
import com.azrul.chenook.service.BizUserService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Qualifier;


@Service
@Qualifier("Manager")
public class BizUserManagerLookup <T extends WorkItem> implements ApproverLookup<T> {

    @Autowired
    BizUserService bizUserService;

    @Override
    public Optional<BizUser> lookupApprover(T work, String userIdentifier) {
        BizUser bizUser = bizUserService.getUser(userIdentifier);
        return Optional.ofNullable(bizUser);
    }


}
