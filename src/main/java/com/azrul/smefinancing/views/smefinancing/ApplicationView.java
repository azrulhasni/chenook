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
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.views.common.Card;
import com.azrul.chenook.views.workflow.MyWorkPanel;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.application.ApplicationForm;
import com.azrul.smefinancing.service.BadgeUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
    private final ApplicantService applicantService;
    private final MessageService msgService;
    private final BadgeUtils badgeUtils;
    private final String DATETIME_FORMAT;
    private final WorkflowConfig workflowConfig;

    public ApplicationView(
            @Autowired FinApplicationService finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired BadgeUtils badgeUtils,
            @Autowired WorkflowConfig workflowConfig,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = finappService;
        this.applicantService = applicantService;
        this.msgService = msgService;
        this.badgeUtils = badgeUtils;
        this.DATETIME_FORMAT = dateTimeFormat;
        this.workflowConfig = workflowConfig;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);

        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken oauth2AuthToken) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) oauth2AuthToken.getPrincipal();
             Map<String,String> sortableFields = Map.of(
                "id","id"
                
            );
            MyWorkPanel<FinApplication> workPanel = new MyWorkPanel<FinApplication>(
                    oidcUser,
                    workflowConfig.rootBizProcess(),
                    sortableFields,
                    finappService,
                    (wp, startEvent) -> {
                        FinApplication finapp = new FinApplication();
                        finapp.setApplicationDate(LocalDateTime.now());
                        finapp = finappService.save(finapp, oidcUser.getPreferredUsername());
//                        WorkflowMemento<FinApplication> memento = new WorkflowMemento<>(
//                                finapp,
//                                finapp.getId(),
//                                oidcUser,
//                                workflowConfig.rootBizProcess(),
//                                "SME_FIN");
                        showApplicationDialog(
                                //memento,
                                startEvent,
                                null,
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
                        content.add(new NativeLabel("Application date: " + finapp.getApplicationDate()));
                        TextArea reason = new TextArea();
                        reason.setValue(finapp.getReasonForFinancing());
                        reason.setWidthFull();
                        reason.setMaxHeight("60px");
                        reason.setReadOnly(true);
                        content.add(reason);
                        return content;
                    }
            );
            this.add(workPanel);
            //grid.setItems(finappService.getApplicationsByUsernameOrEmail(oidcUser.getPreferredUsername(), oidcUser.getEmail()));
        }
    }

//    private Grid<WorkItem> createMyWorkflowPanel(
//            final OidcUser oidcUser,
//            final WorkflowService workflowService,
//            final Consumer<Grid> showCreationDialog,
//            final BiConsumer<Grid, WorkItem> showUpdateDialog) {
//        Grid<WorkItem> grid = new Grid<>(WorkItem.class, false);
//        grid.getStyle().set("max-width", "285px");
//        grid.setAllRowsVisible(true);
//        Button btnAddNew = new Button("Add new", e -> {
//            showCreationDialog.accept(grid);
//        });
//        this.add(btnAddNew);
//        this.add(grid);
//        grid.addComponentColumn(work -> {
//            Card card = new Card(work.getFields().get("TITLE")+": MYR " + work.getFields().get("FINANCING_REQUESTED"));
//            card.add(new NativeLabel("Application date: " + work.getFields().get("APPLICATION_DATE")));
//            card.add(work.getFields().get("REASON_FOR_FINANCING"));
//            HorizontalLayout btnPanel = new HorizontalLayout();
//            btnPanel.add(new Button("See more", e -> {
//                showUpdateDialog.accept(grid,work);
//            }));
//            card.add(btnPanel);
//            return card;
//        });
//        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
//        grid.setItems(workflowService.getWorkByCreator(oidcUser.getPreferredUsername()));
//        return grid;
//    }
    private void showApplicationDialog(
            StartEvent startEvent,
            FinApplication work,
            OidcUser oidcUser,
            String context,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        ApplicationForm appform = new ApplicationForm(
                startEvent,
                work,
                oidcUser,
                DATETIME_FORMAT,
                context,
                applicantService,
                finappService,
                msgService,
                badgeUtils,
                workflowConfig,
                onPostSave,
                onPostRemove,
                onPostCancel
        );

        appform.open();
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
