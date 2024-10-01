/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.attachments;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.AttachmentService;
import com.azrul.chenook.views.common.components.WorkflowAwareGroup;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.codecamp.vaadin.serviceref.ServiceRef;
import java.io.IOException;
import java.io.InputStream;
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
public class AttachmentsPanel<T> extends VerticalLayout {

    //private final AttachmentService attachmentService;
    private static final String[] extensions = {".bib", ".doc", ".xml", ".docx", ".fodt", ".html", ".ltx", ".txt", ".odt", ".ott", ".pdb", ".pdf", ".psw", ".rtf", ".sdw", ".stw", ".sxw", ".uot", ".vor", ".wps", ".epub", ".png", ".bmp", ".emf", ".eps", ".fodg", ".gif", ".jpg", ".met", ".odd", ".otg", ".pbm", ".pct", ".pgm", ".ppm", ".ras", ".std", ".svg", ".svm", ".swf", ".sxd", ".sxw", ".tiff", ".xhtml", ".xpm", ".fodp", ".potm", ".pot", ".pptx", ".pps", ".ppt", ".pwp", ".sda", ".sdd", ".sti", ".sxi", ".uop", ".wmf", ".csv", ".dbf", ".dif", ".fods", ".ods", ".ots", ".pxl", ".sdc", ".slk", ".stc", ".sxc", ".uos", ".xls", ".xlt", ".xlsx", ".tif", ".jpeg", ".odp"};

   
    private final AttachmentService attachmentService;
    private final WorkflowConfig workflowConfig;
    
    private AttachmentsPanel(
            @Autowired AttachmentService attachmentService,
            @Autowired WorkflowConfig workflowConfig
    ){
        this.attachmentService=attachmentService;
        this.workflowConfig=workflowConfig;
    }
    
    
    public static AttachmentsPanel create(Long parentId,
            String context,
            String fileLocation,
            Consumer<Attachment> onPostSave,
            Consumer<Attachment> onPostRemove){
        var attachmentsPanel = ApplicationContextHolder.getBean(AttachmentsPanel.class);
        attachmentsPanel.init(parentId, context, fileLocation, onPostSave, onPostRemove);
        return attachmentsPanel;
    }

    private void init(
            Long parentId,
            String context,
            String fileLocation,
            Consumer<Attachment> onPostSave,
            Consumer<Attachment> onPostRemove
    ) {

        this.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.Background.BASE
        );
        
       
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setId("upload");
        upload.setAcceptedFileTypes(extensions);

        int maxFileSizeInBytes = 20 * 1024 * 1024; // 20MB
        upload.setMaxFileSize(maxFileSizeInBytes);
        
        this.add(upload);

        Grid<Attachment> gridFiles = new Grid<>();
        gridFiles.getStyle().set("max-width","285px");

        gridFiles.setItems(attachmentService.findAttachmentsByParentAndContext(parentId,context));
        gridFiles.addComponentColumn(att -> {
            Card filePanel = new Card("File: " + att.getFileName());
            HorizontalLayout btnPanel = new HorizontalLayout();
            btnPanel.add(addRemoveButton(gridFiles, att, /*editable,*/ onPostRemove),
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
                        parentId,
                        context,
                        fileLocation,
                        fileName,
                        event.getMIMEType(),
                        inputStream.readAllBytes());
                onPostSave.accept(att);
                gridFiles.getDataProvider().refreshAll();
            } catch (IOException ex) {
                Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    private Button addRemoveButton(Grid<Attachment> gridFiles, Attachment attc, /*Boolean editable,*/ Consumer<Attachment> onPostRemove) {
        Button btnRemove = new Button("Remove", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Remove applicant");
            dialog.setText("Are you sure to remove this applicant");

            dialog.setCancelable(true);
            dialog.addCancelListener(event -> dialog.close());

            dialog.setConfirmText("Remove");
            dialog.addConfirmListener(event -> {
                attachmentService.remove(attc);
                gridFiles.getDataProvider().refreshAll();
                onPostRemove.accept(attc);
                dialog.close();
            });
            dialog.open();

        });
        //btnRemove.setEnabled(editable);
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
}
