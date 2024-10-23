/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import com.vaadin.flow.component.html.Div;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorkflowAwareGroup<T extends WorkItem> extends Div {

    private final T workItem;
    private Predicate<T> visibleCondition;
    private Predicate<T> enableCondition;
    
    

    private WorkflowAwareGroup(T workItem) {
        this.workItem = workItem;
        this.visibleCondition = null;
        this.enableCondition = null;
    }

    private WorkflowAwareGroup(
            Predicate<T> visibleCondition,
            Predicate<T> enableCondition,
            T workItem
    ) {
        this.workItem = workItem;
        this.visibleCondition = visibleCondition;
        this.enableCondition = enableCondition;
    }

    public Boolean calculateEnable() {
        return getEnableCondition().test(workItem);
    }

    public Boolean calculateVisible() {
        return getVisibleCondition().test(workItem);
    }

    public static <T extends WorkItem> WorkflowAwareGroup or(T workItem, WorkflowAwareGroup... orGroups) {

        WorkflowAwareGroup newGroup = new WorkflowAwareGroup(workItem);

        for (WorkflowAwareGroup orGroup : orGroups) {
            if (newGroup.visibleCondition == null) {
                newGroup.visibleCondition = orGroup.visibleCondition;
            } else {
                newGroup.visibleCondition = newGroup.visibleCondition.or(orGroup.visibleCondition);
            }

            if (newGroup.enableCondition == null) {
                newGroup.enableCondition = orGroup.enableCondition;
            } else {
                newGroup.enableCondition = newGroup.enableCondition.or(orGroup.enableCondition);
            }
        }
        return newGroup;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createSaveAndSubmitBtnGroup(
            final T item,
            final OidcUser user) {
        Predicate<T> visiblePred = w -> {
            return true;
        };
        Predicate<T> enabledPred = w -> {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //always allow save/submit 
                    return true;
                //... or my approval is needed
                } else{
                    if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                        return true;
                    } else {
                        return false;
                    }
                }
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createCancelBtnGroup(
            final T item,
            final OidcUser user) {
        Predicate<T> visiblePred = w -> {
            return true;
        };
        Predicate<T> enabledPred = w -> {
            return true;
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createRemoveBtnGroup(
            final T item,
            final OidcUser user,
            final BizProcess bizProcess) {
        Predicate<T> visiblePred = w -> {
            return true;
        };
        Predicate<T> enabledPred = w -> {
            //if user is the creator
            if (StringUtils.equals(
                    item.getCreator(),
                    user.getPreferredUsername()
            )) {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //...and the workflow is at start (either just started or being routed there after escalation approval 
                    if (bizProcess.getStartEvents()
                            .stream()
                            .anyMatch(e -> e.getId().equals(item.getWorklist())
                            )) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createForApprovalPanel(
            final T workItem,
            final OidcUser user
    ) {

        Predicate<T> visiblePred = w -> {
            return true;
        };

        Predicate<T> enabledPred = w -> {
            if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                return true;
            } else {
                return false;
            }
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, workItem);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createApproverForButtons(
            final T workItem,
            final OidcUser user
    ) {

        Predicate<T> visiblePred = w -> {
            if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                return true;
            } else {
                return false;
            }
        };

        Predicate<T> enabledPred = w -> {
            if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                return true;
            } else {
                return false;
            }
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, workItem);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createDefaultForForm(
            final T item,
            final OidcUser user,
            final BizProcess bizProcess,
            //final Boolean filterByWorklist,
            final Set<String> worklistsWhereItemIsVisible,
            final Set<String> worklistsWhereItemIsEnabled
    ) {

        Predicate<T> visiblePred = w -> {
            //Case of creator:
            //----------------

            //if user is the creator
            if (StringUtils.equals(
                    item.getCreator(),
                    user.getPreferredUsername()
            )) {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //...and the workflow is at start (either just started or being routed there after escalation approval 
                    if (bizProcess.getStartEvents()
                            .stream()
                            .anyMatch(e -> e.getId().equals(item.getWorklist())
                            )) {
                        //...if we are filtering
                        if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                            //... and the item is allowed to be visible
                            if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    //... but not owner
                    //...if we are filtering
                    if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                        if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
                //if the user is the owner
            } else if (item.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {

                if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                    //... and the item is allowed to be visible
                    //...if we are filtering
                    if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                        return true;
                    } else {
                        return false;
                    }
                    
                } else {
                    return true;
                }
            } else {
                //...if you need to approve something
                if (w.getApprovals().stream()
                        .map(a -> a.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))){
                    return true;
                }else{
                    return false;
                }
            }
        };

        Predicate<T> enabledPred = w -> {
            //Case of creator:
            //----------------

            //if user is the creator
            if (StringUtils.equals(
                    item.getCreator(),
                    user.getPreferredUsername()
            )) {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //...and the workflow is at start (either just started or being routed there after escalation approval 
                    if (bizProcess.getStartEvents()
                            .stream()
                            .anyMatch(e -> e.getId().equals(item.getWorklist())
                            )) {
                        if (!worklistsWhereItemIsEnabled.contains("ANY_WORKLIST")) {
                            //... and the item is allowed to be visible
                            if (worklistsWhereItemIsEnabled.contains(item.getWorklist())) {
                                return true;
                            } else {
                                return false;
                            }
                        }else{
                            return true; 
                        }
                    } else {
                        return false;
                    }
                } else {
                    //... but not owner
                    return false;
                }
                //if the user is the owner
            } else if (item.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                //...if we are filtering
                if (!worklistsWhereItemIsEnabled.contains("ANY_WORKLIST")) {
                    //... and the item is allowed to be visible
                    if (worklistsWhereItemIsEnabled.contains(item.getWorklist())) {
                        //...if you need to approve something
                        if (w.getApprovals().stream()
                                .map(a -> a.getUsername())
                                .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))){
                            return false;
                        }else{
                            return true;
                        }
                    } else {
                        return false;
                    }
                }else{
                    return true;
                }
            } else {
                return false;
            }
        };

        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createDefaultForSubmissionButtons(
            final T item,
            final OidcUser user,
            final BizProcess bizProcess,
            final Set<String> worklistsWhereItemIsVisible,
            final Set<String> worklistsWhereItemIsEnabled
    ) {

        Predicate<T> visiblePred = w -> {
            return true;
        };

        Predicate<T> enabledPred = w -> {
            if (item.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                return true;
            } else {
                //...if you need to approve something
                if (w.getApprovals().stream()
                        .map(a -> a.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))){
                    return true;
                }else{
                    return false;
                }
            }
        };

        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }


    /**
     * @return the visibleCondition
     */
    public Predicate<T> getVisibleCondition() {
        return visibleCondition;
    }

    /**
     * @param visibleCondition the visibleCondition to set
     */
    public void setVisibleCondition(Predicate<T> visibleCondition) {
        this.visibleCondition = visibleCondition;
    }

    /**
     * @return the enableCondition
     */
    public Predicate<T> getEnableCondition() {
        return enableCondition;
    }

    /**
     * @param enableCondition the enableCondition to set
     */
    public void setEnableCondition(Predicate<T> enableCondition) {
        this.enableCondition = enableCondition;
    }
}
