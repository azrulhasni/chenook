/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.signature;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.service.SignatureService;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.common.components.SignatureCapture;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
@SpringComponent
public class SignaturePanel extends VerticalLayout{
    private byte[] signatureImageBinary;
    private Button btnSign;
    private VerticalLayout signaturePanel;
    private final SignatureService signService;
    
   
    
    
    public static SignaturePanel create(){
        var signPanel = ApplicationContextHolder.getBean(SignaturePanel.class);
        return signPanel;
    }
        
     private SignaturePanel(@Autowired SignatureService signService){
        this.signService=signService;
        NativeLabel lbSign = new NativeLabel("Signature:");
        signaturePanel = new VerticalLayout();

        btnSign = new Button("Sign", e -> {
            Dialog signDialog = new Dialog();
            signDialog.setWidth("550px");
            signDialog.setWidth("550px");
            SignatureCapture signature = new SignatureCapture();
            signature.getStyle().set("background-color", "aliceblue");
            signDialog.add(signature);
            signDialog.open();
            Button btnSaveSign = new Button("Save", e1 -> {
                signature.getSignatureAsImage(imageBinary -> {
                    if (imageBinary != null) {
                        signatureImageBinary = imageBinary;
                        setSignaturePad(imageBinary, signaturePanel);
                        signDialog.close();
                    }
                });
            });
            btnSaveSign.setId("btnSaveSign");
            signDialog.getFooter().add(btnSaveSign);
            signDialog.getFooter().add(new Button("Close", e1 -> signDialog.close()));

        });
        this.add(lbSign);
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
                signatureImageBinary = IOUtils.toByteArray(inputStream);
                setSignaturePad(signatureImageBinary , signaturePanel);
            } catch (IOException ex) {
                Logger.getLogger(AttachmentsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        this.add(signaturePanel);
        this.add(btnSign);
    }
    
    public void setParentAndContext(Long parentId, String context){
       Signature sign = signService.findSignatureByParentAndContext(parentId, context);
       if (sign!=null){
        signatureImageBinary = sign.getData();
        setSignaturePad(signatureImageBinary, signaturePanel);
       }
    }
    
    private void setSignaturePad(byte[] imageBinary, VerticalLayout signaturePanel) {
        if (imageBinary != null && imageBinary.length > 0) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBinary);
            StreamResource resource = new StreamResource("signature.png", () -> inputStream);
            Image signatureImage = new Image(resource, "Signature");
            signaturePanel.removeAll();
            signaturePanel.add(signatureImage);
        }

    }
    
    public boolean isSignaturePresent(){
        return !(signatureImageBinary == null || signatureImageBinary.length <= 0);
    }
    
    @Override
    public void setEnabled(boolean enable){
        btnSign.setEnabled(enable);
    }
    
    
    public void save(Long parentId, String context){
        
        Signature sign = signService.findSignatureByParentAndContext(parentId, context);
        if (sign==null){
            sign = new Signature();
        }
        sign.setContext(context);
        sign.setParentId(parentId);
        if (signatureImageBinary!=null){
            sign.setSize((long)signatureImageBinary.length);
            sign.setData(signatureImageBinary);
        }
        signService.save(sign);
    }
}
