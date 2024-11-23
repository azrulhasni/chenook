/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.function.BiConsumer;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 * @param <T>
 */
public class WorkflowCreatePanel<T extends WorkItem> extends VerticalLayout{
    private final WorkflowConfig workflowConfig;
    private final WorkflowService<T> workflowService;
    
    private WorkflowCreatePanel(
            @Autowired WorkflowConfig workflowConfig,
            @Autowired WorkflowService<T> workflowService
    ){
        this.workflowConfig=workflowConfig;
        this.workflowService=workflowService;
    }
    
    public static <T extends WorkItem> WorkflowCreatePanel<T> create(
            final Class<T> workItemClass,  
            final BizUser bizUser,
            final BizProcess bizProcess,
            final BiConsumer<StartEvent,WorkflowCreatePanel<T>> showCreationDialog){
        WorkflowCreatePanel<T> panel = ApplicationContextHolder.getBean(WorkflowCreatePanel.class);
        panel.init( bizUser,bizProcess, showCreationDialog);
        return panel;
    }
    
    private void init(
            final BizUser bizUser,
            final BizProcess bizProcess,
            final BiConsumer<StartEvent, WorkflowCreatePanel<T>> showCreationDialog
    ) {
        this.setWidth("-webkit-fill-available");

        List<StartEvent> startEvents = workflowService.whatUserCanStart(bizUser.getClientRoles(), bizProcess);
        if (!startEvents.isEmpty()) {
            MenuBar menu = new MenuBar();

            for (var startEvent : startEvents) {
                MenuItem miAddNew = menu.addItem("Add new " + startEvent.getDescription(), e -> {
                    showCreationDialog.accept(startEvent,this);

                });
                miAddNew.setId("new_"+startEvent.getId());
            }
            menu.setWidth("-webkit-fill-available");
            this.add(menu);
        }
     }
}
