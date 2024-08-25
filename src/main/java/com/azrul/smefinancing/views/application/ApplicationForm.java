package com.azrul.smefinancing.views.application;

import com.azrul.chenook.service.MessageService;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.smefinancing.domain.Status;
import com.azrul.chenook.views.common.Card;
import com.azrul.chenook.views.message.MessageButton;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String STATUS_LABEL = "Status";
    private static final String FINANCING_APPLIED_LABEL = "Financing Applied";
    private static final String REASON_FOR_FINANCING_LABEL = "Reason for financing";

    public ApplicationForm(
            FinApplication finapp,
            DefaultOidcUser user,
            ApplicantService applicantService,
            FinApplicationService finappService,
            MessageService msgService,
            BadgeUtils badgeUtils,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        if (finapp == null) {
            return; // if finapp is null, this will not work
        }

        Editable editable = isEditable(finapp, applicantService,user);
        Binder<FinApplication> binder = new Binder<>(FinApplication.class);
        binder.setBean(finapp);

        FormLayout form = createForm(binder, badgeUtils, editable);
        MessageButton msgBtn = new MessageButton(finapp.getId(), "SME_FIN", user, msgService);
        this.add(msgBtn);
        this.add(form);

        VerticalLayout applicantPanel = createApplicantPanel(
                finapp,
                applicantService,
                user,
                editable,
                binder
        );

        this.add(applicantPanel);

        AttachmentsPanel attachmentsPanel = new AttachmentsPanel(
                finapp.getId(),
                "SME_FIN",
                Long.toString(finapp.getId()),
                editable == Editable.YES,
                a -> {},
                a -> {});
        this.add(attachmentsPanel);

        configureButtons(
                binder, 
                user, 
                editable, 
                finappService,
                applicantService, 
                onPostSave, 
                onPostRemove, 
                onPostCancel);
    }

    private Editable isEditable(
            FinApplication finapp,
            ApplicantService applicantService, 
            OidcUser oidcUser
    ) {
        Set<String> applicantsEmail = applicantService.getApplicantsEmail(finapp);
        
        int state = 0;
        if (oidcUser.getAuthorities().stream().anyMatch(sga->StringUtils.equals(sga.getAuthority(),"ROLE_FINAPP_ADMIN"))){ // admin
            state = 1;
        }else if (applicantsEmail.contains(oidcUser.getEmail())){ // applicant
            state = 2;
        }else if (StringUtils.equals(oidcUser.getPreferredUsername(),finapp.getUsername())){
            state = 3;
        }else{
            state = 4;
        }
        
        if (state==2){
            if (finapp.getStatus() == Status.DRAFT
                || finapp.getStatus() == Status.NEED_MORE_INFO
                || finapp.getStatus() == Status.NEWLY_CREATED){
                state = 6;
            }else{
                state = 8;
            }
        }else if (state == 3){
            if (finapp.getStatus() == Status.DRAFT
                || finapp.getStatus() == Status.NEED_MORE_INFO
                || finapp.getStatus() == Status.NEWLY_CREATED){
                state = 5;
            }else{
                state = 7;
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
        
        Boolean enabled = (editable==Editable.YES);
        Boolean enabledForStatus = (editable==Editable.YES_AS_ADMIN);

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

        
        Select<Status> cbStatus = createSelect(STATUS_LABEL,enabledForStatus);
        cbStatus.setItems(Status.values());
        cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
        binder.bind(cbStatus, FinApplication::getStatus,FinApplication::setStatus);
        form.add(cbStatus);

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
        textArea.setReadOnly(!editable);
        return textArea;
    }

    private <T> ComboBox<T> createComboBox(String label, boolean editable) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setReadOnly(!editable);
        return comboBox;
    }
    
    private <T> Select<T> createSelect(String label, boolean editable) {
        Select<T> select= new Select<>();
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

    private VerticalLayout createApplicantPanel(FinApplication finapp, ApplicantService applicantService, DefaultOidcUser user, Editable editable, Binder<FinApplication> binder) {
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

    private Grid<Applicant> createApplicantGrid(FinApplication finapp, ApplicantService applicantService, DefaultOidcUser user, Editable editable, Binder<FinApplication> binder) {
        Grid<Applicant> gridApplicants = new Grid<>();
        gridApplicants.getStyle().set("max-width", "285px");
        gridApplicants.addComponentColumn(app -> createApplicantCard(app, finapp, applicantService, user, editable, gridApplicants));
        gridApplicants.setAllRowsVisible(true);
        gridApplicants.setItems(applicantService.getApplicantsByApplication(binder.getBean()));
        return gridApplicants;
    }

    private Card createApplicantCard(Applicant app, FinApplication finapp, ApplicantService applicantService, DefaultOidcUser user, Editable editable, Grid<Applicant> gridApplicants) {
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

    private ConfirmDialog createRemoveApplicantDialog(Applicant app, ApplicantService applicantService, Grid<Applicant> gridApplicants) {
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

    private Button createAddApplicantButton(FinApplication finapp, ApplicantService applicantService, DefaultOidcUser user, Editable editable, Grid<Applicant> gridApplicants) {
        return new Button(ADD_APPLICANT, e -> buildApplicantDialog(null, finapp, applicantService, user, editable, gridApplicants));
    }

    private void configureButtons(
            Binder<FinApplication> binder, 
            DefaultOidcUser user, 
            Editable editable, 
            FinApplicationService finappService,
            ApplicantService applicantService,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel) {
        
        Button btnSaveAndSubmitApp = createSaveAndSubmitButton(binder, finappService, applicantService, user, onPostSave);
        Button btnSaveDraft = createSaveDraftButton(binder, finappService, user, onPostSave);
        Button btnSave = createSaveButton(binder, finappService,applicantService, onPostSave);
        
        Button btnCancel = createCancelButton(binder, finappService, onPostCancel);
        Button btnRemove = createRemoveButton(binder, editable, finappService, onPostRemove);

        HorizontalLayout buttonLayout = new HorizontalLayout(
                btnSaveAndSubmitApp, 
                btnSaveDraft, 
                btnSave,
                btnCancel,
                btnRemove
        );
        buttonLayout.setSpacing(true);
        this.getFooter().add(buttonLayout);

        btnSaveAndSubmitApp.setEnabled(false);
        btnSaveDraft.setEnabled(false);
        btnRemove.setEnabled(false);
        btnCancel.setEnabled(false);
        btnSave.setEnabled(false);
        
        if (editable == Editable.YES) {
            btnSaveAndSubmitApp.setEnabled(true);
            btnSaveDraft.setEnabled(true);
            btnRemove.setEnabled(true);
            btnCancel.setEnabled(true);
        }else if (editable == Editable.YES_AS_APPLICANT){
            btnSaveDraft.setEnabled(true);
            btnCancel.setEnabled(true);
        }else if (editable == Editable.YES_AS_ADMIN) {
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
        }else{
            btnCancel.setEnabled(true);    
        }
    }

    private Button createSaveDraftButton(Binder<FinApplication> binder, FinApplicationService finappService, DefaultOidcUser user, Consumer<FinApplication> onPostSave) {
        Button btnSaveDraft = new Button("Save draft", e1 -> {
            FinApplication finapp = binder.getBean();
            finapp.setStatus(Status.DRAFT);
            finappService.save(finapp, user.getPreferredUsername());
            onPostSave.accept(finapp);
            this.close();
        });
        return btnSaveDraft;
    }

    private Button createSaveAndSubmitButton(
            Binder<FinApplication> binder, 
            FinApplicationService finappService,
            ApplicantService applicantService,
            DefaultOidcUser user, 
            Consumer<FinApplication> onPostSave) {
        Button btnSaveFinApp = new Button("Save and submit", e1 -> {
            Set<String> errors = validateApplication(applicantService, binder);
            if (errors.isEmpty()) {
                FinApplication finapp = binder.getBean();
                finapp.setStatus(Status.IN_PROGRESS);
                finappService.save(finapp, user.getPreferredUsername());
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
    
    private Button createSaveButton(
            Binder<FinApplication> binder, 
            FinApplicationService finappService,
            ApplicantService applicantService,
            Consumer<FinApplication> onPostSave) {
        Button btnSaveFinApp = new Button("Save", e1 -> {
            Set<String> errors = validateApplication(applicantService, binder);
            if (errors.isEmpty()) {
                FinApplication finapp = binder.getBean();
                finappService.save(finapp);
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

    private Button createCancelButton(Binder<FinApplication> binder, FinApplicationService finappService, Consumer<FinApplication> onPostCancel) {
        return new Button("Cancel", e1 -> {
            FinApplication finapp = binder.getBean();
            if (finapp.getStatus() == Status.NEWLY_CREATED) {
                finappService.remove(finapp);
            }
            onPostCancel.accept(finapp);
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

    private Set<String> validateApplication(ApplicantService applicantService, Binder<FinApplication> binder) {
        Set<String> errors = new HashSet<>();
        binder.validate().getValidationErrors().stream().forEach(e -> errors.add(e.getErrorMessage()));

        FinApplication finapp = binder.getBean();
        
        if (applicantService.countApplicants(finapp)<=0) {
            errors.add("No applicant");
        }
        applicantService.getApplicants(finapp).forEach(applicant -> {
            for (String err : applicant.getErrors()) {
                errors.add("[" + applicant.getFullName() + "] " + err);
            }
        });
        return errors;
    }

    private void buildApplicantDialog(Applicant applicant, FinApplication finApplication, ApplicantService applicantService, DefaultOidcUser user, Editable editable, Grid<Applicant> gridApplicants) {
        ApplicantForm appForm = new ApplicantForm(applicant, finApplication, editable, user, applicantService, a -> gridApplicants.getDataProvider().refreshAll());
        appForm.open();
    }

}

//package com.azrul.smefinancing.views.application;
//
//import com.azrul.smefinancing.views.attachments.AttachmentsPanel;
//import com.azrul.smefinancing.domain.Status;
//import com.azrul.chenook.views.common.Card;
//import com.azrul.smefinancing.domain.Applicant;
//import com.azrul.smefinancing.domain.FinApplication;
//import com.azrul.smefinancing.service.ApplicantService;
//import com.azrul.smefinancing.service.FinApplicationService;
//import com.azrul.smefinancing.views.applicant.ApplicantForm;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
//import com.vaadin.flow.component.datetimepicker.DateTimePicker;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.NativeLabel;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.BigDecimalField;
//import com.vaadin.flow.component.textfield.TextArea;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.data.binder.Binder;
//import com.vaadin.flow.theme.lumo.LumoUtility;
//import java.util.List;
//import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
//import java.util.function.Consumer;
//
///**
// *
// * @author azrul
// */
//public class ApplicationForm extends Dialog{
//
//    
//    public ApplicationForm(
//            FinApplication finapp,
//            DefaultOidcUser user,
//            ApplicantService applicantService,
//            FinApplicationService finappService,
//            Consumer<FinApplication> onPostSave
//    ) {
//        //Boolean isNew = finapp == null;
//        Boolean editable = finapp==null || finapp.getStatus() == Status.DRAFT || finapp.getStatus() == Status.NEED_MORE_INFO;
//        Binder<FinApplication> binder = new Binder<>(FinApplication.class);
//        if (finapp != null) {
//            binder.setBean(finapp);
//        } else {
//            return; //if finapp is null, this will not work
//        }
//
//        FormLayout form = new FormLayout();
//       // form.getStyle().
//        
//        TextField tfID = new TextField("Application ID (AA Number)");
//        binder.forField(tfID).bindReadOnly(fa->fa.getId().toString());
//        form.add(tfID);
//
//        TextField tfName = new TextField("Business Name");
//        binder.forField(tfName).asRequired().bind(FinApplication::getName, FinApplication::setName);
//        form.add(tfName);
//
//        TextArea tfAddress = new TextArea("Address");
//        binder.bind(tfAddress, FinApplication::getAddress, FinApplication::setAddress);
//        form.add(tfAddress);
//
//        TextField tfPostalCode = new TextField("Postal code");
//        binder.bind(tfName, FinApplication::getPostalCode, FinApplication::setPostalCode);
//        form.add(tfPostalCode);
//
//        ComboBox cbState = new ComboBox("State");
//        cbState.setItems(List.of("W. Persekutuan Kuala Lumpur", "W. Persekutuan Putrajaya", "Selangor", "Penang", "Johor", "Perak"));
//        binder.bind(cbState, FinApplication::getState, FinApplication::setState);
//        form.add(cbState);
//
//        DateTimePicker dtpApplicationDate = new DateTimePicker("Application date");
//        dtpApplicationDate.setReadOnly(true);
//        binder.bind(dtpApplicationDate, FinApplication::getApplicationDate, FinApplication::setApplicationDate);
//        form.add(dtpApplicationDate);
//
//        TextField tfBizRegNumber = new TextField("SSM Registration");
//        binder.bind(tfBizRegNumber, FinApplication::getSsmRegistrationNumber, FinApplication::setSsmRegistrationNumber);
//        form.add(tfBizRegNumber);
//
//        TextField tfStatus = new TextField("Status");
//        tfStatus.setReadOnly(true);
//        binder.bind(tfStatus, FinApplication::getState, FinApplication::setState);
//        form.add(tfStatus);
//
//        BigDecimalField tfFinRequested = new BigDecimalField("Financing Applied");
//        tfFinRequested.setPrefixComponent(new NativeLabel("MYR "));
//        binder.bind(tfFinRequested, FinApplication::getFinancingRequested, FinApplication::setFinancingRequested);
//
//        form.add(tfFinRequested);
//        TextArea taReasonForFinancing = new TextArea("Reason for financing");
//        binder.bind(taReasonForFinancing, FinApplication::getReasonForFinancing, FinApplication::setReasonForFinancing);
//        form.add(tfFinRequested);
//
//        
//        Grid<Applicant> gridApplicants = new Grid<>();
//
//        final FinApplication fa = finapp;
//        gridApplicants.addComponentColumn(app -> {
//            Card card = new Card("Name: " + app.getFullName());
//            card.add(new NativeLabel("Email:    "+app.getEmail()));
//            card.add(new NativeLabel("Position: "+app.getPosition()));    
//            card.add(new NativeLabel("Phone:    "+app.getPhoneNumber())); 
//            Button btnSeeMore = new Button("See more", e -> {
//                buildApplicantDialog(app, fa, applicantService, editable,gridApplicants);
//            });
//            Button btnRemove = new Button("Remove", e -> {
//                ConfirmDialog dialog = new ConfirmDialog();
//                dialog.setHeader("Remove applicant");
//                dialog.setText("Are you sure to remove this applicant");
//
//                dialog.setCancelable(true);
//                dialog.addCancelListener(event -> dialog.close());
//
//                dialog.setConfirmText("Remove");
//                dialog.addConfirmListener(event -> {
//                    applicantService.remove(app);
//                    gridApplicants.getDataProvider().refreshAll();
//                    dialog.close();
//                });
//                dialog.open();
//            });
//            HorizontalLayout buttonPanel = new HorizontalLayout();
//            buttonPanel.add(btnSeeMore, btnRemove);
//            card.add(buttonPanel);
//            if (editable==false){
//                btnRemove.setEnabled(false);
//            }
//            return card;
//        });
//        gridApplicants.setAllRowsVisible(true);
//        gridApplicants.setItems(applicantService.getApplicantsByApplication(binder.getBean()));
//
//        Button btnAddApplicant = new Button("Add applicant", e -> {
//            buildApplicantDialog(null, fa, applicantService, editable,gridApplicants);
//        });
//        
//        VerticalLayout applicantPanel = new VerticalLayout();
//        applicantPanel.add(btnAddApplicant);
//        applicantPanel.add(gridApplicants);
//        applicantPanel.addClassNames(
//                LumoUtility.Padding.SMALL,
//                LumoUtility.Background.BASE
//        );
//        this.add(form); 
//        this.add(applicantPanel);
//        
//        
//        if (editable == false) {
//            btnAddApplicant.setEnabled(false);
//            taReasonForFinancing.setReadOnly(true);
//            tfFinRequested.setReadOnly(true);
//            tfName.setReadOnly(true);
//            tfAddress.setReadOnly(true);
//            tfPostalCode.setReadOnly(true);
//            cbState.setReadOnly(true);
//            tfBizRegNumber.setReadOnly(true);
//        }
//        AttachmentsPanel attchPanel = new AttachmentsPanel(
//                finapp.getId(),
//                Long.toString(finapp.getId()),
//                editable,
//                a->{},
//                a->{}
//        );
//        this.add(attchPanel);
//        save(binder, user, editable, finappService, onPostSave);
//    }
//    
//    private void save(
//            Binder<FinApplication> binder, 
//            DefaultOidcUser user, 
//            Boolean editable, 
//            FinApplicationService finappService,
//            Consumer<FinApplication> onPostSave){
//            
//        Button btnSaveFinApp = new Button("Save and submit", e1 -> {
//                if (validateApplication(binder)) {
//                    FinApplication finapp = binder.getBean();
//                    finapp.setStatus(Status.IN_PROGRESS);
//                    finappService.save(finapp, user.getPreferredUsername());
//                    onPostSave.accept(finapp);
//                    this.close();
//                }
//            });
//            Button btnSaveDraft = new Button("Save draft", e1 -> {
//                    FinApplication finapp = binder.getBean();
//                    finapp.setStatus(Status.DRAFT);
//                    finappService.save(finapp, user.getPreferredUsername());
//                    onPostSave.accept(finapp);
//                    this.close();
//            });
//            this.getFooter().add( btnSaveFinApp );
//            this.getFooter().add( btnSaveDraft );
//            if (!editable) {
//                btnSaveFinApp.setEnabled(false);
//                btnSaveDraft.setEnabled(false);
//            }
//            this.getFooter().add(new Button("Cancel", e1 -> {
//                this.close();
//            }));
//            this.getFooter().add(new Button("Remove", e1 -> {
//                FinApplication finapp = binder.getBean();
//                finappService.remove(finapp);
//                
//                this.close();
//            }));
//           
//    }
//    
//    private Boolean validateApplication(Binder<FinApplication> binder){
//        if (Boolean.FALSE.equals(binder.validate().isOk())){
//            return false;
//        }
//        FinApplication finapp = binder.getBean();
//        for (Applicant applicant:finapp.getApplicants()){
//            if (applicant.getSignature()==null){
//                return false;
//            }else if (applicant.getSignature().length<=0){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * @return the finapp
//     */
//    
//
//    private void buildApplicantDialog(
//            Applicant applicant, 
//            FinApplication finApplication, 
//            ApplicantService applicantService,
//            Boolean editable,
//            Grid<Applicant> gridApplicants
//        ) {
//       
//        ApplicantForm appform = new ApplicantForm(
//                applicant,
//                finApplication,
//                editable, 
//                applicantService,
//                a->{
//                    gridApplicants.getDataProvider().refreshAll();
//                }
//        );
//        appform.open();
//    }
//
//}
