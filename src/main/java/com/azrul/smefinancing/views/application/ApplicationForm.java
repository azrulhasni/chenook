package com.azrul.smefinancing.views.application;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.service.MessageService;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.views.message.MessageButton;
import com.azrul.chenook.views.workflow.WorkflowPanel;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.service.ApprovalService;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.applicant.ApplicantForm;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.MapperService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.workflow.WorkflowAwareButton;
import com.azrul.chenook.views.common.converter.StringToUngroupLongConverter;
import com.azrul.chenook.views.workflow.WorkflowAwareComboBox;
import com.azrul.chenook.views.workflow.WorkflowAwareDateTimePicker;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.azrul.chenook.views.workflow.WorkflowAwareMoneyField;
import com.azrul.chenook.views.workflow.WorkflowAwareNumberField;
import com.azrul.chenook.views.workflow.WorkflowAwareTextArea;
import com.azrul.chenook.views.workflow.WorkflowAwareTextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.vaadin.addons.MoneyField;

public class ApplicationForm extends Dialog {

    private static final String ADD_APPLICANT = "Add applicant";
    private final FinApplicationService finappService;
    private final ApplicantService applicantService;
    private final MessageService msgService;
    private final WorkflowConfig workflowConfig;

    public static ApplicationForm create(
            StartEvent startEvent,
            FinApplication work,
            OidcUser user,
            String context,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel) {
        var applicationForm = ApplicationContextHolder.getBean(ApplicationForm.class);
        applicationForm.init(
                startEvent,
                work,
                user,
                context,
                onPostSave,
                onPostRemove,
                onPostCancel
        );
        return applicationForm;
    }

    private ApplicationForm(
            @Autowired FinApplicationService finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired WorkflowConfig workflowConfig,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = finappService;
        this.applicantService = applicantService;
        this.msgService = msgService;
        this.workflowConfig = workflowConfig;

    }

    private void init(
            StartEvent startEvent,
            FinApplication work,
            OidcUser user,
            String context,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        BizProcess bizProcess = workflowConfig.rootBizProcess();

        Binder<FinApplication> binder = new Binder<>(FinApplication.class);
        binder.setBean(work);

        WorkflowAwareGroup typicalGroup = WorkflowAwareGroup.create(
                user, 
                work, 
                bizProcess
        );
        
        WorkflowAwareGroup ifNewGroup = WorkflowAwareGroup.createEnabledIfNew(work);
        
        WorkflowAwareGroup valuationGroup =  WorkflowAwareGroup.create(
                user, 
                work, 
                bizProcess, 
                Set.of("S3.VALUATION","S4.UNDERWRITING"),
                Set.of("S3.VALUATION")
        );
        
        WorkflowAwareGroup underwritingGroup =  WorkflowAwareGroup.create(
                user, 
                work, 
                bizProcess, 
                Set.of("S4.UNDERWRITING"),
                Set.of("S4.UNDERWRITING")
        );
        
        WorkflowAwareGroup enableBeforeSubmissionGroup = WorkflowAwareGroup.createEnabledBeforeSubmission(work,user);
        
        
                
        MessageButton msgBtn = new MessageButton(
                work.getId(),
                context,
                user,
                msgService
        );

        FormLayout form = createForm(
                binder, 
                bizProcess, 
                user, 
                typicalGroup,
                valuationGroup,
                underwritingGroup
        );

        var workflowPanel = WorkflowPanel.create(
                work, user
        );

        var attachmentsPanel = AttachmentsPanel.create(
                work.getId(),
                "SME_FIN",
                Long.toString(work.getId()),
                typicalGroup,
                a -> {},
                a -> {});

        this.add(msgBtn);
        this.add(form);
        this.add(workflowPanel);
        this.add(attachmentsPanel);

        VerticalLayout applicantPanel = createApplicantPanel(
                work,
                user,
                binder,
                typicalGroup,
                valuationGroup,
                underwritingGroup
        );

        this.add(applicantPanel);

        configureButtons(
                binder,
                startEvent,
                user,
                workflowPanel,
                typicalGroup,
                enableBeforeSubmissionGroup,
                ifNewGroup,
                onPostSave,
                onPostRemove,
                onPostCancel);
    }

