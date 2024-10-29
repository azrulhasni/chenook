package com.azrul.chenook;

import com.azrul.chenook.views.MainLayout;
import com.azrul.smefinancing.views.landing.LandingPage;
import com.azrul.smefinancing.views.smefinancing.ApplicationView;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;

@EnableVaadin({"com.azrul.chenook.views.*","com.azrul.smefinancing.views.*"})
@Theme(value = "chenook")
public class AppShell implements AppShellConfigurator{
        @Override
    public void configurePage(AppShellSettings settings) {
        settings.setViewport("width=device-width, initial-scale=1");
        settings.setPageTitle("SME Financing");
        settings.addMetaTag("author", "Azrul Hasni MADISA");
//        settings.addFavIcon("icon", "icons/chenook-favicon.png", "192x192");
//        settings.addLink("shortcut icon", "icons/chenook-favicon.ico");
        if (!RouteConfiguration.forSessionScope().isPathAvailable("appview")){
            RouteConfiguration.forSessionScope().setRoute(
                "appview",
                ApplicationView.class,
                MainLayout.class);
        }
        if (!RouteConfiguration.forSessionScope().isPathAvailable("")){
            RouteConfiguration.forSessionScope().setRoute(
                "",
                LandingPage.class,
                MainLayout.class);
        }
        /*if (!RouteConfiguration.forSessionScope().isPathAvailable("about")){
            RouteConfiguration.forSessionScope().setRoute(
                "appview",
                About.class,
                MainLayout.class);
        }*/



    }

}