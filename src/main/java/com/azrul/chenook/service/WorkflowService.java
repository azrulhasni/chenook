/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.chenook.service;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.Priority;
import com.azrul.chenook.domain.Status;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.WorkItemRepository;
import com.azrul.chenook.script.Expression;
import com.azrul.chenook.script.Scripting;
import com.azrul.chenook.views.common.PageNav;
import com.azrul.chenook.workflow.model.Activity;
import com.azrul.chenook.workflow.model.BaseActivity;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.ConditionalBranch;
import com.azrul.chenook.workflow.model.DefaultBranch;
import com.azrul.chenook.workflow.model.End;
import com.azrul.chenook.workflow.model.HumanActivity;
import com.azrul.chenook.workflow.model.ServiceActivity;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.chenook.workflow.model.XorActivity;
import com.azrul.chenook.workflow.model.XorAtleastOneApprovalActivity;
import com.azrul.chenook.workflow.model.XorMajorityApprovalActivity;
import com.azrul.chenook.workflow.model.XorUnanimousApprovalActivity;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.persistence.criteria.Join;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 * @param <T>
 */

public abstract class WorkflowService<T extends WorkItem> {


    private Scripting scripting;

    private List<ApproverLookup<T>> approverLookups;

    private BizUserService bizUserService;

    private Expression<Boolean, T> expr;
    

    private Map<String, List<HumanActivity>> getRoleActivityMap(BizProcess bizProcess) {
        return getActivities(bizProcess)
                .values()
                .stream()
                .filter(x -> HumanActivity.class.isAssignableFrom(x.getClass()))
                .map(x -> (HumanActivity) x)
                .filter(x -> x.getHandledBy() != null)
                .collect(Collectors.groupingBy(HumanActivity::getHandledBy));
    }

    private Boolean isApprovalActivity(BizProcess bizProcess, String activity) {
        Map<String, Activity> activities = getActivities(bizProcess);

        return isApprovalActivity(activities.get(activity));
    }

    private Boolean isApprovalActivity(Activity activity) {
        if (activity == null) {
            return Boolean.FALSE;
        }
        return (activity.getClass().equals(XorAtleastOneApprovalActivity.class)
                || activity.getClass().equals(XorUnanimousApprovalActivity.class)
                || activity.getClass().equals(XorMajorityApprovalActivity.class));
    }
    
    

     public T run(
            final T work,
            final String username,
            final BizProcess bizProcess,
            final boolean isError
    ) {
        BizUser bizUser = getBizUserService().getUser(username);
        return run(work, bizUser, bizProcess, isError);
    }

    private T run(
            final T work,
            final BizUser bizUser,
            final BizProcess bizProcess,
            final boolean isError
    ) {

        Map<String, Activity> activities = getActivities(bizProcess);
        //Pre-run script
        String worklist = work.getWorklist();
        Activity currentActivity = activities.get(worklist);
        work.setStatus(Status.IN_PROGRESS);
        if (!currentActivity.getClass().equals(End.class)) { //if not the end
            //transition to next steps
            List<Activity> nextSteps = runTransition(
                    work,
                    bizUser,
                    bizProcess
            );

            //process any straight through processing, might call run() again recursively
            straightThroughNextStepProcessing(
                    nextSteps,
                    work,
                    bizUser,
                    bizProcess,
                    isError
            );
        }

        return save(work);

    }

    private T straightThroughNextStepProcessing(
            final List<Activity> nextSteps,
            final T work,
            final BizUser bizUser,
            final BizProcess bizProcess,
            final boolean isError
    ) {
        for (Activity activity : nextSteps) {
            if (this.isStartEvent(activity, bizProcess)) {
                //do nothing as this is startEvent
                //do nothing as this is startEvent
            } else {
                work.setWorklist(activity.getId());
                work.setWorklistUpdateTime(LocalDateTime.now());
                if (activity.getClass().equals(End.class)) {
                    //we reach the end, conclude
                    work.setStatus(Status.DONE);
                    return run(work,bizUser, bizProcess, isError); //for post run script exec

                } else if (activity.getClass().equals(ServiceActivity.class)) {
                    String script = ((ServiceActivity) activity).getScript();
                    getScripting().runScript(work, bizUser, script,bizProcess);
                    return run(work, bizUser,bizProcess, isError);
                } else if (activity.getClass().equals(XorActivity.class)) {
                    return run(work, bizUser, bizProcess,isError);
                } else {
                    return work; //<-- this is ok since nextStep will not contain more than 1 activity at one time
                }
            }
        }
        return work;
    }

