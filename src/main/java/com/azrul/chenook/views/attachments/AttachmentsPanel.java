/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.attachments;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.AttachmentService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.workflow.WorkflowAwareButton;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.codecamp.vaadin.serviceref.ServiceRef;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.vaadin.olli.FileDownloadWrapper;

/**
 *
 * @author azrul
 */
@SpringComponent
public class AttachmentsPanel<T> extends CustomField<Set<Attachment>> { 

    //private final AttachmentService attachmentService;
    private static final String[] extensions = {".bib", ".doc", ".xml", ".docx", ".fodt", ".html", ".ltx", ".txt", ".odt", ".ott", ".pdb", ".pdf", ".psw", ".rtf", ".sdw", ".stw", ".sxw", ".uot", ".vor", ".wps", ".epub", ".png", ".bmp", ".emf", ".eps", ".fodg", ".gif", ".jpg", ".met", ".odd", ".otg", ".pbm", ".pct", ".pgm", ".ppm", ".ras", ".std", ".svg", ".svm", ".swf", ".sxd", ".sxw", ".tiff", ".xhtml", ".xpm", ".fodp", ".potm", ".pot", ".pptx", ".pps", ".ppt", ".pwp", ".sda", ".sdd", ".sti", ".sxi", ".uop", ".wmf", ".csv", ".dbf", ".dif", ".fods", ".ods", ".ots", ".pxl", ".sdc", ".slk", ".stc", ".sxc", ".uos", ".xls", ".xlt", ".xlsx", ".tif", ".jpeg", ".odp"};

    private Binder<T> binder;
    private String fieldName;
    private WorkflowAwareGroup group;
    private final WorkflowConfig workflowConfig;
    private final  AttachmentService attachmentService;
    
    private AttachmentsPanel(
            @Autowired AttachmentService attachmentService,
            @Autowired WorkflowConfig workflowConfig
    ){
        this.attachmentService=attachmentService;
        this.workflowConfig=workflowConfig;
    }
    
    
    public static <T> AttachmentsPanel create(String fieldName,
            Binder<T> binder,
            WorkflowAwareGroup group,
            String fileLocation){
        var field = ApplicationContextHolder.getBean(AttachmentsPanel.class);
        
        field.init(fieldName, binder, group, fileLocation);
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
                WorkflowUtils.applyNotEmpty(
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

    private void init(
            String fieldName,
            Binder<T> binder,
            WorkflowAwareGroup group,
            String fileLocation
    ) {
        this.fieldName = fieldName;
        this.binder = binder;
        
        
        Set<Attachment> attachments = getAttachments();
        this.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.Background.BASE
        );
        
       
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        if (group.calculateEnable()==false){
            upload.setMaxFiles(0);
        }
        upload.setVisible(group.calculateVisible());
        upload.setId("upload");
        upload.setAcceptedFileTypes(extensions);

        int maxFileSizeInBytes = 20 * 1024 * 1024; // 20MB
        upload.setMaxFileSize(maxFileSizeInBytes);
        
        this.add(upload);

        Grid<Attachment> gridFiles = new Grid<>();
        gridFiles.getStyle().set("max-width","285px");

        gridFiles.setItems(attachments);
        gridFiles.addComponentColumn(att -> {
            Card filePanel = new Card("File: " + att.getFileName());
            HorizontalLayout btnPanel = new HorizontalLayout();
            btnPanel.add(addRemoveButton(gridFiles, att, group),
                    addBtnToDownloadFile(att, "Download"));
            filePanel.add(btnPanel);
            return filePanel;
        }
        );
        gridFiles.setAllRowsVisible(true);
        gridFiles.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        this.add(gridFiles);

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream(fileName);
                Attachment att = attachmentService.saveToStorage(
                        fileLocation,
                        fileName,
                        event.getMIMEType(),
                        inputStream.readAllBytes());
                addAttachments(Set.of(att));
                gridFiles.getDataProvider().refreshAll();
            } catch (IOException ex) {
                Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }
    
    public void applyGroup() {
        if (this.group != null) {
            this.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }

    private Button addRemoveButton(
            Grid<Attachment> gridFiles, 
            Attachment attc, 
            WorkflowAwareGroup group
    ) {
        WorkflowAwareButton btnRemove = WorkflowAwareButton.create(group);
        btnRemove.setText("Remove");
        btnRemove.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Remove applicant");
            dialog.setText("Are you sure to remove this applicant");

            dialog.setCancelable(true);
            dialog.addCancelListener(event -> dialog.close());

            dialog.setConfirmText("Remove");
            dialog.addConfirmListener(event -> {
                //attachmentService.remove(attc);
                removeAttachment(attc);
                gridFiles.getDataProvider().refreshAll();
                dialog.close();
            });
            dialog.open();

        });
        return btnRemove;
    }

    private Component addBtnToDownloadFile(Attachment attachment, String text) {

        StreamResource resource = new StreamResource(attachment.getFileName(), (out, session) -> {
            attachmentService.getFromStorage(attachment.getFileLocation() + "/" + attachment.getFileName(), out);
        });
        FileDownloadWrapper btnDownloadWrap = new FileDownloadWrapper(resource);
        Button btnDownload = new Button(text);
        btnDownloadWrap.wrapComponent(btnDownload);
        return btnDownloadWrap;

    }
    
    private Set<Attachment> getAttachments(){
        try {
            T item = binder.getBean();
            Field field = WorkflowUtils.getField(item.getClass(), fieldName);
            Set<Attachment> attcs =  (Set<Attachment>)field.get(item);
            if (attcs==null){
                return Set.of();
            }else{
                return attcs;
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Set.of();
    }
    
    private void addAttachments(Set<Attachment> newAttcs){
        try {
            T item = binder.getBean();
            Field field = WorkflowUtils.getField(item.getClass(), fieldName);
            Set<Attachment> attcs =  (Set<Attachment>)field.get(item);
            if (attcs==null){
                field.set(item, newAttcs);
            }else{
                attcs.addAll(newAttcs);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void removeAttachment(Attachment attc){
        try {
            T item = binder.getBean();
            Field field = WorkflowUtils.getField(item.getClass(), fieldName);
            Set<Attachment> attcs =  (Set<Attachment>)field.get(item);
            if (attcs!=null){
                attcs.remove(attc);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Set<Attachment> generateModelValue() {
        return getValue();
    }

    @Override
    protected void setPresentationValue(Set<Attachment> newPresentationValue) {
        setValue(newPresentationValue);  
    }
    
    @Override
    public void setValue(Set<Attachment> attachments){
        addAttachments(attachments);
    }
    
    @Override
    public Set<Attachment> getValue(){
        return getAttachments();
    }
}
