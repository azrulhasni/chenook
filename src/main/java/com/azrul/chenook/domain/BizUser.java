/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author azrul
 */
public class BizUser {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String manager;
    private List<String> groups = new ArrayList<>();
    private List<String> clientRoles = new ArrayList<>();
    private Boolean enabled;

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
        return enabled;
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

   
}
