/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.signature;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Signature;
import com.azrul.chenook.service.SignatureService;
import com.azrul.chenook.views.common.SignatureCapture;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
public class SignaturePanel extends VerticalLayout{
    private byte[] signatureImageBinary;
    private Button btnSign;
    private VerticalLayout signaturePanel;
    
    @Autowired
    private SignatureService signService;
    
    public SignaturePanel(){
        ApplicationContextHolder.autowireBean(this);
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

            signDialog.getFooter().add(new Button("Save", e1 -> {
                signature.getSignatureAsImage(imageBinary -> {
                    if (imageBinary != null) {
                        signatureImageBinary = imageBinary;
                        setSignaturePad(imageBinary, signaturePanel);
                        signDialog.close();
                    }
                });
            }));
            signDialog.getFooter().add(new Button("Close", e1 -> signDialog.close()));

        });
        this.add(lbSign);
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
