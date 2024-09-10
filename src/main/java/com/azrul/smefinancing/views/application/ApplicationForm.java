package com.azrul.smefinancing.views.application;

import com.azrul.chenook.domain.Status;
import com.azrul.chenook.service.MessageService;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.common.Card;
import com.azrul.chenook.views.message.MessageButton;
import com.azrul.chenook.views.workflow.WorkflowPanel;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
//import com.azrul.chenook.service.WorkflowService;
//import com.azrul.chenook.value.WorkflowMemento;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.applicant.ApplicantForm;
import com.azrul.smefinancing.service.BadgeUtils;
import com.azrul.smefinancing.views.common.Editable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class ApplicationForm extends Dialog {

    private static final String PREFIX_MYR = "MYR ";
    private static final String ADD_APPLICANT = "Add applicant";
    private static final String APPLICATION_ID_LABEL = "Application ID (AA Number)";
    private static final String BUSINESS_NAME_LABEL = "Business Name";
    private static final String ADDRESS_LABEL = "Address";
    private static final String POSTAL_CODE_LABEL = "Postal code";
    private static final String STATE_LABEL = "State";
    private static final String APPLICATION_DATE_LABEL = "Application date";
    private static final String SSM_REGISTRATION_LABEL = "SSM Registration";
    //private static final String STATUS_LABEL = "Status";
    private static final String FINANCING_APPLIED_LABEL = "Financing Applied";
    private static final String REASON_FOR_FINANCING_LABEL = "Reason for financing";
    private String DATETIME_FORMAT;
    private DateTimeFormatter formatter;

    public ApplicationForm(
            StartEvent startEvent,
            FinApplication work,
            OidcUser oidcUser,
            String dateTimeFormat,
            String context,
            ApplicantService applicantService,
            FinApplicationService finappService,
            MessageService msgService,
            BadgeUtils badgeUtils,
            WorkflowConfig workflowConfig,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        if (work == null) {
            return; // if finapp is null, this will not work
        }
        this.DATETIME_FORMAT = dateTimeFormat;
        this.formatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);
        BizProcess bizProcess = workflowConfig.rootBizProcess();
        Editable editable = isEditable(
                work,
                applicantService,
                oidcUser
        );

        Binder<FinApplication> binder = new Binder<>(FinApplication.class);
        binder.setBean(work);

        FormLayout form = createForm(binder, badgeUtils, editable);
        MessageButton msgBtn = new MessageButton(
                work.getId(),
                context,
                oidcUser,
                msgService
        );
        this.add(msgBtn);
        this.add(form);

        WorkflowPanel workflowPanel = new WorkflowPanel(
                work,
                false,
                a -> {
                },
                a -> {
                }
        );

        this.add(workflowPanel);

        VerticalLayout applicantPanel = createApplicantPanel(
                work,
                applicantService,
                oidcUser,
                editable,
                binder
        );

        this.add(applicantPanel);

        AttachmentsPanel attachmentsPanel = new AttachmentsPanel(
                work.getId(),
                "SME_FIN",
                Long.toString(work.getId()),
                editable == Editable.YES,
                a -> {
                },
                a -> {
                });
        this.add(attachmentsPanel);

        configureButtons(
                binder,
                startEvent,
                oidcUser,
                bizProcess,
                //work,
                editable,
                finappService,
                applicantService,
                //workflowService,
                onPostSave,
                onPostRemove,
                onPostCancel);
    }

    private Editable isEditable(
            FinApplication work,
            ApplicantService applicantService,
            OidcUser oidcUser
    ) {
        Set<String> applicantsEmail = applicantService.getApplicantsEmail(work);

        int state = 0;
        if (oidcUser.getAuthorities().stream().anyMatch(sga -> StringUtils.equals(sga.getAuthority(), "ROLE_FINAPP_ADMIN"))) { // admin
            state = 1;
        } else if (applicantsEmail.contains(oidcUser.getEmail())) { // applicant
            state = 2;
        } else if (StringUtils.equals(oidcUser.getPreferredUsername(), work.getUsername())) {
            state = 3;
        } else {
            state = 4;
        }

        if (work == null) {
            state = 5;
        } else {

            if (state == 2) {
                if (work.getStatus() == Status.DRAFT
                        || work.getStatus() == Status.NEED_MORE_INFO
                        || work.getStatus() == Status.NEWLY_CREATED) {
                    state = 6;
                } else {
                    state = 8;
                }
            } else if (state == 3) {
                if (work.getStatus() == Status.DRAFT
                        || work.getStatus() == Status.NEED_MORE_INFO
                        || work.getStatus() == Status.NEWLY_CREATED) {
                    state = 5;
                } else {
                    state = 7;
                }
            }
        }

        switch (state) {
            case 4:
                return Editable.NO_DUE_TO_USER;
            case 7:
            case 8:
                return Editable.NO_DUE_TO_STATUS;
            case 6:
                return Editable.YES_AS_APPLICANT;
            case 5:
                return Editable.YES;
            case 1:
                return Editable.YES_AS_ADMIN;
            default:
                return Editable.NO_DUE_TO_USER;
        }

    }

    private FormLayout createForm(
            Binder<FinApplication> binder,
            BadgeUtils badgeUtils,
            Editable editable
    ) {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Boolean enabled = (editable == Editable.YES);
        Boolean enabledForStatus = (editable == Editable.YES_AS_ADMIN);

        TextField tfID = createTextField(APPLICATION_ID_LABEL, enabled, false);
        binder.forField(tfID).bindReadOnly(fa -> fa.getId().toString());
        form.add(tfID);

        TextField tfName = createTextField(BUSINESS_NAME_LABEL, enabled, true);
        binder.forField(tfName).asRequired().bind(FinApplication::getName, FinApplication::setName);
        form.add(tfName);

        TextArea tfAddress = createTextArea(ADDRESS_LABEL, enabled);
        binder.bind(tfAddress, FinApplication::getAddress, FinApplication::setAddress);
        form.add(tfAddress);

        TextField tfPostalCode = createTextField(POSTAL_CODE_LABEL, enabled, true);
        binder.bind(tfPostalCode, FinApplication::getPostalCode, FinApplication::setPostalCode);
        form.add(tfPostalCode);

        ComboBox<String> cbState = createComboBox(STATE_LABEL, enabled);
        cbState.setItems(List.of(
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
        binder.bind(cbState, FinApplication::getState, FinApplication::setState);
        form.add(cbState);

        DateTimePicker dtpApplicationDate = createDateTimePicker(APPLICATION_DATE_LABEL, false);
        binder.bind(dtpApplicationDate, FinApplication::getApplicationDate, FinApplication::setApplicationDate);
        form.add(dtpApplicationDate);

        TextField tfBizRegNumber = createTextField(SSM_REGISTRATION_LABEL, enabled, true);
        binder.bind(tfBizRegNumber, FinApplication::getSsmRegistrationNumber, FinApplication::setSsmRegistrationNumber);
        form.add(tfBizRegNumber);

        BigDecimalField tfFinRequested = createBigDecimalField(FINANCING_APPLIED_LABEL, enabled);
        binder.bind(tfFinRequested, FinApplication::getFinancingRequested, FinApplication::setFinancingRequested);
        form.add(tfFinRequested);

        TextArea taReasonForFinancing = createTextArea(REASON_FOR_FINANCING_LABEL, enabled);
        binder.bind(taReasonForFinancing, FinApplication::getReasonForFinancing, FinApplication::setReasonForFinancing);
        form.add(taReasonForFinancing);

        return form;
    }

    private TextField createTextField(String label, boolean editable, boolean required) {
        TextField textField = new TextField(label);
        textField.setReadOnly(!editable);
        if (required) {
            textField.setRequiredIndicatorVisible(true);
        }
        return textField;
    }

    private TextArea createTextArea(String label, boolean editable) {
        TextArea textArea = new TextArea(label);
        textArea.setMaxLength(255);
        textArea.setReadOnly(!editable);
        return textArea;
    }

    private <T> ComboBox<T> createComboBox(String label, boolean editable) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setReadOnly(!editable);
        return comboBox;
    }

    private <T> Select<T> createSelect(String label, boolean editable) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setReadOnly(!editable);
        return select;
    }

    private DateTimePicker createDateTimePicker(String label, boolean editable) {
        DateTimePicker dateTimePicker = new DateTimePicker(label);
        dateTimePicker.setReadOnly(!editable);
        return dateTimePicker;
    }

    private BigDecimalField createBigDecimalField(String label, boolean editable) {
        BigDecimalField bigDecimalField = new BigDecimalField(label);
        bigDecimalField.setPrefixComponent(new NativeLabel(PREFIX_MYR));
        bigDecimalField.setReadOnly(!editable);
        return bigDecimalField;
    }

    private VerticalLayout createApplicantPanel(
            FinApplication finapp,
            ApplicantService applicantService,
            OidcUser user,
            Editable editable,
            Binder<FinApplication> binder
    ) {
        Grid<Applicant> gridApplicants = createApplicantGrid(finapp, applicantService, user, editable, binder);
        gridApplicants.getStyle().set("max-width", "285px");
        Button btnAddApplicant = createAddApplicantButton(finapp, applicantService, user, editable, gridApplicants);

        VerticalLayout applicantPanel = new VerticalLayout();
        applicantPanel.add(btnAddApplicant, gridApplicants);
        applicantPanel.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.BASE);

        if (editable != Editable.YES) {
            btnAddApplicant.setEnabled(false);
        }

        return applicantPanel;
    }

    private Grid<Applicant> createApplicantGrid(
            FinApplication finapp,
            ApplicantService applicantService,
            OidcUser user,
            Editable editable,
            Binder<FinApplication> binder) {
        Grid<Applicant> gridApplicants = new Grid<>();
        gridApplicants.getStyle().set("max-width", "285px");
        gridApplicants.addComponentColumn(app -> createApplicantCard(app, finapp, applicantService, user, editable, gridApplicants));
        gridApplicants.setAllRowsVisible(true);
        gridApplicants.setItems(applicantService.getApplicantsByApplication(binder.getBean()));
        return gridApplicants;
    }

    private Card createApplicantCard(
            Applicant app,
            FinApplication finapp,
            ApplicantService applicantService,
            OidcUser user,
            Editable editable,
            Grid<Applicant> gridApplicants
    ) {
        Card card = new Card("Name: " + app.getFullName());
        card.add(new NativeLabel("Email: " + app.getEmail()));
        card.add(new NativeLabel("Position: " + app.getPosition()));
        card.add(new NativeLabel("Phone: " + app.getPhoneNumber()));

        Button btnSeeMore = new Button("See more", e -> buildApplicantDialog(app, finapp, applicantService, user, editable, gridApplicants));
        Button btnRemove = new Button("Remove", e -> createRemoveApplicantDialog(app, applicantService, gridApplicants).open());

        HorizontalLayout buttonPanel = new HorizontalLayout(btnSeeMore, btnRemove);
        card.add(buttonPanel);

        if (editable != Editable.YES) {
            btnRemove.setEnabled(false);
        }

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
            ApplicantService applicantService,
            OidcUser user,
            Editable editable,
            Grid<Applicant> gridApplicants
    ) {
        return new Button(
                ADD_APPLICANT,
                e -> buildApplicantDialog(
                        null,
                        finapp,
                        applicantService,
                        user,
                        editable,
                        gridApplicants)
        );
    }

    private void configureButtons(
            Binder<FinApplication> binder,
            StartEvent startEvent,
            OidcUser oidcUser,
            BizProcess bizProcess,
            Editable editable,
            FinApplicationService finappService,
            ApplicantService applicantService,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel) {

        Button btnSaveAndSubmitApp = createSaveAndSubmitButton(
                binder,
                startEvent,
                oidcUser,
                bizProcess,
                finappService,
                applicantService,
                onPostSave
        );

        Button btnSaveDraft = createSaveDraftButton(
                binder,
                startEvent,
                oidcUser,
                bizProcess,
                finappService,
                applicantService,
                onPostSave
        );

        //Button btnSave = createSaveButton(binder, finappService, applicantService, onPostSave);
        Button btnCancel = createCancelButton(binder, finappService, onPostCancel);
        Button btnRemove = createRemoveButton(binder, editable, finappService, onPostRemove);

        HorizontalLayout buttonLayout = new HorizontalLayout(
                btnSaveAndSubmitApp,
                btnSaveDraft,
                btnCancel,
                btnRemove
        );
        buttonLayout.setSpacing(true);
        this.getFooter().add(buttonLayout);

        btnSaveAndSubmitApp.setEnabled(false);
        btnSaveDraft.setEnabled(false);
        btnRemove.setEnabled(false);
        btnCancel.setEnabled(false);
        //btnSave.setEnabled(false);

        if (editable == Editable.YES) {
            btnSaveAndSubmitApp.setEnabled(true);
            btnSaveDraft.setEnabled(true);
            btnRemove.setEnabled(true);
            btnCancel.setEnabled(true);
        } else if (editable == Editable.YES_AS_APPLICANT) {
            btnSaveDraft.setEnabled(true);
            btnCancel.setEnabled(true);
        } else if (editable == Editable.YES_AS_ADMIN) {
            //btnSave.setEnabled(true);
            btnSaveAndSubmitApp.setEnabled(true);
            btnCancel.setEnabled(true);
        } else {
            btnCancel.setEnabled(true);
        }
    }

    private Button createSaveDraftButton(
            Binder<FinApplication> binder,
            StartEvent startEvent,
            OidcUser oidcUser,
            BizProcess bizProcess, 
            FinApplicationService finappService,
            ApplicantService applicantService,
            Consumer<FinApplication> onPostSave
    ) {
        Button btnSaveDraft = new Button("Save draft", e1 -> {
            FinApplication finapp = binder.getBean();
            finapp.setStatus(Status.DRAFT);
            finappService.save(finapp, oidcUser.getPreferredUsername());
           
//            String requestedAmount = finapp.getFinancingRequested()!=null
//                            ?
//                                NumberFormat.getCurrencyInstance().format(finapp.getFinancingRequested())
//                            :
//                                "";
//            final Map<String, String> fields = Map.<String, String>of(
//                    "TITLE", "SME Loan: "+ requestedAmount ,
//                    "APPLICATION_DATE", finapp.getApplicationDate().format(formatter),
//                    "REASON_FOR_FINANCING", finapp.getReasonForFinancing()
//            );
//            if (work == null) {
//                FinApplication f = new FinApplication();
//                WorkItem w = finappService.create(f,memento, startEvent.getId(), fields);
//                w.setFields(fields);
//                w.setStatus(Status.DRAFT);
//                workflowService.save(w);
//            } else {
//                work.setFields(fields);
//                work.setStatus(Status.DRAFT);
//                workflowService.save(work);
//            }

            onPostSave.accept(finapp);
            this.close();
        });
        return btnSaveDraft;
    }

    private Button createSaveAndSubmitButton(
            Binder<FinApplication> binder,
            StartEvent startEvent,
            OidcUser oidcUser,
            BizProcess bizProcess, 
            FinApplicationService finappService,
            ApplicantService applicantService,
            Consumer<FinApplication> onPostSave
    ) {
        Button btnSaveFinApp = new Button("Save and submit", e1 -> {
            Set<String> errors = validateApplication(applicantService, binder);
            if (errors.isEmpty()) {
                FinApplication finapp = binder.getBean();
                finapp.setStatus(Status.DRAFT);
//                finapp = finappService.save(
//                        finapp,
//                        oidcUser.getPreferredUsername()
//                );
//                String requestedAmount = finapp.getFinancingRequested()!=null
//                            ?
//                                NumberFormat.getCurrencyInstance().format(finapp.getFinancingRequested())
//                            :
//                                "";
//                final Map<String, String> fields = Map.<String, String>of(
//                    "TITLE", "SME Loan: "+requestedAmount,
//                    "APPLICATION_DATE", finapp.getApplicationDate().format(formatter),
//                    "REASON_FOR_FINANCING", finapp.getReasonForFinancing()
//            );

                finappService.run(finapp,oidcUser.getPreferredUsername(),bizProcess, false);
                onPostSave.accept(finapp);
                this.close();
            } else {
                StringBuilder errMsg = new StringBuilder();
                for (String err : errors) {
                    errMsg.append(err);
                    errMsg.append("\n");
                }
                Notification.show(errMsg.toString());
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
            Editable editable,
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
        if (editable != Editable.YES) {
            btnRemove.setEnabled(false);
        }

        return btnRemove;
    }

    private Set<String> validateApplication(
            ApplicantService applicantService,
            Binder<FinApplication> binder) {
        Set<String> errors = new HashSet<>();
        binder.validate().getValidationErrors().stream().forEach(e -> errors.add(e.getErrorMessage()));

        FinApplication finapp = binder.getBean();

        if (applicantService.countApplicants(finapp) <= 0) {
            errors.add("No applicant");
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
            ApplicantService applicantService,
            OidcUser user,
            Editable editable,
            Grid<Applicant> gridApplicants
    ) {
        ApplicantForm appForm = new ApplicantForm(
                applicant,
                finApplication,
                editable,
                user,
                applicantService,
                a -> gridApplicants.getDataProvider().refreshAll());
        appForm.open();
    }

}
