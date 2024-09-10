/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.message;

//import com.azrul.chenook.autocomplete.Autocomplete;
import com.azrul.chenook.domain.Message;
import com.azrul.chenook.domain.MessageStatus;
import com.azrul.chenook.service.MessageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoIcon;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class MessageButton extends Button {
    
    public MessageButton(
            Long id,
            String context,
            OidcUser oidcUser, 
            MessageService msgService
    ) {
        Long unreadNotifCount = msgService.countMessagesByParentIdStatusAndContext(id, MessageStatus.NEW, context);
        
        Span counter = buildNotifCounter(unreadNotifCount);
        
        String counterLabel = String.format("%d unread messages", unreadNotifCount);
        
        Icon bell = LumoIcon.BELL.create();
        
        this.setId("btnMessage");
        this.setText("Messages");
        
        ContextMenu notifMenu = new ContextMenu();
        
        notifMenu.add(buildMessagePanel(id, context, oidcUser, msgService));
        notifMenu.setTarget(this);
        notifMenu.setOpenOnClick(true);
        notifMenu.addDetachListener(e -> {
            //if (e. == false) {
            Long unreadNotifCount2 = msgService.countMessagesByParentIdStatusAndContext(id, MessageStatus.NEW, context);
            
            Span counter2 = buildNotifCounter(unreadNotifCount2);
            this.setSuffixComponent(counter2);
            String counterLabel2 = String.format("%d unread messages", unreadNotifCount2);
            this.setTooltipText(counterLabel2);
            //}
        });
        
        this.setTooltipText(counterLabel);
        this.setSuffixComponent(counter);
        this.setIcon(bell);
        this.addThemeVariants(ButtonVariant.LUMO_SMALL);
    }
    
    private VerticalLayout buildMessagePanel(Long parentId, String context, OidcUser oidcUser, MessageService msgService) {
        VerticalLayout messagesPanel = new VerticalLayout();
        HorizontalLayout editor = new HorizontalLayout();
        TextField tfMsg = new TextField();
        Button btnAddMsg = new Button("Send");
        
        editor.add(tfMsg, btnAddMsg);
        // VirtualList<Message> vlMsgs = new VirtualList<Message>();
        Grid<Message> gMsgs = new Grid<>();        
        gMsgs.setItems(msgService.findMessagesByParentAndContext(parentId, context));
        gMsgs.addComponentColumn(msg -> buildCard(msg, oidcUser, gMsgs, msgService));
        messagesPanel.add(editor);
        messagesPanel.add(gMsgs);
        btnAddMsg.addClickListener(e -> {
            Message msg = new Message();
            msg.setContext(context);
            msg.setParentId(parentId);
            msg.setCreatedBy(oidcUser.getPreferredUsername());
            msg.setCreatedDateTime(LocalDateTime.now());
            msg.setFullName(oidcUser.getFullName());
            msg.setWriterUserName(oidcUser.getPreferredUsername());
            msg.setMessage(tfMsg.getValue());
            msg.setStatus(MessageStatus.NEW);
            msgService.save(msg);
            gMsgs.getDataProvider().refreshAll();
        });
        return messagesPanel;
    }

//    private MessageList buildMessageList(List<Message> messages) {
//        MessageList messageList = new MessageList();
//        List<MessageListItem> messageContainer = new ArrayList<>();
//        for (Message msg : messages) {
//            Instant createdDate = msg.getCreatedDateTime().atZone(ZoneOffset.systemDefault()).toInstant();
//
//            MessageListItem message = new MessageListItem(
//                    msg.getMessage(),
//                    createdDate, msg.getFullName() + " (" + msg.getWriterUserName() + ")");
//            messageContainer.add(message);
//
//        }
//        messageList.setItems(messageContainer);
//        return messageList;
//    }
    private Span buildNotifCounter(Long unreadNotifCount) {
        Span counter = new Span(String.valueOf(unreadNotifCount));
        counter.getElement().getThemeList().add("badge pill small contrast");
        counter.getStyle().set("margin-inline-start", "var(--lumo-space-s)");
        if (unreadNotifCount > 0) {
            counter.getStyle().set("background-color", "crimson");
            counter.getStyle().set("color", "white");
        }
        return counter;
    }
    
    private Component buildCard(Message msg, OidcUser oidcUser, Grid<Message> gMsgs, MessageService msgService) {
        HorizontalLayout card = new HorizontalLayout();
        VerticalLayout cardInfo = new VerticalLayout();
        cardInfo.setPadding(false);
        cardInfo.setSpacing(false);
        // cardInfo.add(new Text("[#"+note.getId()+"] "));
        //cardInfo.add(new Text("[#"+msg.getId()+"] "));
        String name = msg.getFullName();
        if (msg.getWriterUserName() != null && !msg.getWriterUserName().isEmpty()) {
            name = name + " (" + msg.getWriterUserName() + ")";
        }
        HorizontalLayout nameLine = new HorizontalLayout();
        NativeLabel txtName = new NativeLabel(name);
        txtName.getStyle().set("fontWeight", "bold");
        nameLine.add(new Text("[#" + msg.getId() + "] "), txtName, new Text(" "));
        cardInfo.add(nameLine);
        cardInfo.add(new NativeLabel(msg.getCreatedDateTime().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss"))));
        HorizontalLayout msgPanel = new HorizontalLayout();
        msgPanel.setWidth("100%");
        TextArea txaMsg = new TextArea();
        txaMsg.setValue(msg.getMessage());
        txaMsg.setId("txaMsg");
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
            
            txaMsgBtnPanel.add(/*btnE, btnS,*/btnX);
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
        if (msg.getMessage() == null) {
            txaMsg.setValue("");
        } else {
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
    }
}
