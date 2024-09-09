/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.views.admin;

import com.azrul.chenook.service.MessageService;
import com.azrul.chenook.views.MainLayout;
import com.azrul.chenook.views.common.PageNav;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.application.ApplicationForm;
import com.azrul.smefinancing.service.BadgeUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 *
 * @author azrul
 */
@PageTitle("My Financing Application:: Administration")
@Route(value = "adminview", layout = MainLayout.class)
@RolesAllowed("FINAPP_ADMIN")
public class AdminView extends VerticalLayout{
    private final FinApplicationService finappService;
    private final ApplicantService applicantService;
    private final MessageService msgService;
    private final WorkflowService workflowService;
    private final String DATETIME_FORMAT;
    private final int COUNT_PER_PAGE = 3;
    private final BadgeUtils badgeUtils;
    private final WorkflowConfig workflowConfig;

    public AdminView(
            @Autowired FinApplicationService finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired WorkflowService workflowService,
            @Autowired BadgeUtils badgeUtils,
            @Autowired WorkflowConfig workflowConfig,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = finappService;
        this.applicantService = applicantService;
        this.workflowService = workflowService;
        this.msgService=msgService;
        this.DATETIME_FORMAT = dateTimeFormat;
        this.badgeUtils=badgeUtils;
        this.workflowConfig=workflowConfig;

        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);

        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken oauth2AuthToken) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) oauth2AuthToken.getPrincipal();
            
            PageNav pageNav = new PageNav();
            Grid<FinApplication> grid = new Grid<>();
            
            Long count = finappService.countFinApplications();
            DataProvider dataProvider = finappService.buildFinApplicationsDataProvider(pageNav);
//            pageNav.init(dataProvider, count.intValue(), COUNT_PER_PAGE,"id", false);
            grid.setDataProvider(dataProvider);
            
            grid.addColumn(FinApplication::getId).setHeader("Id").setSortProperty("id").setSortable(true);
            grid.addColumn(FinApplication::getName).setHeader("Business Name").setSortProperty("name").setSortable(true);
            grid.addColumn(FinApplication::getCreationDate).setHeader("Creation Date").setSortProperty("creationDate").setSortable(true);
//            grid.addComponentColumn(finapp->{
//                return badgeUtils.createStatusBadge(finapp.getStatus());
//            }).setComparator(finapp->finapp.getStatus()).setHeader("Status").setSortProperty("status").setSortable(true);
//            grid.addComponentColumn(finapp->{
//                return new Button("Open", e->{
//                        ApplicationForm appform = new ApplicationForm(
//                            finapp,
//                            oidcUser,
//                            applicantService,
//                            finappService,
//                            workflowService,
//                            msgService,
//                            badgeUtils,
//                            workflowConfig,
//                            fa->grid.getDataProvider().refreshAll(),
//                            fa->grid.getDataProvider().refreshAll(),
//                            fa->grid.getDataProvider().refreshAll()
//                        );
//                        appform.open();
//                });
//            });
//                    
//            grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
//            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//            this.add(pageNav);
//            this.add(grid);
        }
    }
}
