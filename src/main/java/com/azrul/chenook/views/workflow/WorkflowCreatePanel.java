/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 * @param <T>
 */
public class WorkflowCreatePanel<T extends WorkItem> extends VerticalLayout{
    private final FinApplicationService finappService;
    private final WorkflowConfig workflowConfig;
    
    private WorkflowCreatePanel(
            @Autowired WorkflowConfig workflowConfig,
            @Autowired  FinApplicationService finappService
    ){
         this.finappService = finappService;
        this.workflowConfig=workflowConfig;
    }
    
    public static <T extends WorkItem> WorkflowCreatePanel create(
            final Class workItemClass,  
            final OidcUser oidcUser,
            final BiConsumer<StartEvent,WorkflowCreatePanel> showCreationDialog){
        WorkflowCreatePanel panel = ApplicationContextHolder.getBean(WorkflowCreatePanel.class);
        panel.init( oidcUser, showCreationDialog);
        return panel;
    }
    
    private void init(
            final OidcUser oidcUser,
            final BiConsumer<StartEvent, WorkflowCreatePanel> showCreationDialog
    ) {

        //this.oidcUser = oidcUser;
        BizProcess bizProcess = workflowConfig.rootBizProcess();
       
        //Map<String,String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);
        
        this.setWidth("-webkit-fill-available");

        List<StartEvent> startEvents = finappService.whatUserCanStart(oidcUser, bizProcess);
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