    private List<Activity> runTransition(
            final T work,
            final BizUser user,
            final BizProcess bizProcess
    ) {
        final String tenant = "";//(String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        //final String userIdentifier = user.getUsername(); //(String) VaadinSession.getCurrent().getSession().getAttribute("USER_IDENTIFIER");
        List<Activity> nextSteps = new ArrayList<>();

        String worklist = work.getWorklist();

        if (isStartEvent(worklist, bizProcess)) {//just being created
            StartEvent start = (StartEvent) getActivities(bizProcess).get(work.getStartEventId());

            if (start.getSupervisoryApprovalHierarchy().size() != 0) {//if need supervisor, stay in the same activity first
                handleSupervisorApproval(work,
                        tenant,
                        (Activity) ((BaseActivity) start).getNext(),
                        start,
                        nextSteps,
                        user,
                        start.getSupervisoryApprovalHierarchy());

            } else {
                dealWithNextStep(work,
                        tenant,
                        (Activity) start.getNext(),
                        nextSteps);
            }
        } else {
            for (Map.Entry<String, Activity> e : getActivities(bizProcess).entrySet()) {
                Activity activity = e.getValue();
                if (worklist.equals(activity.getId())) {
                    runSingleTransition(
                            activity,
                            work,
                            user,
                            tenant,
                            nextSteps,
                            bizProcess);
                }
            }
        }

        return nextSteps;
    }

