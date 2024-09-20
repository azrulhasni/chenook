package com.azrul.smefinancing.views.applicant;

import com.azrul.chenook.views.common.StringToUngroupLongConverter;
import com.azrul.chenook.views.common.WorkflowAwareComboBox;
import com.azrul.chenook.views.common.WorkflowAwareTextField;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.ApplicantType;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.chenook.views.signature.SignaturePanel;
import com.azrul.smefinancing.views.common.Editable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.converter.StringToLongConverter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ApplicantForm extends Dialog {

    // Class Variables for Strings
    private static final String FULL_NAME_LABEL = "Full name";
    private static final String IC_NUMBER_LABEL = "IC Number";
    private static final String POSITION_LABEL = "Position";
    private static final String PHONE_NUMBER_LABEL = "Phone number";
    private static final String EMAIL_LABEL = "Email";
    private static final String APPLICANT_TYPE_LABEL = "Applicant type";
    private static final String SIGNATURE_CONTEXT = "SME_FIN";
    private static final String SIGNATURE_ERROR = "Signature not present";

    private final Binder<Applicant> binder = new Binder<>(Applicant.class);
    private final ApplicantService applicantService;
    private final SignaturePanel signPanel;

    public ApplicantForm(
            Applicant applicant,
            FinApplication finapp,
            Editable editable,
            OidcUser user,
            ApplicantService applicantService,
            Consumer<Applicant> onPostSave
    ) {
        this.applicantService = applicantService;
        

        FormLayout form = new FormLayout();
        signPanel = new SignaturePanel();
        
        // Initialize applicant and signature panel
        if (applicant != null) {
            binder.setBean(applicant);
            signPanel.setParentAndContext(applicant.getId(), SIGNATURE_CONTEXT);
        } else {
            binder.setBean(new Applicant());
        }
        
        
        // Create form fields
        TextField tfID = WorkflowAwareTextField.create("id", false, binder, new StringToUngroupLongConverter("Not a number"));
        form.add(tfID);
        
        TextField tfFullName = WorkflowAwareTextField.create("fullName", true, binder);
        form.add(tfFullName);
       
        TextField tfICNumber =  WorkflowAwareTextField.create("icNumber", true, binder);
        form.add(tfICNumber);
        
        TextField tfPhone = WorkflowAwareTextField.create("phoneNumber",true, binder);
        form.add(tfPhone);
        
        TextField tfEmail = WorkflowAwareTextField.create("email", true, binder);
        form.add(tfEmail);

        // Applicant type combo box
        ComboBox<ApplicantType> cbType = WorkflowAwareComboBox.create("type", binder, Set.of(ApplicantType.values()));
        //cbType.setItems(ApplicantType.values());
        //binder.forField(cbType).bind(Applicant::getType, Applicant::setType);
        form.add(cbType);
        this.add(form, signPanel);

       
        // Save button and its logic
        Button btnSave = new Button("Save", e -> {
            saveApplicant(finapp, onPostSave);
            
        });
        
         Button btnSaveDraft = new Button("Save draft", e -> {
            saveDraftApplicant(finapp, onPostSave);
            
        });
        
        configureEditability(editable, user, tfFullName, tfICNumber,tfPhone, tfEmail, cbType, btnSave);

        this.getFooter().add(btnSaveDraft,btnSave, new Button("Cancel", e -> this.close()));
    }

    private void configureEditability(Editable editable, 
                                      OidcUser user, 
                                      TextField tfFullName, 
                                      TextField tfICNumber, 
                                      TextField tfPhone, 
                                      TextField tfEmail, 
                                      ComboBox<ApplicantType> cbType, 
                                      Button btnSave) {
        if (editable != Editable.YES) {
            setFieldsReadOnly(tfFullName, tfICNumber,tfPhone, tfEmail, cbType, btnSave);
            
            if (editable == Editable.YES_AS_APPLICANT){
                Applicant applicant = binder.getBean();
                if (StringUtils.equals(user.getEmail(), applicant.getEmail())){
                    signPanel.setEnabled(true);
                    btnSave.setEnabled(true);
                }
            }
        }
    }

    private void setFieldsReadOnly(TextField tfFullName, 
                                   TextField tfICNumber, 
                                   TextField tfPhone, 
                                   TextField tfEmail, 
                                   ComboBox<ApplicantType> cbType, 
                                   Button btnSave) {
        tfFullName.setReadOnly(true);
        tfICNumber.setReadOnly(true);
       // tfDesignation.setReadOnly(true);
        tfPhone.setReadOnly(true);
        tfEmail.setReadOnly(true);
        cbType.setReadOnly(true);
        btnSave.setEnabled(false);
        signPanel.setEnabled(false);
    }

    private void saveApplicant(FinApplication finapp, Consumer<Applicant> onPostSave) {
        Applicant applicant = binder.getBean();
        Set<String> errors = validateApplicant();
        applicant.setErrors(errors);
        if (errors.isEmpty()){
            applicantService.save(applicant, finapp);
            signPanel.save(applicant.getId(), SIGNATURE_CONTEXT);
            onPostSave.accept(applicant);
            this.close();
        }
    }
    
    private void saveDraftApplicant(FinApplication finapp, Consumer<Applicant> onPostSave) {
        Applicant applicant = binder.getBean();
        applicantService.save(applicant, finapp);
        signPanel.save(applicant.getId(), SIGNATURE_CONTEXT);
        onPostSave.accept(applicant);
        this.close();
    }

    private Set<String> validateApplicant() {
        Set<String> errors = new HashSet<>();
        for (ValidationResult err : binder.validate().getValidationErrors()) {
            errors.add(err.getErrorMessage());
        }
        if (!signPanel.isSignaturePresent()) {
            errors.add(SIGNATURE_ERROR);
        }
        return errors;
    }
}
