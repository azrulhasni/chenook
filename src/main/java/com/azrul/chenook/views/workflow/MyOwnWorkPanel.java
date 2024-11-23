/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.azrul.chenook.views.common.function.TriFunction;
import com.vaadin.flow.component.Component;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
@SpringComponent
public class MyOwnWorkPanel<T extends WorkItem> extends VerticalLayout {

    private Triple<SearchTermProvider, PageNav, Grid<T>> myOwnWork;
    private BizUser user;
    private BiFunction<String, SearchTermProvider, Integer> counter;
    private TriFunction<String, SearchTermProvider, PageNav, DataProvider<T, Void>> dataProviderCreator;

    private final WorkflowService<T> workflowService;
    private final int COUNT_PER_PAGE = 3;
    private final WorkflowConfig workflowConfig;

    public static <T extends WorkItem> MyOwnWorkPanel<T> create(
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final TriConsumer<MyOwnWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        var myWorkPanel = ApplicationContextHolder.getBean(MyOwnWorkPanel.class);
        myWorkPanel.init(
                workItemClass,
                user,
                bizProcess,
                showUpdateDialog,
                cardBuilder);
        return myWorkPanel;
    }

    private MyOwnWorkPanel(
            @Autowired WorkflowConfig workflowConfig,
            @Autowired WorkflowService<T> finappService
    ) {
        this.workflowService = finappService;
        this.workflowConfig = workflowConfig;

    }

    public void init(
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final TriConsumer<MyOwnWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder
    ) {

        this.user = user;
        this.counter = (username, searchTermProvider) -> workflowService.countWorkByOwner(workItemClass, username, searchTermProvider);
        this.dataProviderCreator = (username, searchTermProvider, nav) -> workflowService.getWorkByOwner(workItemClass, username, searchTermProvider, nav);

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);

        this.setWidth("-webkit-fill-available");

        myOwnWork = buildDataPanel(
                "My work items",
                "btnMyWork",
                "mywork",
                user,
                bizProcess,
                //workflowConfig.rootBizProcess(),
                showUpdateDialog,
                cardBuilder,
                sortableFields
        );
        add(myOwnWork);

    }

    private void add(Triple<SearchTermProvider, PageNav, Grid<T>> triple) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("40em");
        layout.getStyle().set("border", "1px solid lightgrey");
        layout.getStyle().set("border-radius", "25px");
        layout.add((Component) triple.getLeft());
        layout.add(triple.getMiddle());
        layout.add(triple.getRight());
        this.add(layout);
    }

    private Triple<SearchTermProvider, PageNav, Grid<T>> buildDataPanel(
            final String title,
            final String btnIdDiscriminator,
            final String panelIdDiscriminator,
            final BizUser user,
            final BizProcess bizProcess,
            final TriConsumer<MyOwnWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder,
            final Map<String, String> sortableFields
    ) {
        SearchPanel searchPanel = new SearchPanel(panelIdDiscriminator);
        searchPanel.searchRunner(s->refresh());
        PageNav nav = new PageNav();
        Integer count = counter.apply(user.getUsername(), searchPanel);//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider<T, Void> dataProvider = dataProviderCreator.apply(user.getUsername(), searchPanel, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title, btnIdDiscriminator, user, bizProcess, dataProvider, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);

        Triple<SearchTermProvider, PageNav, Grid<T>> triple = Triple.of(searchPanel, nav, grid);
        return triple;
    }

    public void refresh() {

        if (myOwnWork != null) {
            Integer countMyOwnedWork = counter.apply(user.getUsername(), myOwnWork.getLeft());
            myOwnWork.getMiddle().refresh(countMyOwnedWork);
            myOwnWork.getRight().getDataProvider().refreshAll();
            
        }
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final String btnIdDiscriminator,
            final BizUser user,
            final BizProcess bizProcess,
            final DataProvider<T, Void> dataProvider,
            final TriConsumer<MyOwnWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H4 title = new H4(panelTitle);
        this.add(title);
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work -> {
            Card card =  cardBuilder.apply(work);
            HorizontalLayout btnPanel = new HorizontalLayout();
            Button btnUpdate = new Button("See more", e -> {
                showUpdateDialog.accept(this, null, work);
            });
            btnUpdate.setId(btnIdDiscriminator + work.getId());
            btnPanel.add(btnUpdate);
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(dataProvider);
        return grid;
    }

}
