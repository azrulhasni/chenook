/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.views.smefinancing;

import com.azrul.chenook.domain.Status;
import com.azrul.chenook.views.MainLayout;
import com.azrul.chenook.views.common.Card;
import com.azrul.smefinancing.domain.FinApplication;
import com.azrul.smefinancing.service.ApplicantService;
import com.azrul.chenook.service.MessageService;
import com.azrul.smefinancing.service.FinApplicationService;
import com.azrul.smefinancing.views.application.ApplicationForm;
import com.azrul.smefinancing.service.BadgeUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

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

    public ApplicationView(
            @Autowired FinApplicationService _finappService,
            @Autowired ApplicantService applicantService,
            @Autowired MessageService msgService,
            @Autowired BadgeUtils badgeUtils,
            @Value("${finapp.datetime.format}") String dateTimeFormat
    ) {
        this.finappService = _finappService;
        this.applicantService = applicantService;
        this.msgService=msgService;
        this.badgeUtils=badgeUtils;
        this.DATETIME_FORMAT = dateTimeFormat;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.DATETIME_FORMAT);

        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken oauth2AuthToken) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) oauth2AuthToken.getPrincipal();

            Grid<FinApplication> grid = new Grid<>(FinApplication.class, false);
            grid.getStyle().set("max-width","285px");
            grid.setAllRowsVisible(true);
            Button btnAddNew = new Button("Add new", e -> {
                FinApplication finapp = new FinApplication();
                finapp.setStatus(Status.NEWLY_CREATED);
                finapp.setApplicationDate(LocalDateTime.now());

                finapp = finappService.save(finapp, oidcUser.getPreferredUsername());
                showApplicationDialog(
                        finapp, 
                        oidcUser, 
                        fa -> grid.getDataProvider().refreshAll(),
                        fa -> grid.getDataProvider().refreshAll(),
                        fa -> grid.getDataProvider().refreshAll());
            });
            
            
            this.add(btnAddNew);
            this.add(grid);
            //grid.setSortableColumns("name", "email");
            grid.addComponentColumn(finapp -> {
                Card card = new Card("SME Loan: MYR " + finapp.getFinancingRequested());
                card.add(new NativeLabel("Application date: " + finapp.getApplicationDate().format(dateTimeFormatter)));
                card.add(finapp.getReasonForFinancing());

                HorizontalLayout btnPanel = new HorizontalLayout();
                btnPanel.add(new Button("See more", e -> {
                    showApplicationDialog(
                        finapp, 
                        oidcUser, 
                        fa -> grid.getDataProvider().refreshAll(),
                        fa -> grid.getDataProvider().refreshAll(),
                        fa -> grid.getDataProvider().refreshAll());
                }));
                
                card.add(btnPanel);

                if (null == finapp.getStatus()) {

                } else {
                    card.add(badgeUtils.createStatusBadge(finapp.getStatus()));
                }

                return card;
            });
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.setItems(finappService.getApplicationsByUsernameOrEmail(oidcUser.getPreferredUsername(), oidcUser.getEmail()));
        }
    }

    private void showApplicationDialog(
            FinApplication app,
            DefaultOidcUser oidcUser,
            Consumer<FinApplication> onPostSave,
            Consumer<FinApplication> onPostRemove,
            Consumer<FinApplication> onPostCancel
    ) {
        ApplicationForm appform = new ApplicationForm(
                app,
                oidcUser,
                applicantService,
                finappService,
                msgService,
                badgeUtils,
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
