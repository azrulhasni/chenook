/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import com.azrul.chenook.annotation.WorkField;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class BizUser {



    /**
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }
    
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @WorkField(displayName = "Username")
    private String username;
     
    @WorkField(displayName = "First name")
    private String firstName;
    
    @WorkField(displayName = "Last name")
    private String lastName;
    
    @WorkField(displayName = "Email")
    private String email;
    
    @WorkField(displayName = "Manager")
    private String manager;
    
    @WorkField(displayName = "Group")
    private List<String> groups = new ArrayList<>();
    
    @WorkField(displayName = "Roles")
    private List<String> clientRoles = new ArrayList<>();
    
    @WorkField(displayName = "Enabled")
    private Boolean enabled;
    
//    @Transient
//    @ManyToOne
//    @JoinColumn(name = "work_id", referencedColumnName = "id")
//    private WorkItem workItem;

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String toString(){
        return username;
    }
    
    public String getUserDispalyName() {
        String userDisplayName = getFirstName() + " " + getLastName() + " (" + getUsername() + ")";
        return userDisplayName;
    }

    /**
     * @return the manager
     */
    public String getManager() {
        return manager;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(String manager) {
        this.manager = manager;
    }

    /**
     * @return the groups
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    /**
     * @return the enabled
     */
    public Boolean isEnabled() {
        return getEnabled();
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the roles
     */
    public List<String> getClientRoles() {
        return clientRoles;
    }

    /**
     * @param roles the roles to set
     */
    public void setClientRoles(List<String> roles) {
        this.clientRoles = roles;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BizUser other = (BizUser) obj;
        return Objects.equals(this.username, other.username);
    }
    
    

    /**
     * @return the work
     */
//    public WorkItem getWorkItem() {
//        return workItem;
//    }
//
//    /**
//     * @param work the work to set
//     */
//    public void setWorkItem(WorkItem work) {
//        this.workItem = work;
//    }

   
}
