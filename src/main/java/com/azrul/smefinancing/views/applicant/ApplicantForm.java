package com.azrul.smefinancing.views.applicant;

import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.views.common.converter.StringToUngroupLongConverter;
import com.azrul.chenook.views.common.components.WorkflowAwareComboBox;
import com.azrul.chenook.views.common.components.WorkflowAwareGroup;
import com.azrul.chenook.views.common.components.WorkflowAwareTextField;
import com.azrul.smefinancing.domain.Applicant;
import com.azrul.smefinancing.domain.ApplicantType;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.chenook.views.signature.SignaturePanel;
import com.azrul.chenook.workflow.model.BizProcess;
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
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
public class ApplicantForm extends Dialog {

   
    private static final String SIGNATURE_CONTEXT = "SME_FIN";
    private static final String SIGNATURE_ERROR = "Signature not present";

    private final Binder<Applicant> binder = new Binder<>(Applicant.class);
    private final ApplicantService applicantService;
    private final WorkflowConfig workflowConfig;
    private final SignaturePanel signPanel;

    public ApplicantForm(
            @Autowired ApplicantService applicantService,
            @Autowired WorkflowConfig workflowConfig,
            @Autowired SignaturePanel signPanel
            
    ){
        this.applicantService =applicantService;
        this.workflowConfig = workflowConfig;
        this.signPanel=signPanel;
    }
            
            
    public void init(        
            Applicant applicant,
            FinApplication finapp,
            OidcUser user,
            Consumer<Applicant> onPostSave
    ) {
    

        FormLayout form = new FormLayout();
       // signPanel = new SignaturePanel();
       signPanel.init();
       BizProcess bizProcess = workflowConfig.rootBizProcess();

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

        TextField tfICNumber = WorkflowAwareTextField.create("icNumber", true, binder);
        form.add(tfICNumber);

        TextField tfPhone = WorkflowAwareTextField.create("phoneNumber", true, binder);
        form.add(tfPhone);

        TextField tfEmail = WorkflowAwareTextField.create("email", true, binder);
        form.add(tfEmail);

        // Applicant type combo box
        ComboBox<ApplicantType> cbType = WorkflowAwareComboBox.create("type", binder, Set.of(ApplicantType.values()));
        form.add(cbType);
          WorkflowAwareGroup group = WorkflowAwareGroup.create(user, finapp, bizProcess);
        group.add(form);
        this.add(form, signPanel);

        // Save button and its logic
        Button btnSave = new Button("Save", e -> {
            saveApplicant(finapp, onPostSave);
        });
        btnSave.setId("btnSave");

        Button btnSaveDraft = new Button("Save draft", e -> {
            saveDraftApplicant(finapp, onPostSave);
        });
        btnSaveDraft.setId("btnSaveDraft");

        this.getFooter().add(btnSaveDraft, btnSave, new Button("Cancel", e -> this.close()));
    }

    private void saveApplicant(FinApplication finapp, Consumer<Applicant> onPostSave) {
        Applicant applicant = binder.getBean();
        Set<String> errors = validateApplicant();
        applicant.setErrors(errors);
        if (errors.isEmpty()) {
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