    private FormLayout createForm(
            Binder<FinApplication> binder,
            BizProcess bizProcess,
            OidcUser user,
            WorkflowAwareGroup typicalGroup,
            WorkflowAwareGroup valuationGroup,
            WorkflowAwareGroup underwritingGroup
    ) {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        var tfID = WorkflowAwareTextField.create("id", false, binder, new StringToUngroupLongConverter("Not a number"), typicalGroup);
        form.add(tfID);

        TextField tfName = WorkflowAwareTextField.create("name", true, binder, typicalGroup);
        form.add(tfName);

        TextArea tfAddress = WorkflowAwareTextArea.create("address", binder, typicalGroup);
        form.add(tfAddress);

        TextField tfPostalCode = WorkflowAwareTextField.create("postalCode", true, binder, typicalGroup);
        form.add(tfPostalCode);
//        tfPostalCode.setWidthFull();
//        Div div = new Div(tfPostalCode);
//        form.add(div);

        ComboBox<String> cbState = WorkflowAwareComboBox.create("state", binder, Set.of(
                "Johor",
                "Kedah",
                "Kelantan",
                "Malacca",
                "Negeri Sembilan",
                "Pahang",
                "Penang",
                "Perak",
                "Perlis",
                "Sabah",
                "Sarawak",
                "Selangor",
                "Terengganu",
                "W. Persekutuan Kuala Lumpur",
                "W. Persekutuan Labuan",
                "W. Persekutuan Putrajaya"
        ), typicalGroup);
        //cbState.setItems();
        form.add(cbState);

        DateTimePicker dtpApplicationDate = WorkflowAwareDateTimePicker.create("applicationDate", binder, null);
        dtpApplicationDate.setReadOnly(true);
        form.add(dtpApplicationDate);//add non managed

        TextField tfBizRegNumber = WorkflowAwareTextField.create("ssmRegistrationNumber", true, binder, typicalGroup);
        form.add(tfBizRegNumber);

        MoneyField tfFinRequested = WorkflowAwareMoneyField.create("financingRequested", "MYR", binder, typicalGroup);
        form.add(tfFinRequested);

        TextArea taReasonForFinancing = WorkflowAwareTextArea.create("reasonForFinancing", binder, typicalGroup);
        form.add(taReasonForFinancing);
        
        TextArea taSiteVisitReport = WorkflowAwareTextArea.create("siteVisitReport", binder, valuationGroup);
        form.add(taSiteVisitReport);
        
        TextField tfBureauScore = WorkflowAwareTextField.create("bureauScore",false, binder, new StringToIntegerConverter("Not a number"), underwritingGroup);
        form.add(tfBureauScore );
        
        TextField tfBureauResult = WorkflowAwareTextField.create("bureauResult", false, binder,underwritingGroup);
        form.add(tfBureauResult);
        
        

        return form;
    }

