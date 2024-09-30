package com.azrul.smefinancing.views.application;

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
//import com.azrul.chenook.service.WorkflowService;
//import com.azrul.chenook.value.WorkflowMemento;
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
import com.azrul.chenook.views.common.converter.StringToUngroupLongConverter;
import com.azrul.chenook.views.common.components.WorkflowAwareComboBox;
import com.azrul.chenook.views.common.components.WorkflowAwareDateTimePicker;
import com.azrul.chenook.views.common.components.WorkflowAwareForm;
import com.azrul.chenook.views.common.components.WorkflowAwareGroup;
import com.azrul.chenook.views.common.components.WorkflowAwareMoneyField;
import com.azrul.chenook.views.common.components.WorkflowAwareTextArea;
import com.azrul.chenook.views.common.components.WorkflowAwareTextField;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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

@SpringComponent
public class ApplicationForm extends Dialog {

    private static final String ADD_APPLICANT = "Add applicant";
    private final FinApplicationService finappService;
    private final ApplicantService applicantService;
    private final ApplicantForm applicantForm;
    private final MessageService msgService;
    private final BadgeUtils badgeUtils;
    private final String DATETIME_FORMAT;
    private final WorkflowConfig workflowConfig;
    private final ApprovalService approvalService;
    private final BizUserService bizUserService;
    private final MapperService basicMapper;
    private final WorkflowPanel workflowPanel;
    private final AttachmentsPanel<FinApplication> attachmentsPanel;


    public ApplicationForm(
            @Autowired FinApplicationService finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired BadgeUtils badgeUtils,
            @Autowired WorkflowConfig workflowConfig,
            @Autowired ApprovalService approvalService,
            @Autowired BizUserService bizUserService,
            @Autowired MapperService basicMapper,
            @Autowired AttachmentsPanel<FinApplication> attachmentsPanel,
            @Autowired WorkflowPanel workflowPanel,
            @Autowired ApplicantForm applicantForm,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ){
        this.finappService = finappService;
        this.applicantService = applicantService;
        this.msgService = msgService;
        this.badgeUtils = badgeUtils;
        this.DATETIME_FORMAT = dateTimeFormat;
        this.workflowConfig = workflowConfig;
        this.approvalService=approvalService;
        this.bizUserService=bizUserService;
        this.basicMapper = basicMapper;
        this.workflowPanel = workflowPanel;
        this.attachmentsPanel = attachmentsPanel;
        this.applicantForm =applicantForm;
    }
    
    public void init(
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
        
        
         MessageButton msgBtn = new MessageButton(
                work.getId(),
                context,
                user,
                msgService
        );
        
        
        WorkflowAwareForm form = createForm(binder,bizProcess,user );
        
        
        workflowPanel.init(
                work,
                user
        );

        attachmentsPanel.init(
                work.getId(),
                "SME_FIN",
                Long.toString(work.getId()),
                a -> {},
                a -> {});
        
        
        form.add(msgBtn);
        form.addManagedComponent(workflowPanel);
        form.addManagedComponent(attachmentsPanel, 2);
        
        this.add(form);
        
        VerticalLayout applicantPanel = createApplicantPanel(
                work,
                user,
                bizProcess,
                binder
        );
         

        this.add(applicantPanel);

        configureButtons(binder,
                startEvent,
                user,
                onPostSave,
                onPostRemove,
                onPostCancel);
    }

//    private Editable isEditable(
//            FinApplication work,
//            ApplicantService applicantService,
//            OidcUser oidcUser
//    ) {
//        Set<String> applicantsEmail = applicantService.getApplicantsEmail(work);
//        
//        int state = 0;
//        if (oidcUser.getAuthorities().stream().anyMatch(sga -> {
//            return StringUtils.equals(sga.getAuthority(), "ROLE_FINAPP_ADMIN");
//        })) { // admin
//            state = 1;
//        } else if (applicantsEmail.contains(oidcUser.getEmail())) { // applicant
//            state = 2;
//        } else if (work.getOwners().stream().filter(o->StringUtils.equals(o.getUsername(),oidcUser.getPreferredUsername())).count()>0) {
//            state = 3;
//        } else {
//            state = 4;
//        }
//
//        if (work == null) {
//            state = 5;
//        } else {
//
//            if (state == 2) {
//                if (work.getStatus() == Status.DRAFT
//                        || work.getStatus() == Status.NEED_MORE_INFO
//                        || work.getStatus() == Status.NEWLY_CREATED) {
//                    state = 6;
//                } else {
//                    state = 8;
//                }
//            } else if (state == 3) {
//                if (work.getStatus() == Status.DRAFT
//                        || work.getStatus() == Status.NEED_MORE_INFO
//                        || work.getStatus() == Status.NEWLY_CREATED) {
//                    state = 5;
//                } else {
//                    state = 7;
//                }
//            }
//        }
//
//        switch (state) {
//            case 4:
//                return Editable.NO_DUE_TO_USER;
//            case 7:
//            case 8:
//                return Editable.NO_DUE_TO_STATUS;
//            case 6:
//                return Editable.YES_AS_APPLICANT;
//            case 5:
//                return Editable.YES;
//            case 1:
//                return Editable.YES_AS_ADMIN;
//            default:
//                return Editable.NO_DUE_TO_USER;
//        }
//
//    }