    private void runSingleTransition(
            final Activity activity,
            final T work,
            final BizUser user,
            final String tenant,
            final List<Activity> nextSteps,
            final BizProcess bizProcess) {
        String userIdentifier = user.getUsername();
        if (activity.getClass().equals(HumanActivity.class)) {
            if (((HumanActivity) activity).getSupervisoryApprovalHierarchy().size() != 0) {
                handleSupervisorApproval(work,
                        tenant,
                        (Activity) ((BaseActivity) activity).getNext(),
                        activity,
                        nextSteps,
                        user,
                        ((HumanActivity) activity).getSupervisoryApprovalHierarchy());
            } else {

                dealWithNextStep(work,
                        tenant,
                        (Activity) ((BaseActivity) activity).getNext(),
                        nextSteps);
            }
        } else if (activity.getClass().equals(ServiceActivity.class)) {
            dealWithNextStep(work,
                    tenant,
                    (Activity) ((ServiceActivity) activity).getNext(),
                    nextSteps);
        } else if (activity.getClass().equals(XorActivity.class)) {
            //see which condition triggers and follow that branch
            boolean conditionTriggered = false;
            for (var branch : ((XorActivity) activity).getBranch()) {
                Boolean result = getExpr().evaluate(branch.getCondition(), work, user, bizProcess);
                if (result == true) {
                    conditionTriggered = true;
                    nextSteps.add((Activity) branch.getNext());
                    break;
                }
            }
            if (conditionTriggered == false) {//if no condition triggered, the  branch is executed
                nextSteps.add((Activity) ((XorActivity) activity).getByDefault().getNext());
            }
        } else if (activity.getClass().equals(XorUnanimousApprovalActivity.class)) {

            XorUnanimousApprovalActivity approvalActivity = (XorUnanimousApprovalActivity) activity;
            //determine if there is enough approval
            Map<Boolean, List<Approval>> approvedWork = work
                    .getApprovals()
                    .stream()
                    .filter(x -> x.getApproved() != null)
                    .collect(Collectors.groupingBy(Approval::getApproved));

            int state = 0;
            //state 0 : unanimous approval
            //state 1 : not enough vote
            //state 2 : at least 1 disapproval
            if (approvedWork.containsKey(Boolean.FALSE)) { //if one person voted to disapproved, then the whole thing is disapproved
                state = 2;
            } else {
                if (approvedWork.get(Boolean.TRUE).size() >= work.getApprovals().size()) { //unanimous approval
                    state = 0;
                } else {//not everyone voted yet
                    state = 1;
                }
            }

            if (state == 0) { //we have a unanimous approval
                archiveApprovals(work);
                //go to approved branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorUnanimousApprovalActivity) activity).getApprovedBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnApproved,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (state == 2) {//at least one disapproval
                archiveApprovals(work);
                //go to  branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorUnanimousApprovalActivity) activity).getDefaultBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnRejected,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);

            } else {
                //don't move to next step but takeout the current owners (i.e. the approvers) so that he doesn't see it in his ownership any more
                work.getOwners().remove(userIdentifier);
            }
            //dealWithNextStep(root, tenant, getActivities().get(activity.getNext()), nextSteps, container);
        } else if (activity.getClass().equals(XorAtleastOneApprovalActivity.class)) {
            XorAtleastOneApprovalActivity approvalActivity = (XorAtleastOneApprovalActivity) activity;

            //determine if there is enough approval
            Map<Boolean, List<Approval>> approvedWork = work
                    .getApprovals()
                    .stream().filter(x -> x.getApproved() != null)
                    .collect(Collectors.groupingBy(Approval::getApproved));
            int state = 0;
            //state 0 : unanimous disapproval
            //state 1 : not enough vote
            //state 2 : at least 1 approval

            if (!approvedWork.containsKey(Boolean.FALSE)) { //we don't even have a disapproval
                if (approvedWork.containsKey(Boolean.TRUE)) { //if we have at least 1 approval
                    state = 2;
                } else { //no approval and no disapproval=> no one voted yet
                    state = 1;
                }
            } else {
                if (approvedWork.get(Boolean.FALSE).size() >= work.getApprovals().size()) { //unanimous disapproval
                    state = 0;
                } else {//there are some voted to approved
                    state = 1;
                }

            }

            if (state == 0) { //we have a unanimous dis-approval
                archiveApprovals(work);
                //go to  branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorAtleastOneApprovalActivity) activity).getDefaultBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnRejected,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (state == 2) {//at least one approval
                archiveApprovals(work);
                //go to approved branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorAtleastOneApprovalActivity) activity).getApprovedBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnApproved,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else {
                //don't move to next step but takeout the current owner so that he doesn't see it in his ownership any more
                work.getOwners().remove(userIdentifier);
            }
            //dealWithNextStep(root, tenant, getActivities().get(activity.getNext()), nextSteps, container);
        } else if (activity.getClass().equals(XorMajorityApprovalActivity.class)) {
            int countApprove = 0;
            int countDisapprove = 0;
            XorMajorityApprovalActivity approvalActivity = (XorMajorityApprovalActivity) activity;

            for (Approval approval : work.getApprovals()) {
                if (approval.getApproved() != null) {
                    if (approval.getApproved().equals(Boolean.TRUE)) {
                        countApprove++;
                    } else {
                        countDisapprove++;
                    }
                }
            }

            double majorityVote = work.getApprovals().size() / 2;

            if (countApprove > majorityVote) { //must be strictly bigger than since >= could mean a tie
                archiveApprovals(work);
                //go to approved branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorMajorityApprovalActivity) activity).getApprovedBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnApproved,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (countDisapprove > majorityVote) {
                archiveApprovals(work);
                //go to  branch
                dealWithNextStep(work,
                        tenant,
                        //(Activity) ((XorMajorityApprovalActivity) activity).getDefaultBranch(),
                        this.evaluateBranchesForNextActivty(
                                approvalActivity::getOnRejected,
                                approvalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (countDisapprove == countApprove
                    && countDisapprove + countApprove == work.getApprovals().size()) { //everyone voted and its a tie
                archiveApprovals(work);
                //go to  branch
                dealWithNextStep(work,
                        tenant,
                        (Activity) ((XorMajorityApprovalActivity) activity).getOnTieBreaker().getNext(),
                        nextSteps);
            } else {
                //don't move to next step but takeout the current owner so that he doesn't see it in his ownership any more
                work.getOwners().remove(userIdentifier);
            }

        }
    }

    private void handleSupervisorApproval(final T root,
            final String tenant,
            final Activity next,
            final Activity activity,
            final List<Activity> nextSteps,
            final BizUser user,
            final List<String> supervisorHierarchy) {
        if (isSupervisorNeeded(root)) { //this was sent for approval before
            if (root.getApprovals().isEmpty() == Boolean.FALSE
                    && Boolean.TRUE.equals(root.getApprovals().iterator().next().getApproved())) { //approved
                //see which level is the approval on
                String currentApprLevel = root.getSupervisorApprovalLevel();
                int indexOfNextApprLevel = getArrayIndexOfValue(supervisorHierarchy, currentApprLevel) + 1;

                //if we are at the end of the hierarchy
                if (indexOfNextApprLevel == supervisorHierarchy.size()) {
                    root.setSupervisorApprovalSeeker(null);
                    root.setSupervisorApprovalLevel(null);
                    archiveApprovals(root);
                    dealWithNextStep(root, tenant, next, nextSteps);
                } else {//if we are still not at the end
                    //find next role
                    String nextRole = supervisorHierarchy.get(indexOfNextApprLevel);
                    if (getApproverLookups() != null) {
                        getApproverLookups().stream().filter(a -> {
                            Qualifier qualifier = (Qualifier) a.getClass().getAnnotation(Qualifier.class);
                            if (qualifier == null) {
                                return Boolean.FALSE;
                            }
                            return nextRole.equals(qualifier.value());
                        }).findAny().ifPresent(approverLookup -> {
                            approverLookup.lookupApprover((T) root, root.getSupervisorApprovalSeeker())
                                    .ifPresent(approver -> {
                                        root.getOwners().clear();
                                        root.getOwners().add(approver.getUsername());
//                                        if (approver.getUsername() != null) {
//                                            root.getOwners().add(approver.getUsername());
//                                        } else {
//                                            root.getOwners().add(approver.getLoginName());
//                                        }
                                        root.setSupervisorApprovalLevel(nextRole);

                                        //archive first
                                        archiveApprovals(root);
                                        //then add the new approver
                                        //loadUserIntoApprovalList(approver.getLoginName(), activity, tenant, root);//for apperovals, we log the current activity for supervisor approval
                                        loadUserIntoApprovalList(approver.getUsername(), root);
                                        nextSteps.add(activity); //if need supervisor, stay in the same activity first

                                    });

                        });
                    }

                }
            } else {//not approved. Reassign back to original user
                root.getOwners().clear();
                root.getOwners().add(root.getSupervisorApprovalSeeker());

                archiveApprovals(root);
                root.setSupervisorApprovalLevel(null);
                root.setSupervisorApprovalSeeker(null);
                nextSteps.add(activity);
            }
        } else { //seeking new approval

            String nextRole = supervisorHierarchy.get(0);
            if (getApproverLookups() != null) {
                getApproverLookups().stream().filter(a -> {
                    Qualifier qualifier = (Qualifier) a.getClass().getAnnotation(Qualifier.class);
                    if (qualifier == null) {
                        return Boolean.FALSE;
                    }
                    return nextRole.equals(qualifier.value());
                }).findAny().ifPresent(approverLookup -> {
                    approverLookup.lookupApprover((T) root, user.getUsername()).ifPresent(approver -> {
                        root.getOwners().clear();
                        root.getOwners().add(approver.getUsername());
//                                        if (approver.getUsername() != null) {
//                                            root.getOwners().add(approver.getUsername());
//                                        } else {
//                                            root.getOwners().add(approver.getLoginName());
//                                        }
                        root.setSupervisorApprovalLevel(nextRole);

                        //archive first
                        archiveApprovals(root);
                        //then add the new approver
                        //loadUserIntoApprovalList(approver.getLoginName(), activity, tenant, root);//for apperovals, we log the current activity for supervisor approval
                        loadUserIntoApprovalList(approver.getUsername(), root);

                        nextSteps.add(activity); //if need supervisor, stay in the same activity first
                    });
                });
            }
            root.setSupervisorApprovalSeeker(user.getUsername());
        }
    }

    private void dealWithNextStep(T work, String tenant, Activity nextActivity, List<Activity> nextSteps) {

        work.getOwners().clear(); //so that the next folks can pick it up

        work.setSupervisorApprovalSeeker(null);//nullify the approval seeker too
        getWorkItemRepo().save(work);
        //only current activity ids in wait states
        if (nextActivity == null) { //nextActivity==END
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorUnanimousApprovalActivity.class)) {
            loadUsersIntoApprovalList(((XorUnanimousApprovalActivity) nextActivity).getHandledBy(), nextActivity, tenant, work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorAtleastOneApprovalActivity.class)) {
            loadUsersIntoApprovalList(((XorAtleastOneApprovalActivity) nextActivity).getHandledBy(), nextActivity, tenant, work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorMajorityApprovalActivity.class)) {
            loadUsersIntoApprovalList(((XorMajorityApprovalActivity) nextActivity).getHandledBy(), nextActivity, tenant, work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(HumanActivity.class)) { //nextActivity.getType=="human" OR nextActivity.getType=="service"
            nextSteps.add(nextActivity); //if the next step is just another wait state, make it active
        } else { //service
            nextSteps.add(nextActivity);
        }
    }

    private void loadUserIntoApprovalList(String loginName, T work) {
        Approval approval = new Approval();
        approval.setUsername(loginName);
        work.getApprovals().add(approval);
    }

    private Boolean isSupervisorNeeded(T work) {
        return work.getSupervisorApprovalSeeker() != null; //there is a need to get supervisor's approval
    }

    private void loadUsersIntoApprovalList(String role,
            Activity nextActivity,
            final String tenant,
            T work) {
        List<BizUser> users = getBizUserService().getUsersByRole(role);

        //work.archiveApproval();
        //dao.save(work);
//        System.out.println("=======Element id:"+work.getId()+"==============");
//        System.out.println("   Approvals:");
//        for (Approval a:work.getApprovals()){
//             System.out.println("   approval id:"+a.toString());
//        }
//        archiveApprovals(work);
//        dao.saveAndAssociate(work.getApprovals(),
//                work,
//                "historicalApprovals",
//                w -> ((WorkElement) w).getApprovals().clear());
        archiveApprovalsAndSave(work);
        for (BizUser user : users) {
            loadUserIntoApprovalList(user.getUsername(), work);
        }

    }

    private void archiveApprovals(T work) {
        archiveApprovalsAndSave(work);
//        System.out.println("=======Element id:"+work.getId()+"==============");
//        System.out.println("   Approvals:");
//        for (Approval a:work.getApprovals()){
//             System.out.println("   approval id:"+a.toString());
//        }
//        Set hset = dao.saveAndAssociate(work.getApprovals(),
//                work, "historicalApprovals",
//                w -> ((WorkElement) w).getApprovals().clear());
//        for (var h:hset){
//            System.out.println("    hist approval id:"+((Dual)h).getSecond().toString());
//        }
    }

    private void createApprovalAndAssociateToWork(
            T work,
            BizUser user,
            Set<Approval> approvals
    ) {

//        Optional<String> parentEnumPath = Castor.<T, Element>given(work)
//                .castItTo(Element.class)
//                .thenDo(w -> {
//                    return w.getEnumPath();
//                }).go();
//        Optional<Approval> oapproval = dao.createAndSave(Approval.class,
//                Optional.of(tenant),
//                parentEnumPath,
//                Optional.empty(),
//                Optional.empty(),
//                Status.IN_PROGRESS,
//                work.getCreator());
        Approval approval = new Approval();
        if (user.isEnabled()) {
            approval.setUsername(user.getUsername());

//                approval.setWorklist(((BaseActivity) nextActivity).getId());
//                if (work.getStartEventDescription() != null) {
//                    approval.setStartEventDescription(work.getStartEventDescription());
//                }
//                if (work.getStartEventId() != null) {
//                    approval.setStartEventId(work.getStartEventId());
//                }
//            oapproval.ifPresent(approval -> {
//                mapUserToApproval(approval, user, tenant);
//                approval.setWorklist(((BaseActivity) nextActivity).getId());
//                if (work.getStartEventDescription() != null) {
//                    approval.setStartEventDescription(work.getStartEventDescription());
//                }
//                if (work.getStartEventId() != null) {
//                    approval.setStartEventId(work.getStartEventId());
//                }
//                dao.saveAndAssociate(approval, work, "approvals").ifPresent(d -> {
//                    Approval savedApproval = (Approval) ((Dual) d).getSecond();
//                    approvals.add(savedApproval);
//
//                    //dao.saveAndAssociate(savedApproval, work, "historicalApprovals"); <--move this to ApprovalRenderer beforeSaveCallback/beforeSubmitCallback
//                });
//                userIdentifierLookup.lookup(user).ifPresent(userId -> {
//                    work.getOwners().add(userId);
//                });
//
//            });
        }
    }

    private Boolean isStartEvent(Activity activity, BizProcess bizProcess) {
        return bizProcess.getStartEvents().stream().anyMatch(se -> se.getId().equals(activity.getId()));
    }

    private Boolean isStartEvent(String activityId, BizProcess bizProcess) {
        return bizProcess.getStartEvents().stream().anyMatch(se -> se.getId().equals(activityId));
    }

    private Boolean isActivityAccessibleByRoles(String activityid, Set<String> inroles, BizProcess bizProcess) {
        Map<String, Activity> activities = getActivities(bizProcess);
        return isActivityAccessibleByRoles(activities.get(activityid), inroles, bizProcess);
    }

    private Boolean isActivityAccessibleByRoles(Activity activity, Set<String> inroles, BizProcess bizProcess) {

        if (activity == null || activity.getClass().equals(StartEvent.class)) {
            return whoCanStart(bizProcess).stream().anyMatch(inroles::contains);
        } else if (HumanActivity.class.isAssignableFrom(activity.getClass())) {
            return inroles.contains(((HumanActivity) activity).getHandledBy());
        } else {
            return Boolean.FALSE;
        }

    }
    
     public List<StartEvent> whatUserCanStart(OidcUser oidcUser, BizProcess bizProcess){
        Set<String> roles =  oidcUser
                .getAuthorities()
                .stream()
                .map(a->a.getAuthority())
                .map(String::toLowerCase)
                .map(a->a.replace("role_", ""))
                .collect(Collectors.toSet());
        
        List<StartEvent> startEvents =  bizProcess
                .getStartEvents()
                .stream()
                .filter(e->{
                    Set<String> intersect = e.getCanBeStartedBy().stream().map(String::toLowerCase).collect(Collectors.toSet());
                    intersect.retainAll(roles);
                    return !intersect.isEmpty();
                 })
                .sorted()
                .collect(Collectors.toList());
        return startEvents;
    }
    

    private Set<String> whoCanStart(BizProcess bizProcess) {
        return new HashSet<String>(bizProcess
                .getStartEvents()
                .stream()
                .map(StartEvent::getCanBeStartedBy)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    private Set<String> whoCanStart(T work, BizProcess bizProcess) {
        Map<String, Activity> activities = getActivities(bizProcess);
        String startEventId = work.getStartEventId();
        StartEvent startEvent = (StartEvent) activities.get(startEventId);
        return new HashSet<String>(startEvent.getCanBeStartedBy());
    }

    private Set<StartEvent> getStartEvents(BizProcess bizProcess) {
        return new HashSet<StartEvent>(bizProcess.getStartEvents());
    }

    private int getArrayIndexOfValue(List<String> array, String value) {
        int f = 0;
        for (String v : array) {
            if (v.equals(value)) {
                return f;
            }
            f++;
        }
        return f;
    }

    private Boolean isActivitySLAExpired(String activity, LocalDateTime workSLAUpdateTime, BizProcess bizProcess) {
        if (activity == null) { //start event
            return Boolean.FALSE;
        }
        Map<String, Activity> activities = getActivities(bizProcess);
        if (activities.get(activity) == null) { //cater for updating workflow id in workflow.xml but data is old
            return Boolean.FALSE;
        }
        if (HumanActivity.class.isAssignableFrom(activities.get(activity).getClass())) {
            return isActivitySLAExpired((HumanActivity) activities.get(activity), workSLAUpdateTime);
        } else {
            return Boolean.FALSE;
        }
    }

    private Boolean isActivitySLAExpired(HumanActivity activity, LocalDateTime workSLAUpdateTime) {
        if (activity.getSlaInHours() != null && workSLAUpdateTime != null) {
            Duration diff = Duration.between(workSLAUpdateTime, LocalDateTime.now());
            return (diff.getSeconds() / 3600) > activity.getSlaInHours();
        } else {
            return Boolean.FALSE;
        }
    }

    private Activity evaluateBranchesForNextActivty(
            final Supplier<List<? extends ConditionalBranch>> getBranches,
            final Supplier<DefaultBranch> getDefaultBranch,
            final T work,
            final BizUser user,
            final BizProcess bizProces) {

        //state=0: no activity has condition == true
        //state=1: 1 activity has condition == true
        //state=2: multiple activities has condition == true (in this case, we have no guarantee which branch is executed)
        Activity nextStep = null;
        for (var branch : getBranches.get()) {
            Boolean result = getExpr().evaluate(branch.getCondition(),  work, user, bizProces);
            if (result == true) {
                nextStep = (Activity) branch.getNext(); //deal with state=1
                break; //deal with state=2
            }

        }
        //deal with state=0
        if (nextStep == null) {//if no condition triggered, the  branch is executed
            nextStep = (Activity) (getDefaultBranch.get()).getNext();
        }
        return nextStep;
    }

    private Map<String, Activity> getActivities(BizProcess bizProcess) {
        Map<String, Activity> activities = new HashMap<>();
        BizProcess.Workflow workflow = bizProcess.getWorkflow();
        for (Activity currentActivity : workflow.getStartEventOrServiceOrHuman()) {
            activities.put(currentActivity.getId(), currentActivity);
        }
        return activities;
    }

    @Transactional
    public abstract T save(T work);

    @Transactional
    public T initAndSave(
             final T newwork,
             final OidcUser oidcUser,
             final String context,
             final StartEvent startEvent, 
             final BizProcess bizProcess) {
        
       
        //T newwork = ;
        newwork.setContext(context);
        newwork.setCreator(oidcUser.getPreferredUsername());
        newwork.setPriority(Priority.NONE);
        newwork.setStatus(Status.NEWLY_CREATED);
        
        //newwork.setFields(fields);
        Set<String> owners = new HashSet<>();
        owners.add(oidcUser.getPreferredUsername());
        newwork.setOwners(owners);
        newwork.setStartEventId(startEvent.getId());
        newwork.setStartEventDescription(bizProcess.getStartEvents().iterator().next().getDescription());
        newwork.setWorklist(bizProcess.getStartEvents().iterator().next().getId());
        newwork.setWorklistUpdateTime(LocalDateTime.now());

        T w =  save(newwork);
        return w;
    }

    @Transactional
     public T findById(final Long id, final String context) {
        T work = findById(
                id,
                context);
        return work;
    }

    @Transactional
     public T createApprovalAndSave(T work) {

        work.getHistoricalApprovals().addAll(work.getApprovals());
        work.getApprovals().clear();
        T w = getWorkItemRepo().save(work);
        return w;
    }

    @Transactional
    private T archiveApprovalsAndSave(T work) {
        
        work.getHistoricalApprovals().addAll(work.getApprovals());
        work.getApprovals().clear();
        T w = getWorkItemRepo().save(work);
        return w;
    }

    @Transactional
    private T findById(Long id) {
        Optional<T> work = getWorkItemRepo().findById(id);
        return work.orElse(null);
    }
    
     public Integer countWorkByOwner(String username){
        Long count =  getWorkItemRepo().countByOwner(username);//count(whereOwnersContains(username));
        return count.intValue();
    }
    
     public DataProvider getWorkByOwner(String username, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                
                Sort.Direction sort =   so==null
                                        ?Sort.Direction.DESC
                                        :(
                                            SortDirection.ASCENDING.equals(so.getDirection())
                                            ?Sort.Direction.ASC
                                            :Sort.Direction.DESC
                                        );
                String sorted = so==null
                                ?"id"
                                :so.getSorted();
                query.getPage();
                Page<T> finapps = getWorkItemRepo().findByOwner(username,
                    PageRequest.of(
                            pageNav.getPage() - 1,
                            pageNav.getMaxCountPerPage(),
                            Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<T, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(T item) {
                return item.getId().toString();
            }

        };
        return dp;
    }
    
     public Integer countWorkByCreator(String username){
        Long count =  getWorkItemRepo().count(whereCreatorEquals(username));
        return count.intValue();
    }

     public DataProvider getWorkByCreator(String username, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                
                Sort.Direction sort =   so==null
                                        ?Sort.Direction.DESC
                                        :(
                                            SortDirection.ASCENDING.equals(so.getDirection())
                                            ?Sort.Direction.ASC
                                            :Sort.Direction.DESC
                                        );
                String sorted = so==null
                                ?"id"
                                :so.getSorted();
                query.getPage();
                Page<T> finapps = getWorkItemRepo().findAll(
                    whereCreatorEquals(username),
                    PageRequest.of(
                            pageNav.getPage() - 1,
                            pageNav.getMaxCountPerPage(),
                            Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<T, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(T item) {
                return item.getId().toString();
            }

        };
        return dp;
    }
    
     public Integer countWorkByWorklist(String worklist){
        Long count =  getWorkItemRepo().countByWorklistAndNoOwner(worklist);//count(whereWorklistEquals(worklist));
        return count.intValue();
    }
    
     public DataProvider getWorkByWorklist(String worklist, PageNav pageNav) {
        //build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                QuerySortOrder so = query.getSortOrders().isEmpty() ? null : query.getSortOrders().get(0);
                
                Sort.Direction sort =   so==null
                                        ?Sort.Direction.DESC
                                        :(
                                            SortDirection.ASCENDING.equals(so.getDirection())
                                            ?Sort.Direction.ASC
                                            :Sort.Direction.DESC
                                        );
                String sorted = so==null
                                ?"id"
                                :so.getSorted();
                query.getPage();
                Page<T> finapps = getWorkItemRepo().findByWorklistAndNoOwner(worklist,
                    PageRequest.of(
                            pageNav.getPage() - 1,
                            pageNav.getMaxCountPerPage(),
                            Sort.by(sort, sorted))
                );
                return finapps.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<T, Void> query) {
                return pageNav.getDataCountPerPage();
            }

            @Override
            public String getId(T item) {
                return item.getId().toString();
            }

        };
        return dp;
    }
 

//    private Specification<T> whereOwnersContains(String username) {
//        return (workItem, cq, cb) -> {
//            //workItem.fetch("owners");
//            //return cb.in(cb.workItem)isMember(username, workItem.get("owners"));
//            Set<String> users = new HashSet<>();
//            users.add(username);
//            return cb.literal(users).in(workItem.get("owners").as(Set.class));
//        };
//    }

    private Specification<T> whereCreatorEquals(String username) {
        return (workItem, cq, cb) -> {
            return cb.equal(workItem.get("creator"), username);
        };
    }

//    private Specification<T> whereWorklistEquals(String worklist) {
//        return (workItem, cq, cb) -> {
//            return cb.and(
//                    cb.equal(workItem.get("worklist"), worklist),
//                    cb.isEmpty(workItem.get("owners"))
//            );
//        };
//    }

    /**
     * @return the workItemRepo
     */
    public abstract WorkItemRepository<T> getWorkItemRepo();
    
    /**
     * @return the scripting
     */
    public final Scripting getScripting() {
        return scripting;
    }

    /**
     * @param scripting the scripting to set
     */
    @Autowired
    public final void setScripting(Scripting scripting) {
        this.scripting = scripting;
    }

    /**
     * @return the approverLookups
     */
    public final List<ApproverLookup<T>> getApproverLookups() {
        return approverLookups;
    }

    /**
     * @param approverLookups the approverLookups to set
     */
    @Autowired(required = false)
    public final void setApproverLookups(List<ApproverLookup<T>> approverLookups) {
        this.approverLookups = approverLookups;
    }

    /**
     * @return the bizUserService
     */
    public final BizUserService getBizUserService() {
        return bizUserService;
    }

    /**
     * @param bizUserService the bizUserService to set
     */
    @Autowired
    public final void setBizUserService(BizUserService bizUserService) {
        this.bizUserService = bizUserService;
    }

   

    /**
     * @return the expr
     */
    public final Expression<Boolean, T> getExpr() {
        return expr;
    }

    /**
     * @param expr the expr to set
     */
    @Autowired
    public final void setExpr(Expression<Boolean, T> expr) {
        this.expr = expr;
    }

}
