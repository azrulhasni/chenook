/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.service.AttachmentService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
public class WorkflowPanel<T> extends VerticalLayout {
     @Autowired
    private AttachmentService attachmentService;

    public WorkflowPanel(
            Long parentId,
            String context,
            String fileLocation,
            Boolean editable,
            Consumer<Attachment> onPostSave,
            Consumer<Attachment> onPostRemove
    ) {
        ApplicationContextHolder.autowireBean(this);
    }
}
