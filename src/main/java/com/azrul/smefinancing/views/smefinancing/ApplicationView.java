/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.views.smefinancing;

import com.azrul.chenook.views.MainLayout;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.chenook.service.MessageService;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.service.ApprovalService;
import com.azrul.chenook.views.workflow.MyWorkPanel;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.application.ApplicationForm;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.MapperService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.workflow.WorklistPanel;
import com.azrul.smefinancing.views.applicant.ApplicantForm;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.lang.reflect.Field;
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
    private final ApplicantService applicantService;
    private final MessageService msgService;
    private final BadgeUtils badgeUtils;
    private final String DATETIME_FORMAT;
    private final WorkflowConfig workflowConfig;
    private final ApprovalService approvalService;
    private final BizUserService bizUserService;
    private final MapperService basicMapper;
    private final ApplicationForm applicationForm;
    private final MyWorkPanel<FinApplication> myWorkPanel;
    private final WorklistPanel<FinApplication> worklistPanel;

    public ApplicationView(
            @Autowired FinApplicationService finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired BadgeUtils badgeUtils,
            @Autowired WorkflowConfig workflowConfig,
            @Autowired ApprovalService approvalService,
            @Autowired BizUserService bizUserService,
            @Autowired MapperService basicMapper,
            @Autowired MyWorkPanel<FinApplication> myWorkPanel,
            @Autowired WorklistPanel<FinApplication> worklistPanel,
            //@Autowired ApplicationForm applicationForm,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = finappService;
        this.applicantService = applicantService;
        this.msgService = msgService;
        this.badgeUtils = badgeUtils;
        this.DATETIME_FORMAT = dateTimeFormat;
        this.workflowConfig = workflowConfig;
        this.approvalService=approvalService;
        this.bizUserService=bizUserService;
        this.basicMapper = basicMapper;
        this.myWorkPanel=myWorkPanel;
        this.worklistPanel=worklistPanel;
        this.applicationForm=applicationForm;
        
        

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);
        final BizProcess bizProcess = workflowConfig.rootBizProcess();
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken oauth2AuthToken) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) oauth2AuthToken.getPrincipal();
            
            Map<String,String> fieldNameDisplayNameMap = WorkflowUtils.getFieldNameDisplayNameMap(FinApplication.class);
            myWorkPanel.init(
                    FinApplication.class,
                    oidcUser,
                    (wp, startEvent) -> {
                        FinApplication finapp = new FinApplication();
                        finapp.setApplicationDate(LocalDateTime.now());
                        finapp = finappService.init(finapp, oidcUser, "SME_FIN", startEvent, bizProcess);
                        showApplicationDialog(
                                startEvent,
                                finapp,
                                oidcUser,
                                "SME_FIN",
                                fa -> wp.refresh(),
                                fa -> wp.refresh(),
                                fa -> wp.refresh());
                    },
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
                    finapp -> {
                        VerticalLayout content = new VerticalLayout();
                        content.setSpacing(false);
                        content.setPadding(false);
                        if (finapp.getApplicationDate()!=null){
                            content.add(new NativeLabel(fieldNameDisplayNameMap.get("applicationDate")+": " + dateTimeFormatter.format(finapp.getApplicationDate())));
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
            );
            this.add(myWorkPanel);
             
            worklistPanel.init(
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
                    finapp -> {
                        VerticalLayout content = new VerticalLayout();
                        content.setSpacing(false);
                        content.setPadding(false);
                        if (finapp.getApplicationDate()!=null){
                            content.add(new NativeLabel("Application date: " + dateTimeFormatter.format(finapp.getApplicationDate())));
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
            );
            this.add(worklistPanel);
        }
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
        applicationForm.init(
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
        //   throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private Icon createIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");
        return icon;
    }

}
