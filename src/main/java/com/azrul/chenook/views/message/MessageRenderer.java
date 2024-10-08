/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.message;


//import com.azrul.chenook.autocomplete.Autocomplete;
import com.azrul.chenook.domain.Message;
import com.azrul.chenook.service.MessageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class MessageRenderer extends ComponentRenderer<Component, Message> {

    public MessageRenderer(OidcUser oidcUser, MessageService msgService) {
        super(msg -> {

            HorizontalLayout card = new HorizontalLayout();
            VerticalLayout cardInfo = new VerticalLayout();
            cardInfo.setPadding(false);
            cardInfo.setSpacing(false);
            // cardInfo.add(new Text("[#"+note.getId()+"] "));

            String name = msg.getFullName() + " (" + msg.getWriterUserName() + ")";
            HorizontalLayout nameLine = new HorizontalLayout();
            NativeLabel txtName = new NativeLabel(name);
            txtName.getStyle().set("fontWeight", "bold");
            nameLine.add(new Text("[#" + msg.getId() + "] "), txtName, new Text(" "));
            cardInfo.add(nameLine);
            cardInfo.add(new NativeLabel(msg.getCreatedDateTime().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss"))));
            HorizontalLayout msgPanel = new HorizontalLayout();
            msgPanel.setWidth("100%");
            //Autocomplete txaMsg = new Autocomplete("@", true);
            TextArea  txaMsg = new TextArea();
            txaMsg.setWidth("100%");
            msgPanel.add(txaMsg);
            txaMsg.setReadOnly(true);
            if (StringUtils.equals(msg.getWriterUserName(), oidcUser.getPreferredUsername())) {
                VerticalLayout txaMsgBtnPanel = new VerticalLayout();
//                Button btnE = new Button();
//                btnE.setIcon(LumoIcon.EDIT.create());
//                btnE.addThemeVariants(ButtonVariant.LUMO_SMALL);
//                btnE.getStyle().set("margin", "0");
//                btnE.getStyle().set("padding", "0");

//                Button btnS = new Button();
//                btnS.setVisible(false);
//                btnS.setIcon(LumoIcon.CHECKMARK.create());
//                btnS.addThemeVariants(ButtonVariant.LUMO_SMALL);
//                btnS.getStyle().set("margin", "0");
//                btnS.getStyle().set("padding", "0");

                Button btnX = new Button();
                btnX.setIcon(LumoIcon.CROSS.create());
                btnX.addThemeVariants(ButtonVariant.LUMO_SMALL);
                btnX.getStyle().set("margin", "0");
                btnX.getStyle().set("padding", "0");

                txaMsgBtnPanel.add(/*btnE, btnS,*/ btnX);
                txaMsgBtnPanel.setWidth("12%");
                txaMsgBtnPanel.getStyle().set("gap", "0.1rem");
                txaMsgBtnPanel.setMargin(false);
                txaMsgBtnPanel.setPadding(false);
                msgPanel.add(txaMsgBtnPanel);

//                btnE.addClickListener(btnee -> {
//                    btnE.setVisible(false);
//                    btnS.setVisible(true);
////                    txaMsg.setReadOnly(false);
////                    txaMsg.addTokenListener(event -> {
////                        String text = event.getToken();
////                        if (text != null) {
////                            List<String> selectedUsers = readers.stream().filter(u -> {
////                                return StringUtils.containsIgnoreCase(u, text.trim().replaceAll("[^\\p{L}\\p{Nd}]+", ""));
////                            }).collect(Collectors.toList());
////                            txaMsg.setOptions(selectedUsers);
////                        }
////                    });
//                });

//                btnS.addClickListener(btnse -> {
//                    btnE.setVisible(true);
//                    btnS.setVisible(false);
//                    txaMsg.setReadOnly(true);
//                    note.setMessage(txaMsg.getValue());
//                    
//                    msgService.
//                    //noteService.updateNoteMessage(note.getId(),txaMsg.getValue(),oidcUser.getPreferredUsername());
//                });

                btnX.addClickListener(btnxe -> {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Delete note");

                    dialog.setText(
                            "Are you sure you want to permanently delete this note?");

                    dialog.setCancelable(true);
                    dialog.addCancelListener(event -> dialog.close());

                    dialog.setConfirmText("Delete");
                    dialog.setConfirmButtonTheme("error primary");
                    dialog.addConfirmListener(event -> {
                        
//                        noteService.deleteNote(note,oidcUser.getPreferredUsername());
//                        List<Note> msgs = getNotesList.apply(doc, noteToBeHighlighted);
//                        vlNotesList.setItems(msgs);
                    });
                    dialog.open();
                });
            }

            //txaMsg.setReadOnly(true);
            txaMsg.setWidthFull();
            if (msg.getMessage()==null){
                txaMsg.setValue("");
            }else{
                txaMsg.setValue(msg.getMessage());
            }
            cardInfo.add(msgPanel);

            int index = Math.abs(name.hashCode() % 7 + 1);
            Avatar avUser = new Avatar();
            avUser.setName(msg.getFullName());
            avUser.setColorIndex(index);
            avUser.addThemeVariants(AvatarVariant.LUMO_XSMALL);

            card.add(avUser, cardInfo);
            card.setPadding(true);

//            if (note.getHighlighted() == true) {
//                card.addClassNames(LumoUtility.Background.WARNING_10);
//            }
            return card;
        });
    }

}
