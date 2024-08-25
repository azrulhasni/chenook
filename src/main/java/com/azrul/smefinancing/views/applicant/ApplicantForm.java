/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.views.applicant;

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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class ApplicantForm extends Dialog {

    private Binder<Applicant> binder = new Binder<>(Applicant.class);
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

        TextField tfFullName = new TextField("Full name");
        binder.forField(tfFullName).asRequired().bind(Applicant::getFullName, Applicant::setFullName);
        form.add(tfFullName);

        TextField tfICNumber = new TextField("IC Number");
        binder.forField(tfICNumber).asRequired().bind(Applicant::getIcNumber, Applicant::setIcNumber);
        form.add(tfICNumber);

        TextField tfDesignation = new TextField("Position");
        binder.forField(tfDesignation).asRequired().bind(Applicant::getPosition, Applicant::setPosition);
        form.add(tfDesignation);

        TextField tfPhone = new TextField("Phone number");
        binder.forField(tfPhone).asRequired().bind(Applicant::getPhoneNumber, Applicant::setPhoneNumber);
        form.add(tfPhone);

        TextField tfEmail = new TextField("Email");
        binder.forField(tfEmail).asRequired().bind(Applicant::getEmail, Applicant::setEmail);
        form.add(tfEmail);

        ComboBox<ApplicantType> cbType = new ComboBox<>("Applicant type");
        cbType.setItems(Arrays.asList(ApplicantType.values()));
        binder.forField(cbType).bind(Applicant::getType, Applicant::setType);
        form.add(cbType);

        signPanel = new SignaturePanel();
        this.add(form);
        this.add(signPanel);

        if (applicant != null) {
            binder.setBean(applicant);
            signPanel.setParentAndContext(applicant.getId(), "SME_FIN");
        } else {
            Applicant a = new Applicant();
            binder.setBean(a);
        }

        

        Button btnSave = new Button("Save", e1 -> {
            Applicant appli = getApplicant();
            Set<String> errors = validateApplicant();
            appli.setErrors(errors);
            applicantService.save(appli, finapp);
            signPanel.save(appli.getId(), "SME_FIN");
            onPostSave.accept(appli);
            this.close();
            
//            else {
//                Notification notif = new Notification();
//                StringBuilder errors = new StringBuilder();
//                for (String err:appli.getErrors()){
//                    errors.append(err);
//                    errors.append("\n");
//                }
//                notif.setText(errors.toString());
//                notif.open();
//            }

        });
        if (editable != Editable.YES) {
            tfFullName.setReadOnly(true);
            tfICNumber.setReadOnly(true);
            tfDesignation.setReadOnly(true);
            tfPhone.setReadOnly(true);
            tfEmail.setReadOnly(true);
            cbType.setReadOnly(true);
            btnSave.setEnabled(false);
            signPanel.setEnabled(false); //disable first, then calculate
            
            if (editable == Editable.YES_AS_APPLICANT){
                Applicant a = binder.getBean();
                if (StringUtils.equals(user.getEmail(),a.getEmail())){
                    signPanel.setEnabled(true); //the user is not the creator but he is one of the applicant
                    btnSave.setEnabled(true);
                }
            }
        }
        this.getFooter().add(btnSave);
        this.getFooter().add(new Button("Cancel", e1 -> {
            this.close();
        }));

    }

    private Applicant getApplicant() {
        Applicant app = binder.getBean();
        
        return app;
    }

    private Set<String> validateApplicant() {
        Set<String> errors = new HashSet<>();
        for (ValidationResult err : binder.validate().getValidationErrors()) {
            errors.add(err.getErrorMessage());
        }
        if (!signPanel.isSignaturePresent()) {
            errors.add("Signature not present");
        }
        return errors;
    }
}
