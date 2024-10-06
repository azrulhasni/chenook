/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.ApprovalService;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.users.UserField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
@SpringComponent
public class WorkflowPanel<T extends WorkItem> extends FormLayout {

    private final BadgeUtils badgeUtils;
    private final ApprovalService approvalService;
    private final BizUserService bizUserService;
    private final WorkflowService workflowService;
    private final Integer COUNT_PER_PAGE = 3;
    private Approval approval;
    private WorkflowAwareGroup group;
    private OidcUser user;
    private T work;

    public static <T extends WorkItem> WorkflowPanel create(
            final T work,
            final OidcUser user) {
        var workPanel = ApplicationContextHolder.getBean(WorkflowPanel.class);
        workPanel.init(work, user);
        return workPanel;
    }

    private WorkflowPanel(
            @Autowired BadgeUtils badgeUtils,
            @Autowired ApprovalService approvalService,
            @Autowired BizUserService bizUserService,
            @Autowired WorkflowService workflowService) {
        this.badgeUtils = badgeUtils;
        this.approvalService = approvalService;
        this.bizUserService = bizUserService;
        this.workflowService = workflowService;
    }

    private void init(
            final T work,
            final OidcUser user
    ) {
        this.approval = new Approval();
        var fieldDisplayMap = WorkflowUtils.getFieldNameDisplayNameMap(work.getClass());
        this.group = WorkflowAwareGroup.createEnabledIfApprrovalNeeded(work, user);
        this.work = work;
        this.user = user;

        Select<Status> cbStatus = createSelect(fieldDisplayMap.get("status"));
        cbStatus.setItems(Status.values());
        cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
        if (work != null) {
            cbStatus.setValue(work.getStatus());
        } else {
            cbStatus.setValue(Status.NEWLY_CREATED);
        }
        cbStatus.setReadOnly(true);
        Button btnWorkflow = new Button("...", e -> createWorkflowInfoDialog(work, user));
        btnWorkflow.getStyle().set("align-self", "end");
        btnWorkflow.setHeight(cbStatus.getHeight());
        cbStatus.getStyle().set("width", "50em");
        HorizontalLayout workflowField = new HorizontalLayout();
        workflowField.add(cbStatus);
        workflowField.add(btnWorkflow);
        workflowField.getStyle().set("width", "50em");
        this.add(workflowField);

        if (isWaitingApproval(work, user)) {
            Binder<Approval> binder = new Binder<>(Approval.class);
            binder.setBean(approval);

            WorkflowAwareComboBox<Approval, Boolean> cbApprove = WorkflowAwareComboBox.<Approval, Boolean>create("approved", binder, Set.of(Boolean.TRUE, Boolean.FALSE), group);
            cbApprove.setId("approvalNeeded");
            cbApprove.setItemLabelGenerator(a -> {
                if (Boolean.FALSE.equals(a)) {
                    return "Reject";
                } else {
                    return "Approve";
                }
            });

            cbApprove.getStyle().setWidth("28em");
            if (cbApprove != null) {
                this.add(cbApprove);
            }
        }
    }

    private Boolean isWaitingApproval(
            final T work, 
            final OidcUser user
    ) {
        return work.getApprovals().stream().filter(
                a -> StringUtils.equals(
                        a.getUsername(),
                        user.getPreferredUsername()
                )).count() > 0;
    }

    public void createWorkflowInfoDialog(WorkItem work, OidcUser oidcUser) {
        Dialog workflowDialog = new Dialog();
        TextField tf = new TextField();
        tf.setLabel("Current worklist");
        tf.setValue(work.getWorklist());
        tf.setReadOnly(true);
        workflowDialog.add(tf);
        VerticalLayout approvalPanel = buildApprovalPanel(work, workflowDialog);
        workflowDialog.add(approvalPanel);
        VerticalLayout ownerPanel = buildOwnerPanel(work, workflowDialog);
        workflowDialog.add(ownerPanel);
        workflowDialog.add(new Button("Done", e -> workflowDialog.close()));
        workflowDialog.open();
    }

    private VerticalLayout buildApprovalPanel(WorkItem work, Dialog approvalDialog) {
        VerticalLayout approvalPanel = new VerticalLayout();
        PageNav nav = new PageNav();
        Integer count = approvalService.countApprovalsByWork(work);//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = approvalService.getApprovalsByWork(work, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<Approval> grid = new Grid<>();
        grid.setItems(dataProvider);
        grid.addComponentColumn(approval -> {
            BizUser bizUser = new BizUser();
            bizUser.setFirstName(approval.getFirstName());
            bizUser.setLastName(approval.getLastName());
            bizUser.setUsername(approval.getUsername());

            UserField userField = new UserField(bizUser);
            HorizontalLayout panel = new HorizontalLayout();
            panel.add(userField);
            if (approval.getApproved() == null) {
                Span confirmed = new Span("No decision");
                confirmed.getElement().getThemeList().add("badge contrast pill");
                panel.add(confirmed);
            } else {
                if (approval.getApproved()) {
                    Span confirmed = new Span("Approved");
                    confirmed.getElement().getThemeList().add("badge success pill");
                    panel.add(confirmed);
                } else if (approval.getApproved()) {
                    Span confirmed = new Span("Disapproved");
                    confirmed.getElement().getThemeList().add("badge error pill");
                    panel.add(confirmed);
                }
            }

            return panel;
        });
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(Approval.class);
        grid.setMaxHeight("calc(" + COUNT_PER_PAGE + " * var(--lumo-size-m))");
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
        H4 approvalTitle = new H4("Approvals");
        approvalPanel.add(approvalTitle);
        approvalPanel.add(nav);
        approvalPanel.add(grid);
        return approvalPanel;
    }

    private VerticalLayout buildOwnerPanel(WorkItem work, Dialog approvalDialog) {
        VerticalLayout ownerPanel = new VerticalLayout();
        PageNav nav = new PageNav();
        Integer count = bizUserService.countWorkByOwner(work);
        DataProvider dataProvider = bizUserService.getOwnersByWork(work, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<BizUser> grid = new Grid<>();
        grid.setItems(dataProvider);
        grid.setMaxHeight("calc(" + COUNT_PER_PAGE + " * var(--lumo-size-m))");
        grid.addComponentColumn(owner -> {
            BizUser user = new BizUser();
            user.setFirstName(owner.getFirstName());
            user.setLastName(owner.getLastName());
            user.setUsername(owner.getUsername());

            UserField userField = new UserField(user);
            HorizontalLayout panel = new HorizontalLayout();
            panel.add(userField);
            return panel;
        });

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(BizUser.class);
        grid.setPageSize(COUNT_PER_PAGE);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
        H4 approvalTitle = new H4("Owners");
        ownerPanel.add(approvalTitle);
        ownerPanel.add(nav);
        ownerPanel.add(grid);

        return ownerPanel;
    }

    public Boolean validate() {
        if (isWaitingApproval(work, user)) {
            if (approval.getApproved() == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public Boolean getApproval() {
        return approval.getApproved();
    }

    private <T> Select<T> createSelect(
            final String label
    ) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        return select;
    }

}
