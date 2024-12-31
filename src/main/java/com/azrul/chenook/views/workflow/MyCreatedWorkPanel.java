/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.function.TriFunction;
import com.azrul.chenook.workflow.model.BizProcess;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */

@SpringComponent
public class MyCreatedWorkPanel<T extends WorkItem> extends VerticalLayout {

    private GridMemento<T> myCreatedWork;
    private BizUser user;

    private       WorkflowService<T> workflowService;
    private final int COUNT_PER_PAGE = 3;
    //private final BadgeUtils badgeUtils;
    private final WorkflowConfig workflowConfig;

    public static <T extends WorkItem> MyCreatedWorkPanel<T> create(
            final WorkflowService<T> workflowService,
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final BiConsumer<MyCreatedWorkPanel<T>,  T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        MyCreatedWorkPanel<T> myCreatedWorkPanel = ApplicationContextHolder.getBean(MyCreatedWorkPanel.class);
        myCreatedWorkPanel.init(
                workflowService,
                workItemClass,
                user,
                bizProcess,
                showUpdateDialog,
                cardBuilder);
        return myCreatedWorkPanel;
    }

    private MyCreatedWorkPanel(
            @Autowired WorkflowConfig workflowConfig
    ) {
        this.workflowConfig = workflowConfig;

    }


    public void init(
            final WorkflowService<T> workflowService,
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final BiConsumer<MyCreatedWorkPanel<T>,  T> showUpdateDialog,
            final Function<T, Card> cardBuilder
    ) {

        this.user = user;
        this.workflowService = workflowService;
        BiFunction<String, SearchTermProvider, Integer>  counter = (username, searchTermProvider) -> workflowService.countWorkByCreator(workItemClass, username, searchTermProvider);
        TriFunction<String, SearchTermProvider, PageNav, DataProvider<T, Void>> dataProviderCreator = (username, searchTermProvider, nav) -> workflowService.getWorkByCreator(workItemClass, username, searchTermProvider, nav);

 
        this.setWidth("-webkit-fill-available");


        myCreatedWork = WorkflowAwareGridBuilder.build(
                    "btnMyWork",
                    workItemClass,
                    m->counter.apply(user.getUsername(), m.getSearchPanel()),
                    m->dataProviderCreator.apply(user.getUsername(), m.getSearchPanel(), m.getPageNav()),
                    Optional.of(()->createGrid(workItemClass,"My work items", "btnMyWork", showUpdateDialog, cardBuilder))
        );
        add(myCreatedWork);

    }

    private void add(GridMemento<T> memento) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("40em");
        layout.getStyle().set("border", "1px solid lightgrey");
        layout.getStyle().set("border-radius", "25px");
        layout.add(memento.getSearchPanel());
        layout.add(memento.getPageNav());
        layout.add(memento.getGrid());
        this.add(layout);
    }


    public void refresh() {
        if (myCreatedWork != null) {
            myCreatedWork.refresh();
        }
    }

    private Grid<T> createGrid(
            final Class<T> workItemClass,
            final String panelTitle,
            final String btnIdDiscriminator,
            final BiConsumer<MyCreatedWorkPanel<T>, T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        Grid<T> grid = new Grid<>(workItemClass,false);
        H4 title = new H4(panelTitle);
        this.add(title);
        //grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work -> {
            
            Card card = cardBuilder.apply(work);   
            HorizontalLayout btnPanel = new HorizontalLayout();
            Button btnUpdate = new Button("See more", e -> {
                showUpdateDialog.accept(this,  work);
            });
            btnUpdate.setId(btnIdDiscriminator + work.getId());
            btnPanel.add(btnUpdate);
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        return grid;
    }

}