    private VerticalLayout createApplicantPanel(
            FinApplication finapp,
            OidcUser user,
            Binder<FinApplication> binder,
            WorkflowAwareGroup typicalGroup,
            WorkflowAwareGroup valuationGroup,
            WorkflowAwareGroup underwritingGroup
    ) {
        Grid<Applicant> gridApplicants = createApplicantGrid(finapp, user, binder, typicalGroup);
       
        WorkflowAwareButton btnAddApplicant = createAddApplicantButton(finapp, user, typicalGroup, gridApplicants);

        VerticalLayout applicantPanel = new VerticalLayout();
        applicantPanel.setPadding(true);
        applicantPanel.add(btnAddApplicant);
        applicantPanel.add(gridApplicants);
        applicantPanel.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.BASE);
        return applicantPanel;
    }

    private Grid<Applicant> createApplicantGrid(
            FinApplication finapp,
            OidcUser user,
            Binder<FinApplication> binder,
            WorkflowAwareGroup group) {
        Grid<Applicant> gridApplicants = new Grid<>();
        gridApplicants.getStyle().set("max-width", "285px");
        gridApplicants.addComponentColumn(
                app -> createApplicantCard(app, finapp, user, gridApplicants, group)
        );
        gridApplicants.setAllRowsVisible(true);
        gridApplicants.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        gridApplicants.setItems(applicantService.getApplicantsByApplication(binder.getBean()));
        return gridApplicants;
    }

    private Card createApplicantCard(
            Applicant app,
            FinApplication finapp,
            OidcUser user,
            Grid<Applicant> gridApplicants,
            WorkflowAwareGroup group
    ) {
        Map<String, String> fieldNameDisplayNameMap = WorkflowUtils.getFieldNameDisplayNameMap(app.getClass());
        Card card = new Card(
                fieldNameDisplayNameMap.get("fullName")
                + ": "
                + app.getFullName() != null ? app.getFullName() : "");

        card.add(new NativeLabel(fieldNameDisplayNameMap.get("email") + ": " + (app.getEmail() != null ? app.getEmail() : "")));
        card.add(new NativeLabel(fieldNameDisplayNameMap.get("type") + ": " + (app.getType() != null ? app.getType().getText() : "")));
        card.add(new NativeLabel(fieldNameDisplayNameMap.get("phoneNumber") + ": " + (app.getPhoneNumber() != null ? app.getPhoneNumber() : "")));

        Button btnSeeMore = new Button("See more", e -> buildApplicantDialog(app, finapp, user, gridApplicants));
        WorkflowAwareButton btnRemove = WorkflowAwareButton.create(group);
        btnRemove.setText("Remove");
        btnRemove.addClickListener( e -> createRemoveApplicantDialog(app, applicantService, gridApplicants).open());

        HorizontalLayout buttonPanel = new HorizontalLayout(btnSeeMore, btnRemove);
        card.add(buttonPanel);
        return card;
    }

    private ConfirmDialog createRemoveApplicantDialog(
            Applicant app,
            ApplicantService applicantService,
            Grid<Applicant> gridApplicants
    ) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Remove applicant");
        dialog.setText("Are you sure to remove this applicant?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Remove");
        dialog.addConfirmListener(event -> {
            applicantService.remove(app);
            gridApplicants.getDataProvider().refreshAll();

            dialog.close();
        });
        return dialog;
    }

    private WorkflowAwareButton createAddApplicantButton(
            FinApplication finapp,
            OidcUser user,
            WorkflowAwareGroup typicalWorkflowGroup,
            Grid<Applicant> gridApplicants
    ) {
        WorkflowAwareButton btnAddApplicant = WorkflowAwareButton.create(typicalWorkflowGroup);
        btnAddApplicant.setText(ADD_APPLICANT);
        btnAddApplicant.addClickListener(
                e -> buildApplicantDialog(
                        null,
                        finapp,
                        user,
                        gridApplicants)
        );
        btnAddApplicant.setId("btnAddApplicant");
        return btnAddApplicant;
    }

    private void configureButtons(
            Binder<FinApplication> binder,
            StartEvent startEvent,
            OidcUser user,
            WorkflowPanel workflowPanel,
            WorkflowAwareGroup typicalWorkflowGroup,
            WorkflowAwareGroup enabledBeforeSubmissionGroup,
            WorkflowAwareGroup enabledIfNew,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel) {

        Button btnSaveAndSubmitApp = createSaveAndSubmitButton(
                binder,
                user,
                workflowPanel,
                enabledBeforeSubmissionGroup,
                onPostSave
        );
        btnSaveAndSubmitApp.setId("btnSaveAndSubmitApp");

        Button btnSaveDraft = createSaveDraftButton(
                binder,
                enabledBeforeSubmissionGroup,
                onPostSave
        );
        btnSaveDraft.setId("btnSaveDraft");

        //Button btnSave = createSaveButton(binder, finappService, applicantService, onPostSave);
        Button btnCancel = createCancelButton(
                binder, 
                finappService, 
                onPostCancel
        );
        btnCancel.setId("btnCancel");
        
        Button btnRemove = createRemoveButton(
                binder, 
                finappService, 
                enabledIfNew,
                onPostRemove
        );
        btnRemove.setId("btnRemove");

        HorizontalLayout buttonLayout = new HorizontalLayout(
                btnSaveAndSubmitApp,
                btnSaveDraft,
                btnCancel,
                btnRemove
        );
        buttonLayout.setSpacing(true);
        this.getFooter().add(buttonLayout);

    }

    private Button createSaveDraftButton(
            Binder<FinApplication> binder,
            WorkflowAwareGroup group,
            Consumer<FinApplication> onPostSave
    ) {
        WorkflowAwareButton btnSaveDraft = WorkflowAwareButton.create(group);
        btnSaveDraft.setText("Save draft");
        btnSaveDraft.addClickListener(e1 -> {
            FinApplication finapp = binder.getBean();
            if (finapp.getStatus() == Status.NEWLY_CREATED) {
                finapp.setStatus(Status.DRAFT);
            }
            finappService.save(finapp);
            onPostSave.accept(finapp);
            this.close();
        });
        return btnSaveDraft;
    }

    private Button createSaveAndSubmitButton(
            Binder<FinApplication> binder,
            OidcUser user,
            WorkflowPanel workflowPanel,
            WorkflowAwareGroup group,
            Consumer<FinApplication> onPostSave
    ) {
        WorkflowAwareButton btnSaveFinApp = WorkflowAwareButton.create(group);
        btnSaveFinApp.setText("Save and submit");
        btnSaveFinApp.addClickListener(e1 -> {
            Set<String> errors = validateApplication(applicantService, workflowPanel, binder);
            if (errors.isEmpty()) {
                FinApplication finapp = binder.getBean();
                if (finapp.getStatus() == Status.NEWLY_CREATED) {
                    finapp.setStatus(Status.DRAFT);
                }
                Optional<Approval> oapproval = finapp.getApprovals().stream().filter(a -> StringUtils.equals(user.getPreferredUsername(), a.getUsername())).findAny();
                oapproval.ifPresent(approval -> {
                    approval.setApprovalDateTime(LocalDateTime.now());
                    approval.setApproved(workflowPanel.getApproval());
                    approval.setNote(workflowPanel.getApprovalNote());
                });
                finappService.run(finapp,
                        user.getPreferredUsername(),
                        workflowConfig.rootBizProcess(),
                        false
                );
                onPostSave.accept(finapp);
                this.close();
            } else {
                for (String err : errors) {
                    Notification.show(err);
                }
            }
        });
        return btnSaveFinApp;
    }

    private Button createCancelButton(
            Binder<FinApplication> binder,
            FinApplicationService finappService,
            Consumer<FinApplication> onPostCancel
    ) {
        WorkflowAwareButton btnCancel = WorkflowAwareButton.create(); //always allow cancel
        btnCancel.setText("Cancel");
        btnCancel.addClickListener(e1 -> {
            FinApplication work = binder.getBean();
            if (work == null) {
                finappService.remove(work);
            } else if (work.getStatus() == Status.NEWLY_CREATED) {
                finappService.remove(work);
            }
            onPostCancel.accept(work);
            this.close();
        });

        return btnCancel;
    }

    private Button createRemoveButton(
            Binder<FinApplication> binder,
            FinApplicationService finappService,
            WorkflowAwareGroup group,
            Consumer<FinApplication> onPostRemove
    ) {
        WorkflowAwareButton btnRemove = WorkflowAwareButton.create(group);
        btnRemove.setText("Remove");
        btnRemove.addClickListener(e1 -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Remove this application");
            dialog.setText("Are you sure to remove this applicant?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Remove");
            dialog.addConfirmListener(event -> {
                FinApplication finapp = binder.getBean();
                finappService.remove(finapp);
                onPostRemove.accept(binder.getBean());
                dialog.close();
                this.close();
            });
            dialog.open();
        });

        return btnRemove;
    }

    private Set<String> validateApplication(
            ApplicantService applicantService,
            WorkflowPanel workflowPanel,
            Binder<FinApplication> binder) {
        Set<String> errors = new HashSet<>();
        binder.validate().getValidationErrors().stream().forEach(e -> errors.add(e.getErrorMessage()));

        FinApplication finapp = binder.getBean();

        if (applicantService.countApplicants(finapp) <= 0) {
            errors.add("No applicant");
        }
        if (workflowPanel.validate() == false) {
            errors.add("Approval not set");
        }
        applicantService.getApplicants(finapp).forEach(applicant -> {
            for (String err : applicant.getErrors()) {
                errors.add("[" + applicant.getFullName() + "] " + err);
            }
        });
        return errors;
    }

    private void buildApplicantDialog(
            Applicant applicant,
            FinApplication finApplication,
            OidcUser user,
            //Editable editable,
            Grid<Applicant> gridApplicants
    ) {
        var applicantForm = ApplicantForm.create(
                applicant,
                finApplication,
                user,
                a -> gridApplicants.getDataProvider().refreshAll());
        applicantForm.open();
    }

}
