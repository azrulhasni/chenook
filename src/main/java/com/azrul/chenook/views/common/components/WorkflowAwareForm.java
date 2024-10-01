/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorkflowAwareForm<T extends WorkItem> extends FormLayout{
    private final T workItem;
    private final Predicate<T> visibleCondition;
    private final Predicate<T> enableCondition;
    private final Set<HasEnabled> managedComponents = new HashSet<>();
    
    private WorkflowAwareForm(
            Predicate<T> visibleCondition, 
            Predicate<T> enableCondition,
            T workItem
        ){
        this.workItem = workItem;
        this.visibleCondition=visibleCondition;
        this.enableCondition=enableCondition;
    }
    
    public void addManagedComponents(HasEnabled... components){
        managedComponents.addAll(Arrays.asList(components));
        boolean enable = enableCondition.test(workItem);
        boolean visible = visibleCondition.test(workItem);
        for (HasEnabled component:components){
            component.setEnabled(enable);
            Component c = ((Component)component);
            c.setVisible(visible);
            this.add(c);
        }
    }
    
     public void addManagedComponent(HasEnabled component){
         addManagedComponent(component,1);
     }
    
    public void addManagedComponent(HasEnabled component, Integer colspan){
        managedComponents.add(component);
        boolean enable = enableCondition.test(workItem);
        boolean visible = visibleCondition.test(workItem);
        
        
        if (component instanceof Component c){
            c.setVisible(visible);
        }
        if (component instanceof HasValueAndElement c){
            c.setReadOnly(!enable);
        }else{
            component.setEnabled(enable);
        }
        this.add((Component)component, colspan);
    }
    
    public void refresh(){
        boolean enable = enableCondition.test(workItem);
        boolean visible = visibleCondition.test(workItem);
        for (HasEnabled component:managedComponents){
            component.setEnabled(enable);
            Component c = ((Component)component);
            c.setVisible(visible);
            this.add(c);
        }
    }
    
    public void calculateEnable(){
       
        boolean enable = enableCondition.test(workItem);
        this.setEnabled(enable);
    }
    
    public void calculateVisible(){
        boolean visible = visibleCondition.test(workItem);
        this.setVisible(visible);
    }
    
    private static <T extends WorkItem> Predicate<T> getDefaultVisible(OidcUser user){
        Predicate<T> visibleRule = (T item)->{
            if (StringUtils.equals(item.getCreator(),user.getPreferredUsername())){
                return true;
            }else if (item.getOwners().stream().map(bu->bu.getUsername()).anyMatch(u->StringUtils.equals(u,user.getPreferredUsername()))){
                return true; 
            }else if (item.getApprovals().stream().map(a->a.getUsername()).anyMatch(u->StringUtils.equals(u,user.getPreferredUsername()))){
                return true;
            }else{
                return false;
            }
            
        };
        return visibleRule;
    }
    
   
    
    private static <T extends WorkItem> Predicate<T> getDefaultEnabled(
             final OidcUser user,
             final BizProcess bizProcess,
             final Set<String> worklistsWhereItemIsEnabled
     ){
        Predicate<T> enabledRule = (T item)->{
            
            if (getDefaultVisible(user).test(item)==false){
                return false;
            }
            //if the workflow is at start (either just started or being routed there) and the user is the creator & owner, allow edition 
            if (bizProcess.getStartEvents()
                    .stream()
                    .anyMatch(e->e.getId().equals(item.getWorklist())
                ) &&
                StringUtils.equals(
                    item.getCreator(),
                    user.getPreferredUsername()
                ) &&
                item.getOwners()
                    .stream()
                    .map(bu->bu.getUsername())
                    .anyMatch(u->StringUtils.equals(u,user.getPreferredUsername()))){
                return true;
            }else{
                //if we set worklistsWhereItemIsEnabled and the item is in that worklist and the use is the owner, then enable
                if (worklistsWhereItemIsEnabled!=null || worklistsWhereItemIsEnabled.isEmpty()){
                    return false;
                }
                if (worklistsWhereItemIsEnabled
                        .contains(
                                item.getWorklist()
                        ) &&
                    (item
                        .getOwners()
                        .stream()
                        .map(bu->bu.getUsername())
                        .anyMatch(u->StringUtils.equals(u,user.getPreferredUsername())))){
                    return true;
                }else{
                    return false;
                }
            }
        };
        return enabledRule;
    }
    
    public static <T extends WorkItem>  WorkflowAwareForm create(
            final OidcUser user,
            final T workItem,
            final BizProcess bizProcess,
            final Set<String> worklistsWhereItemIsEnabled
             ){
        Predicate<T> visiblePred = getDefaultVisible(user);
        Predicate<T> enablePred = getDefaultEnabled(user, bizProcess, worklistsWhereItemIsEnabled);
        WorkflowAwareForm group = new WorkflowAwareForm(visiblePred, enablePred,  workItem);
        return group;
    }
    
     public static <T extends WorkItem>  WorkflowAwareForm create(
            final OidcUser user,
            final T workItem,
            final BizProcess bizProcess
             ){
        Predicate<T> visiblePred = getDefaultVisible(user);
        Predicate<T> enablePred = getDefaultEnabled(user, bizProcess, Set.of());
        WorkflowAwareForm group = new WorkflowAwareForm(visiblePred, enablePred,  workItem);
        return group;
    }
}
