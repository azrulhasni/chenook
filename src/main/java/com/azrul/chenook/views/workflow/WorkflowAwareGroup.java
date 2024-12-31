/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import com.vaadin.flow.component.html.Div;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
            Predicate<T> enableCondition
     ){
        this.workItem = null;
        this.visibleCondition = visibleCondition;
        this.enableCondition = enableCondition;
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
            final BizUser user) {
        Predicate<T> visiblePred = w -> {
            return true;
        };
        Predicate<T> enabledPred = w -> {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getUsername()))) {
                    //always allow save/submit 
                    return true;
                //... or my approval is needed
                } else{
                    if (w.getApprovals()==null) {
                        return false;
                    }
                    if (w.getApprovals().isEmpty()) {
                        return false;
                    }
                    if (w.getApprovals()
                            .stream()
                            .map(a -> a.getUsername())
                            .anyMatch(u -> 
                                    StringUtils.equals(u, user.getUsername())
                            )
                    ) {
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
            final BizUser user) {
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
            final BizUser user,
            final BizProcess bizProcess) {
        Predicate<T> visiblePred = w -> {
            return true;
        };
        Predicate<T> enabledPred = w -> {
            //if user is the creator
            if (StringUtils.equals(
                    item.getCreator(),
                    user.getUsername()
            )) {
                //if the user is the owner
                if (item.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getUsername()))) {
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
            final BizUser user
    ) {

        Predicate<T> visiblePred = w -> {
            return true;
        };

        Predicate<T> enabledPred = w -> {
            if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getUsername()))) {
                return true;
            } else {
                return false;
            }
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, workItem);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createForApproverrButtons(
            final T workItem,
            final BizUser user,
             final BizProcess bizProcess
    ) {
        
        Predicate<T> visiblePred = w -> {
            Boolean  approvalNeeded = w.getApprovals()!=null && !w.getApprovals().isEmpty();
            return approvalNeeded;
        };

        Predicate<T> enabledPred = w -> {
            Boolean  approvalNeeded = w.getApprovals()!=null && !w.getApprovals().isEmpty();
            Boolean isApprover = w.getApprovals()==null?false:w.getApprovals().stream()
                                .map(a -> a.getUsername())
                                .anyMatch(u -> StringUtils.equals(u, user.getUsername()));
                             
            return (approvalNeeded && isApprover);
        };
        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, workItem);
        return group;
    }
    
    
    //in the default settings components are visible everywhere and is editable only at the start of the workflow when the WorkItem is created 
    public static <T extends WorkItem> WorkflowAwareGroup createDefaultForForm(
            final T item,
            final BizUser user,
            final BizProcess bizProcess
    ){
        Set<String> enabled = new HashSet<>();
        
        //ASSUMPTION: A work must be editable at the beginning of the workflow (at creation)
        enabled.addAll(bizProcess
                        .getStartEvents()
                        .stream()
                        .map(se->se.getId())
                        .collect(Collectors.toSet())
                );
        
        return createForForm(
                item,
                user,
                bizProcess, 
                Set.of("ANY_WORKLIST"),
                enabled);
    }
    
    //in the default settings components are visible everywhere and is editable only at the start of the workflow when the WorkItem is created 
    public static <T extends WorkItem> WorkflowAwareGroup createDefaultForFormWithExtToDirectActivities(
            final T item,
            final BizUser user,
            final BizProcess bizProcess
    ){
        Set<String> enabled = new HashSet<>();
        
        //ASSUMPTION: A work must be editable at the beginning of the workflow (at creation)
        enabled.addAll(bizProcess
                        .getStartEvents()
                        .stream()
                        .map(se->se.getId())
                        .collect(Collectors.toSet())
                );
        
        //ASSUMPTION: A work must be editable if it is directly sent to a human being
        enabled.addAll(bizProcess
                        .getDirectHuman()
                        .stream()
                        .map(se->se.getId())
                        .collect(Collectors.toSet())
                );
        
                
        
        return createForForm(
                item,
                user,
                bizProcess, 
                Set.of("ANY_WORKLIST"),
                enabled);
    }

    public static <T extends WorkItem> WorkflowAwareGroup createForForm(
            final T item,
            final BizUser user,
            final BizProcess bizProcess,
            final Set<String> worklistsWhereItemIsVisible,
            final Set<String> worklistsWhereItemIsEnabled
    ) {

        Predicate<T> visiblePred = w -> {
            return worklistsWhereItemIsVisible.contains("ANY_WORKLIST")||
                    worklistsWhereItemIsVisible.contains(w.getWorklist());
        };

        Predicate<T> enabledPred = w -> {
             Boolean isOwner = w.getOwners()==null?false:w.getOwners().stream()
                                .map(a -> a.getUsername())
                                .anyMatch(u -> StringUtils.equals(u, user.getUsername()));
             Boolean isApprover = w.getApprovals()==null?false:w.getApprovals().stream()
                                .map(a -> a.getUsername())
                                .anyMatch(u -> StringUtils.equals(u, user.getUsername()));
             Boolean a = worklistsWhereItemIsEnabled.contains("ANY_WORKLIST");
             Boolean b = worklistsWhereItemIsEnabled.contains(w.getWorklist());
             
             Boolean inWorklistWhereItemEnabled = a||b;
             return isOwner && inWorklistWhereItemEnabled && !isApprover;
        };

        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }
    
    public static <T extends WorkItem> WorkflowAwareGroup createNoConstraintsGroup() {

        Predicate<T> visiblePred = w -> {
            return true;
        };

        Predicate<T> enabledPred = w -> {
            return true;
        };

        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred);
        return group;
    }

    public static <T extends WorkItem> WorkflowAwareGroup createForSubmissionButtons(
            final T item,
            final BizUser user,
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
                    .anyMatch(u -> StringUtils.equals(u, user.getUsername()))) {
                return true;
            } else {
                //...if you need to approve something
                if (w.getApprovals().stream()
                        .map(a -> a.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getUsername()))){
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

/**
 * public static <T extends WorkItem> WorkflowAwareGroup createApproverForButtons(
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
                    w.getCreator(),
                    user.getPreferredUsername()
            )) {
                //if the user is the owner
                if (w.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //...and the workflow is at start (either just started or being routed there after escalation approval 
                    if (bizProcess.getStartEvents()
                            .stream()
                            .anyMatch(e -> e.getId().equals(w.getWorklist())
                            )) {
                        
                        return true;
                    } else {
                        //...if we are filtering
                        if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                            //... and the item is allowed to be visible . E.g. for S10.INFORM_CUSTOMER 
                            if (worklistsWhereItemIsVisible.contains(w.getWorklist())) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }
                    }
                } else {
                    //... but not owner
                    //...if we are filtering
                    if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                        if (worklistsWhereItemIsVisible.contains(w.getWorklist())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
                //if the user is the owner
            } else if (w.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {

                if (!worklistsWhereItemIsVisible.contains("ANY_WORKLIST")) {
                    //... and the item is allowed to be visible
                    //...if we are filtering
                    if (worklistsWhereItemIsVisible.contains(w.getWorklist())) {
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
                    w.getCreator(),
                    user.getPreferredUsername()
            )) {
                //if the user is the owner
                if (w.getOwners()
                        .stream()
                        .map(bu -> bu.getUsername())
                        .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                    //...and the workflow is at start (either just started or being routed there after escalation approval 
                    if (bizProcess.getStartEvents()
                            .stream()
                            .anyMatch(e -> e.getId().equals(w.getWorklist())
                            )) {
//                        if (!worklistsWhereItemIsEnabled.contains("ANY_WORKLIST")) {
                            //... and the item is allowed to be enabled
//                            if (worklistsWhereItemIsEnabled.contains(w.getWorklist())) {
                                return true;
//                            } else {
//                                return false;
//                            }
//                        }else{
//                            //w is at the beginning and the user is both woner and creator
//                            return true;
////                            if (w.getStatus().equals(Status.NEWLY_CREATED)||w.getStatus().equals(Status.DRAFT)){
////                                return true;
////                            }else{
////                                return false;
////                            }
//                        }
                    } else {
                         if (!worklistsWhereItemIsEnabled.contains("ANY_WORKLIST")) {
                            if (worklistsWhereItemIsEnabled.contains(w.getWorklist())) {
                                return true;
                            } else {
                                return false;
                            }
                        }else{
                            return true;
                        }
                    }
                } else {
                    //... but not owner
                    return false;
                }
                //if the user is the owner
            } else if (w.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                //...if we are filtering
                if (!worklistsWhereItemIsEnabled.contains("ANY_WORKLIST")) {
                    //... and the item is allowed to be visible
                    if (worklistsWhereItemIsEnabled.contains(w.getWorklist())) {
                        if (w.getApprovals()==null){
                            return false;
                        }
                        if (w.getApprovals().isEmpty()){
                            return false;
                        }
                        //...if you need to approve something
                        if (w.getApprovals().stream()
                                .map(a -> a.getUsername())
                                .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))){
                            return true;
                        }else{
                            return false;
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
 */