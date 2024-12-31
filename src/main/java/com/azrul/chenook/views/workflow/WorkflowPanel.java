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
import com.azrul.chenook.views.users.UserField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
@SpringComponent
public class WorkflowPanel<T extends WorkItem> extends CustomField<Set<Approval>> {

    private final ApprovalService approvalService;
    private final BizUserService<T> bizUserService;
    private       WorkflowService<T> workflowService;
    private final Integer COUNT_PER_PAGE = 3;
    private Binder<T> binder;
    private WorkflowAwareGroup<T> group;

    public static <T extends WorkItem> WorkflowPanel<T> create(
            final WorkflowService<T> workflowService,
            final String fieldName,
            final Binder<T> binder,
            final BizUser user,
            final WorkflowAwareGroup<T> group,
            final Function<T,Component> workflowDisplay) {

        T workItem = binder.getBean();
        
        var field = ApplicationContextHolder.getBean(WorkflowPanel.class);
        field.init(fieldName, binder, user, group,workflowDisplay);
        field.setId(fieldName);

         List<Validator> validators = new ArrayList<>();
        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(), 
                fieldName);
        
        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap, 
                field
        );
        
        validators.addAll(
                WorkflowUtils.applyNotNull(
                    annoFieldDisplayMap, 
                    field, 
                    workfieldMap, 
                    fieldName
                )
        );
        
        validators.addAll(
                WorkflowUtils.<T>applyApprovalValidator(
                        field, 
                        binder, 
                        user, 
                        "Missing approvals"
                )
        );
        
        

        var bindingBuilder = binder.forField(field);
        
        bindingBuilder.withNullRepresentation("");
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

    private WorkflowPanel(
            @Autowired ApprovalService approvalService,
            @Autowired BizUserService<T> bizUserService,
            @Autowired WorkflowService<T> workflowService
            ) {
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
            final BizUser user,
            final WorkflowAwareGroup<T> group,
            final Function<T,Component> workflowDisplay) {
        T work = binder.getBean();
        this.binder = binder;
        if (work.getApprovals() == null) {
            return;
        }
        HorizontalLayout workflowField = new HorizontalLayout();
        
        Component cWorkflowDisplay = workflowDisplay.apply(work);
            workflowField.add(cWorkflowDisplay);
        
        Optional<Approval> oapproval = work.getApprovals().stream()
                .filter(a -> StringUtils.equals(user.getUsername(), a.getUsername())).findAny();
        oapproval.ifPresent(approval -> {
            workflowField.getStyle().set("width", "100%");

            if (WorkflowUtils.isWaitingApproval(work, user)) {
                Button btnApproval = new Button("Approval", e -> {
                    Dialog approvalDialog = new Dialog();
                    approvalDialog.setHeaderTitle("Approval");
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
                    Button btnClose = new Button("Close", e1 -> {
                        approvalDialog.close();
                    });
                    approvalDialog.getFooter().add(btnClose);
                    btnClose.setId("btnClose");
                    approvalDialog.open();
                });
                btnApproval.setId("btnApproval");
                btnApproval.getStyle().set("align-self", "center");
                btnApproval.addThemeVariants(ButtonVariant.LUMO_SMALL);

                workflowField.add(btnApproval);

            }
            
        });
        Button btnWorkflow = new Button("Workflow Info.", e -> createWorkflowInfoDialog(work, user, fieldName));
        btnWorkflow.setId("btnWorkflowInfo");
        btnWorkflow.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnWorkflow.getStyle().set("align-self", "center");
        workflowField.add(btnWorkflow);
        this.add(workflowField);

    }

    public void createWorkflowInfoDialog(T work, BizUser user, String fieldName) {
        Dialog workflowDialog = new Dialog();
        workflowDialog.setHeaderTitle("Workflow information");
        workflowDialog.setWidth("40em");
        TextField tf = new TextField();
        tf.setLabel("Current worklist");
        tf.setValue(work.getWorklist());
        tf.setReadOnly(true);
        Div content = new Div();
        content.add(tf);
        workflowDialog.add(content);
        
        VerticalLayout approvalPanel = buildApprovalPanel(work, fieldName);
        workflowDialog.add(approvalPanel);
        VerticalLayout histApprovalPanel = buildHistoricalApprovalPanel(work, fieldName);
        workflowDialog.add(histApprovalPanel);
        VerticalLayout ownerPanel = buildOwnerPanel(work, workflowDialog);
        workflowDialog.add(ownerPanel);
        Button btnDone = new Button("Done", e -> workflowDialog.close());
        btnDone.setId("btnDone");
        workflowDialog.getFooter().add(btnDone);
        workflowDialog.open();
    }

    private VerticalLayout buildApprovalPanel(T work, String fieldName) {
        VerticalLayout approvalPanel = new VerticalLayout();
//        PageNav nav = new PageNav();
//        WorkflowUtils.getSortableFields(Approval.class);
//        Integer count = approvalService.countApprovalsByWork(work);// finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
//        DataProvider dataProvider = approvalService.getApprovalsByWork(work, nav);// finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(),
//        // nav);
//        Grid<Approval> grid = new Grid<>();
//        grid.setItems(dataProvider);
//        grid.addComponentColumn(approval -> {
//            return buildApprovalRow(approval);
//        });
//        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(Approval.class);
//        grid.setMaxHeight("calc(" + (COUNT_PER_PAGE + 1.5) + " * var(--lumo-size-m))");
//        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
        H4 approvalTitle = new H4("Approvals");
        approvalPanel.add(approvalTitle);
        GridMemento<Approval> approvalMemento =  WorkflowAwareGridBuilder.build(
                    fieldName,
                    Approval.class,
                    m-> approvalService.countApprovalsByWork(work),
                    m-> approvalService.getApprovalsByWork(work,m.getPageNav()),
                    Optional.of(()->createApprovalGrid()));
        approvalPanel.add(approvalMemento.getPanel());
        return approvalPanel;
    }

    private VerticalLayout buildHistoricalApprovalPanel(T work, String fieldName) {
        
        VerticalLayout approvalPanel = new VerticalLayout();
        PageNav nav = new PageNav();
     
        H4 approvalTitle = new H4("Past Approvals");
        approvalPanel.add(approvalTitle);
       
        
        GridMemento<Approval> approvalMemento =  WorkflowAwareGridBuilder.build(
                    fieldName,
                    Approval.class,
                    m-> approvalService.countHistoricalApprovalsByWork(work),
                    m-> approvalService.getHistoricalApprovalsByWork(work,m.getPageNav()),
                    Optional.of(()->createApprovalGrid()));
        approvalPanel.add(approvalMemento.getPanel());
        return approvalPanel;
    }

    private Grid<Approval> createApprovalGrid() {
        Grid<Approval> grid = new Grid<>(Approval.class, false);
        grid.addComponentColumn(approval -> {
            return buildApprovalRow(approval);
        });
        grid.setMaxHeight("calc(" + (COUNT_PER_PAGE + 1.5) + " * var(--lumo-size-m))");
        return grid;
    }

    private HorizontalLayout buildApprovalRow(Approval approval) {
        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(Approval.class, "approvalDateTime");
        
        BizUser bizUser = new BizUser();
        bizUser.setFirstName(approval.getFirstName());
        bizUser.setLastName(approval.getLastName());
        bizUser.setUsername(approval.getUsername());
        
        Dialog noteDialog = new Dialog();
        noteDialog.setHeaderTitle("Notes");
        VerticalLayout notePanel = new VerticalLayout();
        noteDialog.add(notePanel);
        
        TextField worklist = new TextField();
        worklist.setLabel("Worklist");
        worklist.setValue(approval.getCurrentWorklist()==null?"":approval.getCurrentWorklist());
        worklist.setReadOnly(true);
        notePanel.add(worklist);
        
        TextArea note = new TextArea();
        note.setLabel("Notes");
        note.setValue(approval.getNote()==null?"":approval.getNote());
        note.setReadOnly(true);
        notePanel.add(note);
        
        
        Button btnClose = new Button("Close", e3 -> {
            noteDialog.close();
        });
        btnClose.setId("btnClose");
        noteDialog.getFooter().add(btnClose);
        
        
        UserField userField = new UserField(bizUser);
        
        HorizontalLayout panel = new HorizontalLayout();
        panel.add(userField);
        if (approval.getApproved() == null) {
            Span confirmed = new Span("No decision");
            confirmed.addClickListener(e->noteDialog.open());
            confirmed.setId("spanApproval");
            confirmed.getElement().getThemeList().add("badge contrast pill");
            panel.add(confirmed);
        } else if (approval.getApproved()) {
            WorkflowUtils.formatDateTime(annoFieldDisplayMap, approval.getApprovalDateTime()).ifPresentOrElse(dt->{
                Span confirmed = new Span("Approved ["+dt+"]");
                confirmed.addClickListener(e->noteDialog.open());
                confirmed.getElement().getThemeList().add("badge success pill");
                confirmed.setId("spanApproval");
                panel.add(confirmed);
            },
            ()->{
                Span confirmed = new Span("Approved");
                confirmed.addClickListener(e->noteDialog.open());
                confirmed.getElement().getThemeList().add("badge success pill");
                panel.add(confirmed);
            });
            
        } else {
            WorkflowUtils.formatDateTime(annoFieldDisplayMap, approval.getApprovalDateTime()).ifPresentOrElse(dt->{
                Span confirmed = new Span("Disapproved ["+dt+"]");
                confirmed.addClickListener(e->noteDialog.open());
                confirmed.setId("spanApproval");
                confirmed.getElement().getThemeList().add("badge  error  pill");
                panel.add(confirmed);
            },
            ()->{
                Span confirmed = new Span("Disapproved");
                confirmed.addClickListener(e->noteDialog.open());
                confirmed.setId("spanApproval");
                confirmed.getElement().getThemeList().add("badge  error  pill");
                panel.add(confirmed);
            });
        }
        
        
        return panel;
    }

    private VerticalLayout buildOwnerPanel(T work, Dialog approvalDialog) {
        VerticalLayout ownerPanel = new VerticalLayout();
        
         GridMemento<BizUser> bizUserMemento =  WorkflowAwareGridBuilder.build(
                    "btnMyBizUser",
                    BizUser.class,
                    m-> bizUserService.countWorkByOwner(work),
                    m-> bizUserService.getOwnersByWork(work, m.getPageNav()),
                    Optional.of(()->createBizUserGrid()));

        H4 approvalTitle = new H4("Owners");
        ownerPanel.add(approvalTitle);
        ownerPanel.add(bizUserMemento.getPanel());

        return ownerPanel;
    }

    private Grid<BizUser> createBizUserGrid() {
        Grid<BizUser> grid = new Grid<>(BizUser.class, false);
        grid.setMaxHeight("calc(" + COUNT_PER_PAGE + " * var(--lumo-size-m))");
        grid.addComponentColumn(owner -> {
            BizUser ouser = new BizUser();
            ouser.setFirstName(owner.getFirstName());
            ouser.setLastName(owner.getLastName());
            ouser.setUsername(owner.getUsername());

            UserField userField = new UserField(ouser);
            HorizontalLayout panel = new HorizontalLayout();
            panel.add(userField);
            return panel;
        });
        //Map<String, String> sortableFields = WorkflowUtils.getSortableFields(BizUser.class);
        grid.setPageSize(COUNT_PER_PAGE);
        return grid;
    }


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
