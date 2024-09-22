/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.views.common.Card;
import com.azrul.chenook.views.common.PageNav;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class MyWorkPanel<T extends WorkItem> extends VerticalLayout {

    private final int COUNT_PER_PAGE = 3;
    private final Pair<Grid<T>, PageNav> myCreatedWork;
    private final Pair<Grid<T>, PageNav> myOwnedWork;
    private final FinApplicationService finappService;
    private final OidcUser oidcUser;
    private final Map<String, String> sortableFields;
    private final BadgeUtils badgeUtils;

    public MyWorkPanel(
            final Class workItemClass,  
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final Map<String, String> sortableFields,
            final FinApplicationService finappService,
            final BadgeUtils badgeUtils,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder
    ) {

        this.oidcUser = oidcUser;
        this.sortableFields = sortableFields;
        this.finappService = finappService;
        this.badgeUtils = badgeUtils;
        this.setWidth("-webkit-fill-available");

        List<StartEvent> startEvents = finappService.whatUserCanStart(oidcUser, bizProcess);
        if (!startEvents.isEmpty()) {
            MenuBar menu = new MenuBar();

            for (var startEvent : startEvents) {
                menu.addItem("Add new " + startEvent.getDescription(), e -> {
                    showCreationDialog.accept(this, startEvent);

                });
            }
            menu.setWidth("-webkit-fill-available");
            this.add(menu);
            myCreatedWork = buildDataPanel(
                    "Work items I've created",
                    oidcUser,
                    bizProcess,
                    showCreationDialog,
                    showUpdateDialog,
                    cardBuilder,
                    sortableFields,
                    (username) -> finappService.countWorkByCreator(username),
                    (username, nav) -> finappService.getWorkByCreator(username, nav)
            );
            addPair(myCreatedWork);
        }else{
            myCreatedWork = null;
        }

        myOwnedWork = buildDataPanel(
                "My work items",
                oidcUser,
                bizProcess,
                showCreationDialog,
                showUpdateDialog,
                cardBuilder,
                sortableFields,
                (username) -> finappService.countWorkByOwner(username),
                (username, nav) -> finappService.getWorkByOwner(workItemClass, username, nav)
        );
        addPair(myOwnedWork);

    }

    private void addPair(Pair<Grid<T>, PageNav> pair) {
        this.add(pair.getSecond());
        this.add(pair.getFirst());
    }

    private Pair<Grid<T>, PageNav> buildDataPanel(
            final String title,
            final OidcUser oidcUser1,
            final BizProcess bizProcess,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder,
            final Map<String, String> sortableFields1,
            final Function<String, Integer> counter,
            final BiFunction<String, PageNav, DataProvider> dataProviderCreator) {
        PageNav nav = new PageNav();
        Integer count = counter.apply(oidcUser1.getPreferredUsername());//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = dataProviderCreator.apply(oidcUser1.getPreferredUsername(), nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title, oidcUser1, bizProcess, dataProvider, showCreationDialog, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields1, false);
        Pair<Grid<T>, PageNav> pair = Pair.of(grid, nav);
        return pair;
    }

    public void refresh() {
        myCreatedWork.getFirst().getDataProvider().refreshAll();
        Integer countWorkByCreator = finappService.countWorkByCreator(oidcUser.getPreferredUsername());
        myCreatedWork.getSecond().refreshPageNav(countWorkByCreator);
        
        myOwnedWork.getFirst().getDataProvider().refreshAll();
        Integer countMyOwnedWork = finappService.countWorkByOwner(oidcUser.getPreferredUsername());
        myOwnedWork.getSecond().refreshPageNav(countMyOwnedWork);
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final DataProvider dataProvider,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H3 title = new H3(panelTitle);
        this.add(title);
        grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work-> {
            VerticalLayout content = cardBuilder.apply(work);
            Span badge = badgeUtils.createStatusBadge(work.getStatus());
            Card card = new Card(work.getTitle(), badge);
            card.add(content);
            HorizontalLayout btnPanel = new HorizontalLayout();
            btnPanel.add(new Button("See more", e -> {
                showUpdateDialog.accept(this, null, work);
            }));
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(dataProvider);
        return grid;
    }

}
