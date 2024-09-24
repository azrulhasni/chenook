package com.azrul.chenook.views;

import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.service.MapperService;
import com.azrul.chenook.views.about.AboutView;
import com.azrul.chenook.views.common.UserField;
import com.azrul.smefinancing.views.admin.AdminView;
import com.azrul.smefinancing.views.smefinancing.ApplicationView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
//@Uses(Autocomplete.class)
public class MainLayout extends AppLayout {

    private H2 viewTitle;
    private final String BETWEEN_BRACKETS = "\\((.*?)\\)";

    private Pattern userPattern = Pattern.compile("(?i:(?<=uid=)).*?(?=,[A-Za-z]{0,2}=|$)", Pattern.CASE_INSENSITIVE);

    public MainLayout() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken token
                && token.getPrincipal() instanceof DefaultOidcUser oidcUser) {

            setPrimarySection(Section.DRAWER);
            addDrawerContent(oidcUser);
            addHeaderContent();

            this.setDrawerOpened(false);
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        addToNavbar(true, toggle, viewTitle);

    }

    private void addDrawerContent(OidcUser oidcUser) {
        H1 appName = new H1("SME Financing");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation(oidcUser));
        addToDrawer(header, scroller, createFooter(oidcUser));
    }

    private SideNav createNavigation(OidcUser oidcUser) {
        try {
            SideNav nav = new SideNav();
            nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));
            if (oidcUser.getAuthorities().stream().anyMatch(
                    sga -> StringUtils.equals(sga.getAuthority(), "ROLE_FINAPP_USER"))) {
                nav.addItem(new SideNavItem("SME applications", ApplicationView.class, LineAwesomeIcon.FILE.create()));
            }
            if (oidcUser.getAuthorities().stream().anyMatch(
                    sga -> StringUtils.equals(sga.getAuthority(), "ROLE_FINAPP_ADMIN"))) {
                nav.addItem(new SideNavItem("Admin", AdminView.class, LineAwesomeIcon.FILE.create()));
            }
            return nav;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Footer createFooter(OidcUser oidcUser) {
        Footer layout = new Footer();
        if (oidcUser != null) {

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            UserField userField = new UserField(mapUser(oidcUser), true);

            div.add(userField);
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                UI.getCurrent().getPage().setLocation("/logout");
            });

            layout.add(userMenu);
        } else {
            Button login = new Button("Sign in",
                    event -> UI.getCurrent().getPage().setLocation("/feed"));
            layout.add(login);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    private BizUser mapUser(OidcUser oidcUser) {
//        BizUser user = new BizUser();
//        user.setEmail(oidcUser.getEmail());
//        user.setFirstName(oidcUser.getGivenName());
//        user.setLastName(oidcUser.getFamilyName());
//        user.setUsername(oidcUser.getPreferredUsername());
//        return user;
        BizUser bizUser = new BizUser();

        List<String> roles = oidcUser
                .getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .map(String::toLowerCase)
                .map(a -> a.replace("role_", ""))
                .collect(Collectors.toList());
        bizUser.setUsername(oidcUser.getPreferredUsername());
        bizUser.setClientRoles(roles);
        bizUser.setEmail(oidcUser.getEmail());
        bizUser.setEnabled(Boolean.TRUE);
        bizUser.setFirstName(oidcUser.getGivenName());

        bizUser.setLastName(oidcUser.getFamilyName());
        String manager = oidcUser.getAttribute("manager");
        setManager(manager, bizUser);
        return bizUser;
    }

    private void setManager(String manager, BizUser bizUser) {
        if (manager == null) {
            return;
        }
        if (manager.contains("uid=")) {//ldap exprreession
            Matcher matcher = userPattern.matcher(manager);
            if (matcher.find()) {
                bizUser.setManager(matcher.group(0));
            }
        } else {
            bizUser.setManager(manager);
        }
    }
}
