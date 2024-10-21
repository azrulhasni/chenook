/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.Status;
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
    
    public static <T extends WorkItem> WorkflowAwareGroup createForApprovalPanel(
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
    
     public static <T extends WorkItem> WorkflowAwareGroup createApproverForForm(
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
           return false;
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
                        //... and the item is allowed to be visible
                        if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    //... but not owner
                    if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                        return true;
                    } else {
                        return false;
                    }
                }
                //if the user is the owner
            } else if (item.getOwners()
                    .stream()
                    .map(bu -> bu.getUsername())
                    .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
                //... and the item is allowed to be visible
                if (worklistsWhereItemIsVisible.contains(item.getWorklist())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
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
                        //... and the item is allowed to be visible
                        if (worklistsWhereItemIsEnabled.contains(item.getWorklist())) {
                            return true;
                        } else {
                            return false;
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
                //... and the item is allowed to be visible
                if (worklistsWhereItemIsEnabled.contains(item.getWorklist())) {
                    return true;
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
             }else{
                 return false;
             }
        };

        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enabledPred, item);
        return group;
    }

//    private static <T extends WorkItem> Predicate<T> getDefaultVisible(OidcUser user) {
//        Predicate<T> visibleRule = (T item) -> {
//            if (StringUtils.equals(item.getCreator(), user.getPreferredUsername())) {
//                return true;
//            } else if (item.getOwners().stream().map(bu -> bu.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
//                return true;
//            } else if (item.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
//                return true;
//            } else {
//                return false;
//            }
//
//        };
//        return visibleRule;
//    }
//    
//    
//
//    private static <T extends WorkItem> Predicate<T> getDefaultVisibleByWorklist(
//            final OidcUser user,
//            final Set<String> worklistsWhereItemIsVisible) {
//        Predicate<T> enabledRule = (T item) -> {
//            //worklistsWhereItemIsVisible==null => visible anywhere
//            if (worklistsWhereItemIsVisible == null) {
//                return true;
//            }
//
//            //if we set worklistsWhereItemIsViisible and the item is in that worklist 
//            if (worklistsWhereItemIsVisible
//                    .contains(
//                            item.getWorklist()
//                    )
//                    && (item
//                            .getOwners()
//                            .stream()
//                            .map(bu -> bu.getUsername())
//                            .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername())))) {
//                return true;
//            } else {
//                return false;
//            }
//        };
//        return enabledRule;
//    }
//
//    private static <T extends WorkItem> Predicate<T> getDefaultEnabled(
//            final OidcUser user,
//            final BizProcess bizProcess,
//            final Set<String> worklistsWhereItemIsEnabled
//    ) {
//        Predicate<T> enabledRule = (T item) -> {
//
//            //if the workflow is at start (either just started or being routed there) and the user is the creator & owner, allow edition 
//            if (bizProcess.getStartEvents()
//                    .stream()
//                    .anyMatch(e -> e.getId().equals(item.getWorklist())
//                    )
//                    && StringUtils.equals(
//                            item.getCreator(),
//                            user.getPreferredUsername()
//                    )
//                    && item.getOwners()
//                            .stream()
//                            .map(bu -> bu.getUsername())
//                            .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
//                return true;
//            } else {
//                //worklistsWhereItemIsEnabled==null => enabled anywhere
//                if (worklistsWhereItemIsEnabled == null) {
//                    return true;
//                }
//                //worklistsWhereItemIsEnabled is empty => enabled anywhere
//                if (worklistsWhereItemIsEnabled.isEmpty()) {
//                    return true;
//                }
//                //if we set worklistsWhereItemIsEnabled and the item is in that worklist and the use is the owner, then enable
//                if (worklistsWhereItemIsEnabled
//                        .contains(
//                                item.getWorklist()
//                        )
//                        && (item
//                                .getOwners()
//                                .stream()
//                                .map(bu -> bu.getUsername())
//                                .anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername())))) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        };
//        return enabledRule;
//    }
//    
//   
//
//    public static <T extends WorkItem> WorkflowAwareGroup create(
//            final OidcUser user,
//            final T workItem,
//            final BizProcess bizProcess,
//            final Set<String> worklistsWhereItemIsVisible,
//            final Set<String> worklistsWhereItemIsEnabled
//    ) {
//        Predicate<T> visiblePred = getDefaultVisibleByWorklist(user, worklistsWhereItemIsVisible);
//        Predicate<T> enablePred = getDefaultEnabled(user, bizProcess, worklistsWhereItemIsEnabled);
//        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enablePred, workItem);
//        return group;
//    }
//
//    public static <T extends WorkItem> WorkflowAwareGroup createEnabledIfApprrovalNeeded(
//            final T workItem,
//            final OidcUser user
//    ) {
//
//        Predicate<T> commonPred = w -> {
//            if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
//                return true;
//            } else {
//                return false;
//            }
//        };
//        WorkflowAwareGroup group = new WorkflowAwareGroup(commonPred, commonPred, workItem);
//        return group;
//    }
//
//    public static <T extends WorkItem> WorkflowAwareGroup createEnabledIfNew(
//            final T workItem) {
//
//        Predicate<T> visiblePred = w -> {
//            return true;
//        };
//        Predicate<T> enablePred = w -> {
//            if (w.getStatus() == Status.NEWLY_CREATED || w.getStatus() == Status.DRAFT) {
//                return true;
//            }
//            return false;
//        };
//
//        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enablePred, workItem);
//        return group;
//
//    }
//
////    public static <T extends WorkItem> WorkflowAwareGroup createEnabledBeforeSubmission(
////            final T workItem,
////            final OidcUser user
////    ) {
////        Predicate<T> visiblePred = w -> {
////            return true;
////        };
////        Predicate<T> enablePred = w -> {
////            if (w.getStatus() == Status.NEWLY_CREATED || w.getStatus() == Status.DRAFT) {
////                return true;
////            } else if (w.getApprovals().stream().map(a -> a.getUsername()).anyMatch(u -> StringUtils.equals(u, user.getPreferredUsername()))) {
////                return true;
////            } else {
////                return false;
////            }
////        };
////        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enablePred, workItem);
////        return group;
////    }
//
//    public static <T extends WorkItem> WorkflowAwareGroup create(
//            final OidcUser user,
//            final T workItem,
//            final BizProcess bizProcess
//    ) {
//        Predicate<T> visiblePred = getDefaultVisible(user);
//        Predicate<T> enablePred = getDefaultEnabled(user, bizProcess, Set.of());
//        WorkflowAwareGroup group = new WorkflowAwareGroup(visiblePred, enablePred, workItem);
//        return group;
//    }
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
