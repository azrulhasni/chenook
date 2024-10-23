/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.views.smefinancing;

import com.azrul.chenook.views.MainLayout;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.views.workflow.MyOwnWorkPanel;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.application.ApplicationForm;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.workflow.MyCreatedWorkPanel;
import com.azrul.chenook.views.workflow.WorkflowCreatePanel;
import com.azrul.chenook.views.workflow.WorklistPanel;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
/**
 *
 * @author azrul
 */
@PageTitle("My Financing Application")
@Route(value = "appview", layout = MainLayout.class)
@RolesAllowed("FINAPP_USER")
public class ApplicationView extends VerticalLayout implements AfterNavigationObserver/*, HasUrlParameter<Long> */ {

    private final FinApplicationService finappService;
    private final String DATETIME_FORMAT;
    private final WorkflowConfig workflowConfig;

    public ApplicationView(
            @Autowired FinApplicationService finappService,
            @Autowired WorkflowConfig workflowConfig,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = finappService;
        this.DATETIME_FORMAT = dateTimeFormat;
        this.workflowConfig = workflowConfig;
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);
        final BizProcess bizProcess = this.workflowConfig.rootBizProcess();
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken oauth2AuthToken &&
             oauth2AuthToken.getPrincipal() instanceof DefaultOidcUser oidcUser ) {
            
            Map<String,String> fieldNameDisplayNameMap = WorkflowUtils
                    .getFieldNameDisplayNameMap(FinApplication.class);
            
            var myOwnWorkPanel = MyOwnWorkPanel.create(
                    FinApplication.class,
                    oidcUser,
                    (wp, startEvent, finapp) -> {
                        showApplicationDialog(
                                startEvent,
                                finapp,
                                oidcUser,
                                "SME_FIN",
                                fa -> wp.refresh(),
                                fa -> wp.refresh(),
                                fa -> wp.refresh());
                    },
                    finapp -> createCard(finapp, fieldNameDisplayNameMap, dateTimeFormatter)
            );
            var myCreatedWorkPanel = MyCreatedWorkPanel.create(
                    FinApplication.class,
                    oidcUser,
                    (wp, startEvent, finapp) -> {
                        showApplicationDialog(
                                startEvent,
                                finapp,
                                oidcUser,
                                "SME_FIN",
                                fa -> wp.refresh(),
                                fa -> wp.refresh(),
                                fa -> wp.refresh());
                    },
                    finapp -> createCard(finapp, fieldNameDisplayNameMap, dateTimeFormatter)
            );
            var worklistPanel = WorklistPanel.create(
                    FinApplication.class,
                    oidcUser,
                    (wp, startEvent, finapp) -> {
                        showApplicationDialog(
                                startEvent,
                                finapp,
                                oidcUser,
                                "SME_FIN",
                                fa -> wp.refresh(),
                                fa -> wp.refresh(),
                                fa -> wp.refresh());
                    },
                    finapp -> createCard(finapp, fieldNameDisplayNameMap, dateTimeFormatter)
            );
            
            
            var workflowCreatePanel = WorkflowCreatePanel.create(
                    FinApplication.class,
                    oidcUser, 
                    (startEvent,wp) -> {
                        FinApplication finapp = new FinApplication();
                        finapp.setApplicationDate(LocalDateTime.now());
                        finapp = this.finappService.initializeAndSave(
                                finapp, 
                                oidcUser, 
                                "SME_FIN", 
                                startEvent, 
                                bizProcess
                        );
                        showApplicationDialog(
                                startEvent,
                                finapp,
                                oidcUser,
                                "SME_FIN",
                                fa -> myCreatedWorkPanel.refresh(),
                                fa -> myCreatedWorkPanel.refresh(),
                                fa -> myCreatedWorkPanel.refresh());
                    }
                    
            );
            
            
            
            this.add(workflowCreatePanel);
            TabSheet tabSheet = new TabSheet();
            
            myOwnWorkPanel.setWidth("28em");
            Tab tab1 = tabSheet.add("My work",myOwnWorkPanel);
            tab1.setId("tabMyWork");
            
            myCreatedWorkPanel.setWidth("28em");
            Tab tab2 = tabSheet.add("Work I've created",myCreatedWorkPanel);
            tab2.setId("tabWorkICreated");
            
            worklistPanel.setWidth("28em");
            Tab tab3 = tabSheet.add("Work list",worklistPanel);
            tab3.setId("tabWorkList");
            
            this.add(tabSheet);
        }
    }

    private VerticalLayout createCard(
            FinApplication finapp, 
            Map<String, String> fieldNameDisplayNameMap, 
            DateTimeFormatter dateTimeFormatter) {
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        if (finapp.getApplicationDate()!=null){
            content.add(
                    new NativeLabel(
                            fieldNameDisplayNameMap.get("applicationDate")
                                    +": "
                                    + dateTimeFormatter.format(
                                            finapp.getApplicationDate()
                                    )
                    )
            );
        }
        TextArea reason = new TextArea();
        reason.setValue(finapp.getReasonForFinancing()!=null
                ?finapp.getReasonForFinancing()
                :"");
        reason.setWidthFull();
        reason.setMaxHeight("60px");
        reason.setReadOnly(true);
        content.add(reason);
        
        return content;
    }

    private void showApplicationDialog(
            StartEvent startEvent,
            FinApplication work,
            OidcUser user,
            String context,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        var applicationForm = ApplicationForm.create(
                startEvent, 
                work, 
                user, 
                context, 
                onPostSave, 
                onPostRemove, 
                onPostCancel);
        
        applicationForm.open();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
    }

    private Icon createIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");
        return icon;
    }

}
