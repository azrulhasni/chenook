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
import com.azrul.chenook.search.repository.WorkItemSearchRepository;
import com.azrul.chenook.script.Expression;
import com.azrul.chenook.script.FunctionExpression;
import com.azrul.chenook.script.PredicateExpression;
import com.azrul.chenook.script.Scripting;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.workflow.SearchTermProvider;
import com.azrul.chenook.workflow.model.Activity;
import com.azrul.chenook.workflow.model.BaseActivity;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.ConditionalBranch;
import com.azrul.chenook.workflow.model.DefaultBranch;
import com.azrul.chenook.workflow.model.DirectHumanActivity;
import com.azrul.chenook.workflow.model.End;
import com.azrul.chenook.workflow.model.HumanActivity;
import com.azrul.chenook.workflow.model.ServiceActivity;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.chenook.workflow.model.XorActivity;
import com.azrul.chenook.workflow.model.XorAtleastOneApprovalActivity;
import com.azrul.chenook.workflow.model.XorMajorityApprovalActivity;
import com.azrul.chenook.workflow.model.XorUnanimousApprovalActivity;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.SetJoin;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 * @param <T>
 */
public abstract class WorkflowService<T extends WorkItem> {

    // setter injection
    private EntityManagerFactory emFactory;

    private Scripting scripting;

    private List<ApproverLookup<T>> approverLookups;

    // Setter injection
    private BizUserService bizUserService;

    // Setter injection
    private ApprovalService approvalService;

    // Setter injection
    private PredicateExpression<T> predicateExpression;

    // Setter injection
    private FunctionExpression<T> functionExpression;
    
    private static final String EXPR_OPEN = "#{";
    
    private static final String EXPR_CLOSE = "}";

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

    public Boolean isWaitingApproval(WorkItem work) {
        return (work.getSupervisorApprovalSeeker() != null);
    }

    @Transactional
    public T run(
            final T work,
            final String username,
            final BizProcess bizProcess,
            final boolean isError) {
        System.out.println("ID:" + work.getId());
        System.out.println("     User name:" + username);
        System.out.println("     Incoming worklist:" + work.getWorklist());
        BizUser bizUser = getBizUserService().getUser(username);
        T w = runRecursive(work, bizUser, bizProcess, isError);
        System.out.println("     Outging worklist:" + work.getWorklist());
        return save(w);

    }

    private T runRecursive(
            final T work,
            final BizUser bizUser,
            final BizProcess bizProcess,
            final boolean isError) {

        Map<String, Activity> activities = getActivities(bizProcess);
        // Pre-run script

        String worklist = work.getWorklist();
        Activity currentActivity = activities.get(worklist);

        if (!currentActivity.getClass().equals(End.class)) { // if not the end
            // transition to next steps
            List<Activity> nextSteps = runTransition(
                    work,
                    bizUser,
                    bizProcess);

            //if transition does happen, then call the pre run script of the new actiivities and the post run script of the current activity
            //this is to simulate 'front montant' / 'front decendant'
            if (currentActivity.getClass().equals(BaseActivity.class)) { //run post current activity script
                BaseActivity baseActivity = (BaseActivity) currentActivity;
                String script = baseActivity.getPreRunScript();
                getScripting().runScript(work, bizUser, script, bizProcess);
            }
            for (Activity nextStep : nextSteps) { //run pre next activities scripts

                if (nextStep.getClass().equals(BaseActivity.class)) {
                    BaseActivity baseActivity = (BaseActivity) currentActivity;
                    String script = baseActivity.getPreRunScript();
                    getScripting().runScript(work, bizUser, script, bizProcess);
                }
            }

            // process any straight through processing, might call runRecursive() again recursively
            straightThroughNextStepProcessing(
                    nextSteps,
                    work,
                    bizUser,
                    bizProcess,
                    isError);
        } else {
            End endActivity = (End) currentActivity;//run pre & post current activity script
            String preRunScript = endActivity.getPreRunScript();
            getScripting().runScript(work, bizUser, preRunScript, bizProcess);
            String postRunScript = endActivity.getPostRunScript();
            getScripting().runScript(work, bizUser, postRunScript, bizProcess);

        }

        return work;

    }

