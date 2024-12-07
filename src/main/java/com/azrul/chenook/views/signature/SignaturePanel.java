package com.azrul.chenook.views.signature;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.workflow.WorkflowAwareButton;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
@SpringComponent
public class SignaturePanel<T> extends CustomField<Signature> {

    //private byte[] signatureImageBinary;
    private Binder<T> binder;
    private String fieldName;
    private Button btnSign;
    private VerticalLayout signaturePanel;
    private WorkflowAwareGroup group;

    public void applyGroup() {
        if (this.group != null) {
            this.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }

    public static <T> SignaturePanel create(
            String fieldName,
            Binder<T> binder,
            WorkflowAwareGroup group
    ) {
        var field = ApplicationContextHolder.getBean(SignaturePanel.class);

        field.init(fieldName, binder, group);
        T item = binder.getBean();

        List<Validator> validators = new ArrayList<>();
        field.setId(fieldName);
        field.applyGroup();

        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(item.getClass(),
                fieldName
        );

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

        var bindingBuilder = binder.forField(field);
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

//     private SignaturePanel(@Autowired SignatureService signService){
//        this.signService=signService;
//     }
    private void init(String fieldName, Binder<T> binder, WorkflowAwareGroup group) {

        this.group = group;
        this.binder = binder;
        this.fieldName = fieldName;

        signaturePanel = new VerticalLayout();
        setSignaturePad();
        btnSign = WorkflowAwareButton.create(group);
        btnSign = new Button("Sign", e -> {
            Dialog signDialog = new Dialog();
            signDialog.setWidth("550px");
            signDialog.setWidth("550px");
            SignatureCapture signCapture = new SignatureCapture();
            signCapture.getStyle().set("background-color", "aliceblue");
            signDialog.add(signCapture);
            signDialog.open();
            Button btnSaveSign = new Button("Save", e1 -> {
                signCapture.getSignatureAsImage(imageBinary -> {
                    if (imageBinary != null) {
                        getSignature().setData(imageBinary);
                        setSignaturePad();
                        signDialog.close();
                    }
                });
            });
            btnSaveSign.setId("btnSaveSign");
            signDialog.getFooter().add(btnSaveSign);
            signDialog.getFooter().add(new Button("Close", e1 -> signDialog.close()));

        });
        //this.add(lbSign);
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setId("uploadSign");
        upload.setAcceptedFileTypes(".png");

        int maxFileSizeInBytes = 200 * 1024; // 10KB
        upload.setMaxFileSize(maxFileSizeInBytes);

        this.add(upload);

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream(fileName);
                Signature signature = new Signature();
                signature.setData(IOUtils.toByteArray(inputStream));
                setSignature(signature);
                setSignaturePad();
            } catch (IOException ex) {
                Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        this.add(signaturePanel);
        this.add(btnSign);

    }

    private void setSignaturePad() {
        if (getSignature() != null) {
            byte[] imageBinary = getSignature().getData();
            if (imageBinary != null && imageBinary.length > 0) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBinary);
                StreamResource resource = new StreamResource("signature.png", () -> inputStream);
                Image signatureImage = new Image(resource, "Signature");
                signaturePanel.removeAll();
                signaturePanel.add(signatureImage);
            }
        }

    }

    public boolean isSignaturePresent() {
        return !(getSignature().getData() == null || getSignature().getData().length <= 0);
    }

    @Override
    public void setEnabled(boolean enable) {
        btnSign.setEnabled(enable);
    }

    public void setSignature(Signature signature) {
        try {
            Class<?> workClass = binder.getBean().getClass();
            Field signField = WorkflowUtils.getField(workClass, fieldName);
            signField.set(binder.getBean(), signature);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SignaturePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Signature getSignature() {
        try {
            Class<?> itemClass = binder.getBean().getClass();
            Field signField = WorkflowUtils.getField(itemClass, fieldName);
            if (signField.get(binder.getBean())==null){
                signField.set(binder.getBean(), new Signature());
            }
            return (Signature) signField.get(binder.getBean());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SignaturePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

   
    @Override
    protected void setPresentationValue(Signature newPresentationValue) {
        setValue(newPresentationValue);
    }

    @Override
    protected Signature generateModelValue() {
        return getValue();
    }

    @Override
    public Signature getValue() {
        return getSignature();
    }

    @Override
    public void setValue(Signature signature) {
        setSignature(signature);
        setSignaturePad();
    }

    /**
     * @return the binder
     */
    public Binder<T> getBinder() {
        return binder;
    }

    /**
     * @param binder the binder to set
     */
    public void setBinder(Binder<T> binder) {
        this.binder = binder;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

}
