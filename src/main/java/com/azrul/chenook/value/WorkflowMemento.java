/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.value;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorkflowMemento<T> {
    private T parent;
    private Long parentId;
    private OidcUser oidcUser;
    private BizProcess bizProcess;
    private String context;

    public WorkflowMemento(T parent, Long parentId, OidcUser oidcUser, BizProcess bizProcess, String context) {
        this.parent = parent;
        //this.parentId = parentId;
        this.oidcUser = oidcUser;
        this.bizProcess = bizProcess;
        this.context = context;
    }

    /**
     * @return the parent
     */
    public T getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(T parent) {
        this.parent = parent;
    }

    /**
     * @return the parentId
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the oidcUser
     */
    public OidcUser getOidcUser() {
        return oidcUser;
    }

    /**
     * @param oidcUser the oidcUser to set
     */
    public void setOidcUser(OidcUser oidcUser) {
        this.oidcUser = oidcUser;
    }

    /**
     * @return the bizProcess
     */
    public BizProcess getBizProcess() {
        return bizProcess;
    }

    /**
     * @param bizProcess the bizProcess to set
     */
    public void setBizProcess(BizProcess bizProcess) {
        this.bizProcess = bizProcess;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }
    
    
    
}
