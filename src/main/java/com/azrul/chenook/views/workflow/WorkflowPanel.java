/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.ApprovalService;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.validator.ApprovalValidator;
import com.azrul.chenook.views.users.UserField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
@SpringComponent
public class WorkflowPanel<T extends WorkItem> extends CustomField<Set<Approval>> {

    private final ApprovalService approvalService;
    private final BizUserService<T> bizUserService;
    private final WorkflowService<T> workflowService;
    private final Integer COUNT_PER_PAGE = 3;
    private Binder<T> binder;
    private String fieldName;
    private WorkflowAwareGroup<T> group;
    private OidcUser user;
    // private T work;

    public static <T extends WorkItem> WorkflowPanel<T> create(
            final String fieldName,
            final Binder<T> binder,
            final OidcUser user,
            final WorkflowAwareGroup<T> group,
            final Function<T,Component> workflowDisplay) {

        T workItem = binder.getBean();
        var field = ApplicationContextHolder.getBean(WorkflowPanel.class);
        field.init(fieldName, binder, user, group,workflowDisplay);
        List<Validator> validators = new ArrayList<>();
        field.setId(fieldName);

        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(),
                fieldName);

        validators.add(new ApprovalValidator(binder, user, "Approval not done yet"));

        var bindingBuilder = binder.forField(field);
        bindingBuilder.withNullRepresentation(Set.of());
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);

        return field;
    }

    private WorkflowPanel(
            @Autowired ApprovalService approvalService,
            @Autowired BizUserService<T> bizUserService,
            @Autowired WorkflowService<T> workflowService) {
        this.approvalService = approvalService;
        this.bizUserService = bizUserService;
        this.workflowService = workflowService;
    }

    public void applyGroup() {
        if (group != null) {
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }

    private void init(
            final String fieldName,
            final Binder<T> binder,
            final OidcUser user,
            final WorkflowAwareGroup<T> group,
            final Function<T,Component> workflowDisplay) {
        T work = binder.getBean();
        this.user = user;
        this.binder = binder;
        this.fieldName = fieldName;
        if (work.getApprovals() == null) {
            return;
        }
        HorizontalLayout workflowField = new HorizontalLayout();
        Optional<Approval> oapproval = work.getApprovals().stream()
                .filter(a -> StringUtils.equals(user.getPreferredUsername(), a.getUsername())).findAny();
        oapproval.ifPresent(approval -> {
//            var fieldDisplayMap = WorkflowUtils.getFieldNameDisplayNameMap(work.getClass());
//            Select<T> select = new Select<>();
//            select.setLabel(label);
//            Select<Status> cbStatus = new Select<>();
//            createSelect(fieldDisplayMap.get("status"));
//            cbStatus.setItems(Status.values());
//            cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
//            if (work != null) {
//                cbStatus.setValue(work.getStatus());
//            } else {
//                cbStatus.setValue(Status.NEWLY_CREATED);
//            }
//            cbStatus.setReadOnly(true);
//
//            cbStatus.getStyle().set("width", "100%");

            Component cWorkflowDisplay = workflowDisplay.apply(work);
            workflowField.add(cWorkflowDisplay);

            workflowField.getStyle().set("width", "100%");

            if (WorkflowUtils.isWaitingApproval(work, user)) {
                Button btnApproval = new Button("Approval", e -> {
                    Dialog approvalDialog = new Dialog();
                    VerticalLayout approvalPanel = new VerticalLayout();

                    Binder<Approval> approvalBinder = new Binder<>(Approval.class);
                    approvalBinder.setBean(approval);

                    WorkflowAwareComboBox<Approval, Boolean> cbApprove = WorkflowAwareComboBox
                            .<Approval, Boolean>create("approved", approvalBinder, Set.of(Boolean.TRUE, Boolean.FALSE), group);
                    cbApprove.setId("approvalNeeded");
                    cbApprove.setItemLabelGenerator(a -> {
                        if (Boolean.FALSE.equals(a)) {
                            return "Reject";
                        } else {
                            return "Approve";
                        }
                    });

                    cbApprove.getStyle().setWidth("28em");
                    approvalPanel.add(cbApprove);

                    TextArea taApprovalNote = WorkflowAwareTextArea.create("note", approvalBinder, group);
                    taApprovalNote.setId("approvalNote");

                    taApprovalNote.getStyle().setWidth("28em");
                    approvalPanel.add(taApprovalNote);

                    approvalDialog.add(approvalPanel);
                    Button btnClose = new Button("Close", e1 -> approvalDialog.close());
                    approvalDialog.getFooter().add(btnClose);
                    btnClose.setId("btnClose");
                    approvalDialog.open();
                });
                btnApproval.setId("btnApproval");
                btnApproval.getStyle().set("align-self", "end");
                //btnApproval.setHeight(cbStatus.getHeight());
                btnApproval.addThemeVariants(ButtonVariant.LUMO_SMALL);

                workflowField.add(btnApproval);

            }
            
        });
        Button btnWorkflow = new Button("Workflow Info.", e -> createWorkflowInfoDialog(work, user));
        btnWorkflow.setId("btnWorkflowInfo");
        btnWorkflow.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnWorkflow.getStyle().set("align-self", "end");
        workflowField.add(btnWorkflow);
        this.add(workflowField);
        //btnWorkflow.setHeight(cbStatus.getHeight());

    }

    public void createWorkflowInfoDialog(T work, OidcUser oidcUser) {
        Dialog workflowDialog = new Dialog();
        workflowDialog.setWidth("40em");
        TextField tf = new TextField();
        tf.setLabel("Current worklist");
        tf.setValue(work.getWorklist());
        tf.setReadOnly(true);
        Div content = new Div();
        content.add(tf);
        workflowDialog.add(content);
        VerticalLayout approvalPanel = buildApprovalPanel(work, workflowDialog);
        workflowDialog.add(approvalPanel);
        VerticalLayout histApprovalPanel = buildHistoricalApprovalPanel(work, workflowDialog);
        workflowDialog.add(histApprovalPanel);
        VerticalLayout ownerPanel = buildOwnerPanel(work, workflowDialog);
        workflowDialog.add(ownerPanel);
        workflowDialog.add(new Button("Done", e -> workflowDialog.close()));
        workflowDialog.open();
    }

    private VerticalLayout buildApprovalPanel(WorkItem work, Dialog approvalDialog) {
        VerticalLayout approvalPanel = new VerticalLayout();
        PageNav nav = new PageNav();
        Integer count = approvalService.countApprovalsByWork(work);// finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = approvalService.getApprovalsByWork(work, nav);// finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(),
        // nav);
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
            } else if (approval.getApproved()) {
                Span confirmed = new Span("Approved");
                confirmed.getElement().getThemeList().add("badge success pill");
                panel.add(confirmed);
            } else {
                Span confirmed = new Span("Disapproved");
                confirmed.getElement().getThemeList().add("badge error pill");
                panel.add(confirmed);
            }

            if (approval.getNote() != null) {
                TextArea note = new TextArea();

                note.setValue(approval.getNote());
                note.setReadOnly(true);
                panel.add(note);
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

    private VerticalLayout buildHistoricalApprovalPanel(WorkItem work, Dialog approvalDialog) {
        VerticalLayout approvalPanel = new VerticalLayout();
        PageNav nav = new PageNav();
        Integer count = approvalService.countHistoricalApprovalsByWork(work);// finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider<Approval, Void> dataProvider = approvalService.getHistoricalApprovalsByWork(work, nav);// finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(),
        // nav);
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
            } else if (approval.getApproved()) {
                Span confirmed = new Span("Approved");
                confirmed.getElement().getThemeList().add("badge success pill");
                panel.add(confirmed);
            } else {
                Span confirmed = new Span("Disapproved");
                confirmed.getElement().getThemeList().add("badge error pill");
                panel.add(confirmed);
            }

            if (approval.getNote() != null) {
                Dialog noteDialog = new Dialog();
                TextArea note = new TextArea();

                note.setValue(approval.getNote());
                note.setReadOnly(true);
                noteDialog.add(note);
                Button btnClose = new Button("Close", e3 -> noteDialog.close());
                btnClose.setId("btnClose");
                noteDialog.getFooter().add(btnClose);
                Button btnNote = new Button("...", e3 -> noteDialog.open());
                btnNote.getStyle().set("align-self", "end");
                panel.add(btnNote);
            }
            return panel;
        });
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(Approval.class);
        grid.setMaxHeight("calc(" + COUNT_PER_PAGE + " * var(--lumo-size-m))");
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
        H4 approvalTitle = new H4("Past Approvals");
        approvalPanel.add(approvalTitle);
        approvalPanel.add(nav);
        approvalPanel.add(grid);
        return approvalPanel;
    }

    private VerticalLayout buildOwnerPanel(T work, Dialog approvalDialog) {
        VerticalLayout ownerPanel = new VerticalLayout();
        PageNav nav = new PageNav();
        Integer count = bizUserService.countWorkByOwner(work);
        DataProvider<BizUser, Void> dataProvider = bizUserService.getOwnersByWork(work, nav);// finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(),
        // nav);
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

//    private <T> Select<T> createSelect(
//            final String label) {
//        Select<T> select = new Select<>();
//        select.setLabel(label);
//        return select;
//    }

    @Override
    protected Set<Approval> generateModelValue() {
        var approvals = binder
                .getBean()
                .getApprovals();
        if (approvals == null) {
            return new HashSet<>();
        } else {
            return approvals;
        }
    }

    @Override
    public Set<Approval> getValue() {
        return generateModelValue();
    }

    @Override
    protected void setPresentationValue(Set<Approval> newPresentationValue) {
        T work = binder.getBean();
        if (newPresentationValue != null) {
            work.setApprovals(newPresentationValue);
        } else {
            work.setApprovals(new HashSet<>());
        }
    }

    @Override
    public void setValue(Set<Approval> value) {
        setPresentationValue(value);
    }

}