    private T straightThroughNextStepProcessing(
            final List<Activity> nextSteps,
            final T work,
            final BizUser bizUser,
            final BizProcess bizProcess,
            final boolean isError) {
        for (Activity activity : nextSteps) {
            work.setWorklist(activity.getId());
            work.setWorklistUpdateTime(LocalDateTime.now());
            if (activity.getClass().equals(End.class)) {
                // we reach the end, conclude
                T endWork = runRecursive(work, bizUser, bizProcess, isError); // for post run script exec
                endWork.setEndDate(ZonedDateTime.now()); //record completed time
                return endWork;
            } else if (activity.getClass().equals(ServiceActivity.class)) {
                String script = ((ServiceActivity) activity).getScript();
                getScripting().runScript(work, bizUser, script, bizProcess);
                return runRecursive(work, bizUser, bizProcess, isError);
            } else if (activity.getClass().equals(XorActivity.class)) {
                return runRecursive(work, bizUser, bizProcess, isError);
            } else {
                return work; // <-- this is ok since nextStep will not contain more than 1 activity at one
                // time
            }

        }
        return work;
    }

    private List<Activity> runTransition(
            final T work,
            final BizUser user,
            final BizProcess bizProcess) {
        final String tenant = "";// (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        // final String userIdentifier = user.getUsername(); //(String)
        // VaadinSession.getCurrent().getSession().getAttribute("USER_IDENTIFIER");
        List<Activity> nextSteps = new ArrayList<>();

        String worklist = work.getWorklist();

        if (isStartEvent(worklist, bizProcess)) {// just being created
            StartEvent start = (StartEvent) getActivities(bizProcess).get(work.getStartEventId());
            work.setStartDate(ZonedDateTime.now()); //record start time
            if (start.getSupervisoryApprovalHierarchy().size() != 0) {// if need supervisor, stay in the same activity
                // first
                handleSupervisorApproval(work,
                        tenant,
                        (Activity) ((BaseActivity) start).getNext(),
                        start,
                        nextSteps,
                        user,
                        bizProcess,
                        start.getSupervisoryApprovalHierarchy());

            } else {
                dealWithNextStep(work,
                        tenant,
                        user,
                        bizProcess,
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
            final BizProcess bizProcess
    ) {
        // String userIdentifier = user.getUsername();
        if (activity.getClass().equals(HumanActivity.class)) {
            HumanActivity humanActivity = (HumanActivity) activity;
            if (!humanActivity.getSupervisoryApprovalHierarchy().isEmpty()) {
                handleSupervisorApproval(work,
                        tenant,
                        (Activity) humanActivity.getNext(),
                        activity,
                        nextSteps,
                        user,
                        bizProcess,
                        humanActivity.getSupervisoryApprovalHierarchy());
            } else {
                dealWithNextStep(work,
                        tenant,
                        user,
                        bizProcess,
                        (Activity) ((BaseActivity) activity).getNext(),
                        nextSteps);
            }
        } else if (activity.getClass().equals(DirectHumanActivity.class)) {
            DirectHumanActivity directHumanActivity = (DirectHumanActivity) activity;
            dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        (Activity) directHumanActivity.getNext(),
                        nextSteps);
            
        } else if (activity.getClass().equals(ServiceActivity.class)) {
            ServiceActivity serviceActivity = (ServiceActivity) activity;
            dealWithNextStep(work,
                    tenant,
                    user,
                    bizProcess,
                    (Activity) serviceActivity.getNext(),
                    nextSteps);
        } else if (activity.getClass().equals(XorActivity.class)) {
            XorActivity xorActivity = (XorActivity) activity;
            // see which condition triggers and follow that branch
            boolean conditionTriggered = false;
            for (var branch : xorActivity.getBranch()) {
                Boolean result = getPredicateExpression().evaluate(getCondition(branch), work, user, bizProcess);
                if (result) {
                    conditionTriggered = true;
                    nextSteps.add((Activity) branch.getNext());
                    break;
                }
            }
            if (!conditionTriggered) { // if no condition triggered, the branch is executed
                nextSteps.add((Activity) xorActivity.getByDefault().getNext());
            }
        } else if (activity.getClass().equals(XorUnanimousApprovalActivity.class)) {
            XorUnanimousApprovalActivity xorUnanimousApprovalActivity = (XorUnanimousApprovalActivity) activity;
            // determine if there is enough approval
            Map<Boolean, List<Approval>> approvedWork = work
                    .getApprovals()
                    .stream()
                    .filter(x -> x.getApproved() != null)
                    .collect(Collectors.groupingBy(Approval::getApproved));

            int state = 0;
            // state 0 : unanimous approval
            // state 1 : not enough vote
            // state 2 : at least 1 disapproval
            if (approvedWork.containsKey(Boolean.FALSE)) { // if one person voted to disapproved, then the whole thing
                // is disapproved
                state = 2;
            } else {
                if (approvedWork.get(Boolean.TRUE).size() >= work.getApprovals().size()) { // unanimous approval
                    state = 0;
                } else {// not everyone voted yet
                    state = 1;
                }
            }

            if (state == 0) { // we have a unanimous approval
                archiveApprovals(work);
                // go to approved branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorUnanimousApprovalActivity::getOnApproved,
                                xorUnanimousApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (state == 2) {// at least one disapproval
                archiveApprovals(work);
                // go to branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorUnanimousApprovalActivity::getOnRejected,
                                xorUnanimousApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);

            } else {
                // don't move to next step but takeout the current owners (i.e. the approvers)
                // so that he doesn't see it in his ownership any more
                work.removeOwner(user);
            }
        } else if (activity.getClass().equals(XorAtleastOneApprovalActivity.class)) {
            XorAtleastOneApprovalActivity xorAtleastOneApprovalActivity = (XorAtleastOneApprovalActivity) activity;
            // determine if there is enough approval
            Map<Boolean, List<Approval>> approvedWork = work
                    .getApprovals()
                    .stream().filter(x -> x.getApproved() != null)
                    .collect(Collectors.groupingBy(Approval::getApproved));
            int state = 0;
            // state 0 : unanimous disapproval
            // state 1 : not enough vote
            // state 2 : at least 1 approval

            if (!approvedWork.containsKey(Boolean.FALSE)) { // we don't even have a disapproval
                if (approvedWork.containsKey(Boolean.TRUE)) { // if we have at least 1 approval
                    state = 2;
                } else { // no approval and no disapproval=> no one voted yet
                    state = 1;
                }
            } else {
                if (approvedWork.get(Boolean.FALSE).size() >= work.getApprovals().size()) { // unanimous disapproval
                    state = 0;
                } else {// there are some voted to approved
                    state = 1;
                }

            }

            if (state == 0) { // we have a unanimous dis-approval
                archiveApprovals(work);
                // go to branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorAtleastOneApprovalActivity::getOnRejected,
                                xorAtleastOneApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (state == 2) {// at least one approval
                archiveApprovals(work);
                // go to approved branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorAtleastOneApprovalActivity::getOnApproved,
                                xorAtleastOneApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else {
                // don't move to next step but takeout the current owner so that he doesn't see
                // it in his ownership any more
                work.removeOwner(user);
            }
        } else if (activity.getClass().equals(XorMajorityApprovalActivity.class)) {
            XorMajorityApprovalActivity xorMajorityApprovalActivity = (XorMajorityApprovalActivity) activity;
            int countApprove = 0;
            int countDisapprove = 0;

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

            if (countApprove > majorityVote) { // must be strictly bigger than since >= could mean a tie
                archiveApprovals(work);
                // go to approved branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorMajorityApprovalActivity::getOnApproved,
                                xorMajorityApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (countDisapprove > majorityVote) {
                archiveApprovals(work);
                // go to branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        this.evaluateBranchesForNextActivty(
                                xorMajorityApprovalActivity::getOnRejected,
                                xorMajorityApprovalActivity::getByDefault,
                                work,
                                user,
                                bizProcess),
                        nextSteps);
            } else if (countDisapprove == countApprove
                    && countDisapprove + countApprove == work.getApprovals().size()) { // everyone voted and its a tie
                archiveApprovals(work);
                // go to branch
                dealWithNextStep(work,
                        tenant,
                         user,
                        bizProcess,
                        (Activity) xorMajorityApprovalActivity.getOnTieBreaker().getNext(),
                        nextSteps);
            } else {
                // don't move to next step but takeout the current owner so that he doesn't see
                // it in his ownership any more
                work.removeOwner(user);
            }
        }
    }

    private void handleSupervisorApproval(final T root,
            final String tenant,
            final Activity next,
            final Activity activity,
            final List<Activity> nextSteps,
            final BizUser user,
            final BizProcess bizProcess,
            final List<String> supervisorHierarchy) {
        if (isSupervisorNeeded(root)) { // this was sent for approval before
            if (root.getApprovals().isEmpty() == Boolean.FALSE
                    && Boolean.TRUE.equals(root.getApprovals().iterator().next().getApproved())) { // approved. This is
                // supervisor so it
                // should only have 1
                // approval
                // see which level is the approval on
                String currentApprLevel = root.getSupervisorApprovalLevel();
                int indexOfNextApprLevel = getArrayIndexOfValue(supervisorHierarchy, currentApprLevel) + 1;

                // if we are at the end of the hierarchy
                if (indexOfNextApprLevel == supervisorHierarchy.size()) {
                    root.setSupervisorApprovalSeeker(null);
                    root.setSupervisorApprovalLevel(null);
                    archiveApprovals(root);
                    dealWithNextStep(root, tenant,user,bizProcess, next, nextSteps);
                } else {// if we are still not at the end
                    // find next role
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
                                        root.clearOwners();
                                        root.addOwner(approver);
                                        // root.getOwners().add(approver);
                                        // if (approver.getUsername() != null) {
                                        // root.getOwners().add(approver.getUsername());
                                        // } else {
                                        // root.getOwners().add(approver.getLoginName());
                                        // }
                                        root.setSupervisorApprovalLevel(nextRole);

                                        // archive first
                                        archiveApprovals(root);
                                        // then add the new approver
                                        // loadUserIntoApprovalList(approver.getLoginName(), activity, tenant,
                                        // root);//for apperovals, we log the current activity for supervisor approval
                                        loadUserIntoApprovalList(
                                                approver.getUsername(),
                                                approver.getFirstName(),
                                                approver.getLastName(),
                                                root,
                                                activity);
                                        nextSteps.add(activity); // if need supervisor, stay in the same activity first

                                    });

                        });
                    }

                }
            } else {// not approved. Reassign back to original user
                root.clearOwners();
                BizUser supervisorApprovalSeeker = getBizUserService().getUser(root.getSupervisorApprovalSeeker());
                // root.getOwners().add(supervisorApprovalSeeker);
                root.addOwner(supervisorApprovalSeeker);

                archiveApprovals(root);
                root.setSupervisorApprovalLevel(null);
                root.setSupervisorApprovalSeeker(null);
                nextSteps.add(activity);
            }
        } else { // seeking new approval

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
                        root.clearOwners();
                        root.addOwner(approver);
                        // root.getOwners().add(approver);
                        // if (approver.getUsername() != null) {
                        // root.getOwners().add(approver.getUsername());
                        // } else {
                        // root.getOwners().add(approver.getLoginName());
                        // }
                        root.setSupervisorApprovalLevel(nextRole);

                        // archive first
                        archiveApprovals(root);
                        // then add the new approver
                        // loadUserIntoApprovalList(approver.getLoginName(), activity, tenant,
                        // root);//for apperovals, we log the current activity for supervisor approval
                        loadUserIntoApprovalList(
                                approver.getUsername(),
                                approver.getFirstName(),
                                approver.getLastName(),
                                root,
                                activity);

                        nextSteps.add(activity); // if need supervisor, stay in the same activity first
                    });
                });
            }
            root.setSupervisorApprovalSeeker(user.getUsername());

        }

    }

    private void dealWithNextStep(
            T work,
            String tenant,
            BizUser user,
            BizProcess bizProcess,
            Activity nextActivity,
            List<Activity> nextSteps
    ) {

        work.clearOwners(); // so that the next folks can pick it up

        work.setSupervisorApprovalSeeker(null);// nullify the approval seeker too

        // only current activity ids in wait states
        if (nextActivity == null) { // nextActivity==END
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorUnanimousApprovalActivity.class)) {
            XorUnanimousApprovalActivity xorUnanimousApprovalActivity = (XorUnanimousApprovalActivity) nextActivity;
            loadUsersIntoApprovalList(xorUnanimousApprovalActivity.getHandledBy(),
                    nextActivity,
                    tenant,
                    work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorAtleastOneApprovalActivity.class)) {
            XorAtleastOneApprovalActivity xorAtleastOneApprovalActivity = (XorAtleastOneApprovalActivity) nextActivity;
            loadUsersIntoApprovalList(
                    xorAtleastOneApprovalActivity.getHandledBy(),
                    nextActivity,
                    tenant,
                    work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(XorMajorityApprovalActivity.class)) {
            XorMajorityApprovalActivity xorMajorityApprovalActivity = (XorMajorityApprovalActivity) nextActivity;
            loadUsersIntoApprovalList(
                    xorMajorityApprovalActivity.getHandledBy(),
                    nextActivity,
                    tenant,
                    work);
            nextSteps.add(nextActivity);
        } else if (nextActivity.getClass().equals(HumanActivity.class)) {
            HumanActivity humanActivity = (HumanActivity) nextActivity;
            nextSteps.add(humanActivity);
        } else if (nextActivity.getClass().equals(DirectHumanActivity.class)) {
            DirectHumanActivity directHumanActivity = (DirectHumanActivity) nextActivity;
            String targetWorklist = directHumanActivity.getHandledBy();
            
            BizUser bizUser = getTargetDirectlySentTo(directHumanActivity, work, user, bizProcess);
            if (bizUser.getClientRoles().contains(targetWorklist)) {
                work.setOwners(Set.of(bizUser));
            }
            nextSteps.add(directHumanActivity);
            
        } else { // service
            nextSteps.add(nextActivity);
        }
    }

    private BizUser getTargetDirectlySentTo(
            DirectHumanActivity directHumanActivity, 
            T work, 
            BizUser currentUser, 
            BizProcess bizProcess) {
        
        String expr = directHumanActivity.getDirectlySentTo();
        if (StringUtils.startsWith(expr, EXPR_OPEN) && StringUtils.endsWith(expr, EXPR_CLOSE)){
            String targetUserName = StringUtils.substringBetween(expr, EXPR_OPEN,EXPR_CLOSE);
            String targetUser = (String) getFunctionExpression().evaluate(targetUserName, work, currentUser, bizProcess);
            BizUser targetBizUser = bizUserService.getUser(targetUser);
            return targetBizUser;
        }else{
            BizUser targetBizUser = bizUserService.getUser(expr);
            return targetBizUser;
        }
        
        
    }
 

    private void loadUserIntoApprovalList(
            String loginName,
            String firstName,
            String lastName,
            T work,
            Activity nextActivity) {
        Approval approval = new Approval();
        approval.setUsername(loginName);
        approval.setFirstName(firstName);
        approval.setLastName(lastName);
        approval.setCurrentWorklist(nextActivity.getId());
        work.addApproval(approval);
    }

    private Boolean isSupervisorNeeded(T work) {
        return work.getSupervisorApprovalSeeker() != null; // there is a need to get supervisor's approval
    }

    private void loadUsersIntoApprovalList(
            String role,
            Activity nextActivity,
            final String tenant,
            T work) {
        List<BizUser> users = getBizUserService().getUsersByRole(role);

        archiveApprovalsAndSave(work);
        for (BizUser user : users) {
            loadUserIntoApprovalList(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    work,
                    nextActivity);
        }

    }

    private void archiveApprovals(T work) {
        archiveApprovalsAndSave(work);
    }

    private void createApprovalAndAssociateToWork(
            T work,
            BizUser user,
            Set<Approval> approvals) {

        Approval approval = new Approval();
        if (user.isEnabled()) {
            approval.setUsername(user.getUsername());
            approval.setFirstName(user.getFirstName());
            approval.setUsername(user.getLastName());
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

    public List<StartEvent> whatUserCanStart(List<String> roles, BizProcess bizProcess) {

        List<StartEvent> startEvents = bizProcess
                .getStartEvents()
                .stream()
                .filter(e -> {
                    Set<String> intersect = e.getCanBeStartedBy().stream().map(String::toLowerCase)
                            .collect(Collectors.toSet());
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

    public Map<String, String> findWorklistsByRoles(Set<String> roles, BizProcess bizProcess) {
        Map<String, Activity> activities = getActivities(bizProcess);
        Map<String, String> worklists = activities
                .values()
                .stream()
                .filter(a -> a.getClass().equals(HumanActivity.class))
                .map(HumanActivity.class::cast)
                .filter(ha -> roles.contains(StringUtils.lowerCase(ha.getHandledBy())))
                .collect(Collectors.toMap(HumanActivity::getId, HumanActivity::getDescription));
        return worklists;
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
        if (activity == null) { // start event
            return Boolean.FALSE;
        }
        Map<String, Activity> activities = getActivities(bizProcess);
        if (activities.get(activity) == null) { // cater for updating workflow id in workflow.xml but data is old
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
    
    private String getCondition(ConditionalBranch branch){
        if (branch==null){
            return "";
        }
        if (branch.getCondition()==null){
            return "";
        }
        return StringUtils.substringBetween(branch.getCondition(),EXPR_OPEN, EXPR_CLOSE);
    }
    
   

    private Activity evaluateBranchesForNextActivty(
            final Supplier<List<? extends ConditionalBranch>> getBranches,
            final Supplier<DefaultBranch> getDefaultBranch,
            final T work,
            final BizUser user,
            final BizProcess bizProces) {

        // state=0: no activity has condition == true
        // state=1: 1 activity has condition == true
        // state=2: multiple activities has condition == true (in this case, we have no
        // guarantee which branch is executed)
        Activity nextStep = null;
        for (var branch : getBranches.get()) {
            Boolean result = getPredicateExpression().evaluate(getCondition(branch), work, user, bizProces);
            if (result == true) {
                nextStep = (Activity) branch.getNext(); // deal with state=1
                break; // deal with state=2
            }

        }
        // deal with state=0
        if (nextStep == null) {// if no condition triggered, the branch is executed
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

    public abstract T save(T work);

    @Transactional
    public T initializeAndSave(
            final T newwork,
            final BizUser bizUser,
            final String context,
            final StartEvent startEvent,
            final BizProcess bizProcess) {

        newwork.setContext(context);
        newwork.setCreator(bizUser.getUsername());
        newwork.setPriority(Priority.NONE);
        newwork.setStatus(Status.NEWLY_CREATED);
        BizUser bu = bizUserService.save(bizUser);
                

        Set<BizUser> owners = new HashSet<>();
        owners.add(bu);
        newwork.setOwners(owners);
        newwork.setStartEventId(startEvent.getId());
        newwork.setStartEventDescription(bizProcess.getStartEvents().iterator().next().getDescription());
        newwork.setWorklist(bizProcess.getStartEvents().iterator().next().getId());
        newwork.setWorklistUpdateTime(LocalDateTime.now());

        T w = save(newwork);
        return w;
    }

    public T findById(final Long id, final String context) {
        T work = findById(
                id,
                context);
        return work;
    }

    private T archiveApprovalsAndSave(T work) {
        if (work.getApprovals() != null && !work.getApprovals().isEmpty()) {
            work.getHistoricalApprovals().addAll(work.getApprovals());
            work.clearApprrovals();
        }
        return work;
    }

    private T findById(Long id) {
        Optional<T> work = getWorkItemRepo().findById(id);
        return work.orElse(null);
    }

    public Integer countWorkByOwner(
            Class<T> workItemClass,
            String username,
            SearchTermProvider searchTermProvider) {

        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getWorkItemRepo()
                    .count(
                            whereOwnersOrUndecidedApprovalsContains(
                                    username));
            return count.intValue();
        } else {
            Long count = getWorkItemSearchRepo()
                    .countWhereOwnersOrUndecidedApprovalsContains(
                            searchTermProvider.getSearchTerm(),
                            username);
            return count.intValue();
        }
    }

    public DataProvider getWorkByOwner(
            Class<T> workItemClass,
            String username,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sorted = pageNav.getSortField();
                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<T> finapps = getWorkItemRepo().findAll(whereOwnersOrUndecidedApprovalsContains(username),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));
                    return finapps.stream();
                } else {
                    Page<T> finapps = getWorkItemSearchRepo().findAllWhereOwnersOrUndecidedApprovalsContains(
                            searchTermProvider.getSearchTerm(),
                            username,
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, modifySortFieldForSearch(sorted, workItemClass))));
                    return finapps.stream();
                }
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

    public Integer countWorkByCreator(
            Class<T> workItemClass,
            String username,
            SearchTermProvider searchTermProvider) {
        if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
            Long count = getWorkItemRepo().count(whereCreatorEquals(username));
            return count.intValue();
        } else {
            Long count = getWorkItemSearchRepo().countByCreator(searchTermProvider.getSearchTerm(), username);
            return count.intValue();
        }
    }

    public DataProvider<T, Void> getWorkByCreator(
            Class<T> workItemClass,
            String username,
            SearchTermProvider searchTermProvider,
            PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
                String sorted = pageNav.getSortField();
                query.getPage();
                if (searchTermProvider == null || StringUtils.isEmpty(searchTermProvider.getSearchTerm())) {
                    Page<T> finapps = getWorkItemRepo().findAll(
                            whereCreatorEquals(username),
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, sorted)));
                    return finapps.stream();
                } else {
                    Page<T> finapps = getWorkItemSearchRepo().findByCreator(searchTermProvider.getSearchTerm(),
                            username,
                            PageRequest.of(
                                    pageNav.getPage() - 1,
                                    pageNav.getMaxCountPerPage(),
                                    Sort.by(sort, modifySortFieldForSearch(sorted, workItemClass))));
                    return finapps.stream();
                }
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

    public Integer countWorkByWorklist(String worklist) {
        Long count = getWorkItemRepo().count(whereNoOwnerAndWorklistEquals(worklist));// countByWorklistAndNoOwner(worklist);//count(whereWorklistEquals(worklist));
        return count.intValue();
    }

    private String modifySortFieldForSearch(String sortField, Class<T> workItemClass) {
        Field field = WorkflowUtils.getField(workItemClass, sortField);
        if (Number.class.isAssignableFrom(field.getType())
                || LocalDateTime.class.isAssignableFrom(field.getType())
                || LocalDate.class.isAssignableFrom(field.getType())
                || Date.class.isAssignableFrom(field.getType())) {
            return sortField;
        } else {

            return sortField + ".keyword";
        }
    }

    public DataProvider<T, Void> getWorkByWorklist(Class<T> workItemClass, String worklist, PageNav pageNav) {
        // build data provider
        var dp = new AbstractBackEndDataProvider<T, Void>() {
            @Override
            protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
                Sort.Direction sort = pageNav.getAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;

                String sorted = pageNav.getSortField();
                query.getPage();

                Page<T> finapps = getWorkItemRepo().findAll(whereNoOwnerAndWorklistEquals(worklist),
                        PageRequest.of(
                                pageNav.getPage() - 1,
                                pageNav.getMaxCountPerPage(),
                                Sort.by(sort, sorted)));
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

    private Specification<T> whereOwnersOrUndecidedApprovalsContains(String username) {
        return (workItem, cq, cb) -> {
            SetJoin<T, BizUser> owners = workItem.joinSet("owners", JoinType.LEFT);
            SetJoin<T, Approval> approvals = workItem.joinSet("approvals", JoinType.LEFT);
            return cb.or(
                    cb.equal(owners.get("username"), username),
                    cb.and(
                            cb.equal(approvals.get("username"), username),
                            cb.isNull(approvals.get("approved"))));
        };
    }

    private Specification<T> whereCreatorEquals(String username) {
        return (workItem, cq, cb) -> {
            return cb.equal(workItem.get("creator"), username);
        };
    }

    private Specification<T> whereNoOwnerAndWorklistEquals(String worklist) {
        return (workItem, cq, cb) -> {
            SetJoin<T, BizUser> children = workItem.joinSet("owners", JoinType.LEFT);
            return cb.and(
                    cb.equal(workItem.get("worklist"), worklist),
                    children.isNull());
        };
    }

    /**
     * @return the workItemRepo
     */
    public abstract WorkItemRepository<T> getWorkItemRepo();

    /**
     * @return the workItemSearchRepo
     */
    public abstract WorkItemSearchRepository<T> getWorkItemSearchRepo();

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
    public final PredicateExpression<T> getPredicateExpression() {
        return predicateExpression;
    }

    /**
     * @param expr the expr to set
     */
    @Autowired
    public final void setPredicateExpression(PredicateExpression<T> expr) {
        this.predicateExpression = expr;
    }

    /**
     * @return the expr
     */
    public final FunctionExpression<T> getFunctionExpression() {
        return functionExpression;
    }

    /**
     * @param expr the expr to set
     */
    @Autowired
    public final void setFunctionExpression(FunctionExpression<T> expr) {
        this.functionExpression = expr;
    }

    /**
     * @return the approvalService
     */
    public ApprovalService getApprovalService() {
        return approvalService;
    }

    /**
     * @param approvalService the approvalService to set
     */
    @Autowired
    public void setApprovalService(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * @return the emFactory
     */
    public EntityManagerFactory getEmFactory() {
        return emFactory;
    }

    /**
     * @param emFactory the emFactory to set
     */
    @Autowired
    public void setEmFactory(EntityManagerFactory emFactory) {
        this.emFactory = emFactory;
    }
}