    private WorkflowAwareForm createForm(
            Binder<FinApplication> binder,
            BizProcess bizProcess,
            OidcUser user
    ) {
        WorkflowAwareForm form = WorkflowAwareForm.create(user,binder.getBean(), bizProcess);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );


        var tfID = WorkflowAwareTextField.create("id", false, binder, new StringToUngroupLongConverter("Not a number"));
        form.addManagedComponent(tfID);

        TextField tfName = WorkflowAwareTextField.create("name", true, binder);
        form.addManagedComponent(tfName);

        TextArea tfAddress = WorkflowAwareTextArea.create("address", binder);
        form.addManagedComponent(tfAddress);

        TextField tfPostalCode = WorkflowAwareTextField.create("postalCode", true, binder);
        form.addManagedComponent(tfPostalCode);
//        tfPostalCode.setWidthFull();
//        Div div = new Div(tfPostalCode);
//        form.add(div);

        ComboBox<String> cbState = WorkflowAwareComboBox.create("state", binder,Set.of(
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
        ));
        //cbState.setItems();
        form.addManagedComponent(cbState);

        DateTimePicker dtpApplicationDate = WorkflowAwareDateTimePicker.create("applicationDate", binder);
        form.addManagedComponent(dtpApplicationDate);

        TextField tfBizRegNumber = WorkflowAwareTextField.create("ssmRegistrationNumber", true, binder);
        form.addManagedComponent(tfBizRegNumber);

        MoneyField tfFinRequested = WorkflowAwareMoneyField.create("financingRequested", "MYR", binder);
        form.addManagedComponent(tfFinRequested);

        TextArea taReasonForFinancing = WorkflowAwareTextArea.create("reasonForFinancing", binder);
        form.addManagedComponent(taReasonForFinancing);
        
