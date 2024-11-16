/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

/**
 *
 * @author azrul
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.views.common.components.PageNav;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public interface BizUserService<T extends WorkItem> {

    public BizUser getUser(String username);

    public List<BizUser> getUsersByRole(String role);

    public String getFullName(String username);
    
    public DataProvider<BizUser,Void> getOwnersByWork(T work, PageNav pageNav);

    public Integer countWorkByOwner(T work);
    
    public BizUser save(BizUser user);

}