        return form;
    }

    private VerticalLayout createApplicantPanel(
            FinApplication finapp,
            OidcUser user,
            BizProcess bizProcess,
            Binder<FinApplication> binder
    ) {
        Grid<Applicant> gridApplicants = createApplicantGrid(finapp, user,binder);
       // gridApplicants.getStyle().set("max-width", "285px");
        Button btnAddApplicant = createAddApplicantButton(finapp,  user, gridApplicants);

       WorkflowAwareGroup group = WorkflowAwareGroup.create(user, binder.getBean(), bizProcess);
       group.addManagedComponents(btnAddApplicant); //
       group.add(gridApplicants); //not managed. Will always show
       group.setWidthFull();
       VerticalLayout applicantPanel = new VerticalLayout();
       applicantPanel.add(group);
       applicantPanel.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.BASE);
       return applicantPanel;
    }

    private Grid<Applicant> createApplicantGrid(
            FinApplication finapp,
            OidcUser user,
            //Editable editable,
            Binder<FinApplication> binder) {
        Grid<Applicant> gridApplicants = new Grid<>();
        gridApplicants.getStyle().set("max-width", "285px");
        gridApplicants.addComponentColumn(
                app -> createApplicantCard(app, finapp, user, gridApplicants)
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
            Grid<Applicant> gridApplicants
    ) {
        Map<String,String> fieldNameDisplayNameMap = WorkflowUtils.getFieldNameDisplayNameMap(app.getClass());
        Card card = new Card(
                fieldNameDisplayNameMap.get("fullName")+
                ": " + 
                app.getFullName()!=null?app.getFullName():"");
        
        card.add(new NativeLabel(fieldNameDisplayNameMap.get("email")+": "+(app.getEmail()!=null?app.getEmail():"")));
        card.add(new NativeLabel(fieldNameDisplayNameMap.get("type")+": "+(app.getType()!=null?app.getType().getText():"")));
        card.add(new NativeLabel(fieldNameDisplayNameMap.get("phoneNumber")+": "+(app.getPhoneNumber()!=null?app.getPhoneNumber():"")));

        Button btnSeeMore = new Button("See more", e -> buildApplicantDialog(app, finapp, user,gridApplicants));
        Button btnRemove = new Button("Remove", e -> createRemoveApplicantDialog(app, applicantService, gridApplicants).open());

        HorizontalLayout buttonPanel = new HorizontalLayout(btnSeeMore, btnRemove);
        card.add(buttonPanel);
//
//        if (editable != Editable.YES) {
//            btnRemove.setEnabled(false);
//        }

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

    private Button createAddApplicantButton(
            FinApplication finapp,
            OidcUser user,
            Grid<Applicant> gridApplicants
    ) {
        Button btnAddApplicant =  new Button(
                ADD_APPLICANT,
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
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel) {

        Button btnSaveAndSubmitApp = createSaveAndSubmitButton(
                binder,
                user,
                onPostSave
        );
        btnSaveAndSubmitApp.setId("btnSaveAndSubmitApp");

        Button btnSaveDraft = createSaveDraftButton(
                binder,
                onPostSave
        );
        btnSaveDraft.setId("btnSaveDraft");

        //Button btnSave = createSaveButton(binder, finappService, applicantService, onPostSave);
        Button btnCancel = createCancelButton(binder, finappService, onPostCancel);
        btnCancel.setId("btnCancel");
        Button btnRemove = createRemoveButton(binder, /*editable,*/ finappService, onPostRemove);
        btnRemove.setId("btnRemove");

        HorizontalLayout buttonLayout = new HorizontalLayout(
                btnSaveAndSubmitApp,
                btnSaveDraft,
                btnCancel,
                btnRemove
        );
        buttonLayout.setSpacing(true);
        this.getFooter().add(buttonLayout);

        btnSaveAndSubmitApp.setEnabled(true);
        btnSaveDraft.setEnabled(false);
        btnRemove.setEnabled(false);
        btnCancel.setEnabled(false);
        //btnSave.setEnabled(false);

//        if (editable == Editable.YES) {
//            //btnSaveAndSubmitApp.setEnabled(true);
//            btnSaveDraft.setEnabled(true);
//            btnRemove.setEnabled(true);
//            btnCancel.setEnabled(true);
//        } else if (editable == Editable.YES_AS_APPLICANT) {
//            btnSaveDraft.setEnabled(true);
//            btnCancel.setEnabled(true);
//            //btnSaveAndSubmitApp.setEnabled(true);
//            btnCancel.setEnabled(true);
//        } else {
//            btnCancel.setEnabled(true);
//        }
    }

    private Button createSaveDraftButton(
            Binder<FinApplication> binder,
            Consumer<FinApplication> onPostSave
    ) {
        Button btnSaveDraft = new Button("Save draft", e1 -> {
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
            Consumer<FinApplication> onPostSave
    ) {
        Button btnSaveFinApp = new Button("Save and submit", e1 -> {
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
        return new Button("Cancel", e1 -> {

            FinApplication work = binder.getBean();
            if (work == null) {
                finappService.remove(work);
            } else if (work.getStatus() == Status.NEWLY_CREATED) {
                finappService.remove(work);
            }
            onPostCancel.accept(work);
            this.close();
        });
    }

    private Button createRemoveButton(
            Binder<FinApplication> binder,
            //Editable editable,
            FinApplicationService finappService,
            Consumer<FinApplication> onPostRemove
    ) {
        Button btnRemove = new Button("Remove", e1 -> {
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
//        if (editable != Editable.YES) {
//            btnRemove.setEnabled(false);
//        }

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
        applicantForm.init(
                applicant,
                finApplication,
                user,
                a -> gridApplicants.getDataProvider().refreshAll());
        applicantForm.open();
    }

}
