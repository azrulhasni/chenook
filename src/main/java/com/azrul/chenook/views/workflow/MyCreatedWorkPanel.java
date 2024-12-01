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
import com.azrul.chenook.views.common.components.SearchPanel;
import com.azrul.chenook.views.common.function.TriFunction;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.vaadin.flow.component.Component;
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
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */

@SpringComponent
public class MyCreatedWorkPanel<T extends WorkItem> extends VerticalLayout {

    private Triple<SearchTermProvider, PageNav, Grid<T>> myCreatedWork;
    private BizUser user;
    private BiFunction<String, SearchTermProvider, Integer> counter;
    private TriFunction<String, SearchTermProvider, PageNav, DataProvider<T,Void>> dataProviderCreator;

    private       WorkflowService<T> workflowService;
    private final int COUNT_PER_PAGE = 3;
    //private final BadgeUtils badgeUtils;
    private final WorkflowConfig workflowConfig;

    public static <T extends WorkItem> MyCreatedWorkPanel<T> create(
            final WorkflowService<T> workflowService,
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final TriConsumer<MyCreatedWorkPanel<T>, StartEvent, T> showUpdateDialog,
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
            final TriConsumer<MyCreatedWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder
    ) {

        this.user = user;
        this.workflowService = workflowService;
        this.counter = (username, searchTermProvider) -> workflowService.countWorkByCreator(workItemClass, username, searchTermProvider);
        this.dataProviderCreator = (username, searchTermProvider, nav) -> workflowService.getWorkByCreator(workItemClass, username, searchTermProvider, nav);

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);

        this.setWidth("-webkit-fill-available");

        myCreatedWork = buildDataPanel(
                "My work items",
                "btnMyWork",
                "workItems",
                user,
                bizProcess,//workflowConfig.rootBizProcess(),
                showUpdateDialog,
                cardBuilder,
                sortableFields
        );
        add(myCreatedWork);

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
            final TriConsumer<MyCreatedWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder,
            final Map<String, String> sortableFields
    ) {
        SearchPanel searchPanel = new SearchPanel(panelIdDiscriminator);
        searchPanel.searchRunner(s->refresh());
        
        Integer count = counter.apply(user.getUsername(), searchPanel);//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        PageNav nav = new PageNav();
        
        DataProvider<T,Void> dataProvider = dataProviderCreator.apply(user.getUsername(), searchPanel, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav); 
        Grid<T> grid = createGrid(title, btnIdDiscriminator, user, bizProcess, dataProvider, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
       

        Triple<SearchTermProvider, PageNav, Grid<T>> triple = Triple.of(searchPanel, nav, grid);
        return triple;
    }

    public void refresh() {

        if (myCreatedWork != null) {
            Integer countMyCreatedWork = counter.apply(user.getUsername(), myCreatedWork.getLeft());
            myCreatedWork.getMiddle().refresh(countMyCreatedWork);
            myCreatedWork.getRight().getDataProvider().refreshAll();
            
        }
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final String btnIdDiscriminator,
            final BizUser user,
            final BizProcess bizProcess,
            final DataProvider<T,Void> dataProvider,
            final TriConsumer<MyCreatedWorkPanel<T>, StartEvent, T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H4 title = new H4(panelTitle);
        this.add(title);
        //grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work -> {
//            VerticalLayout content = cardBuilder.apply(work);
//            Span badge = badgeUtils.createStatusBadge(work.getStatus());
//            Card card = new Card(work.getTitle(), badge);
//            card.add(content);
            
            Card card = cardBuilder.apply(work);   
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

/*public class MyCreatedWorkPanel<T extends WorkItem> extends VerticalLayout {
    
    
    private       Pair<Grid<T>, PageNav> myCreatedWork;
    private       OidcUser oidcUser;
    
    private final FinApplicationService finappService;
    private final int COUNT_PER_PAGE = 3;
    private final BadgeUtils badgeUtils;
    private final WorkflowConfig workflowConfig;
    
    public static <T extends WorkItem> MyCreatedWorkPanel create( final Class<T> workItemClass,  
            final OidcUser oidcUser,
            final TriConsumer<MyCreatedWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder){
        var myWorkPanel = ApplicationContextHolder.getBean(MyCreatedWorkPanel.class);
        myWorkPanel.init(
                workItemClass, 
                oidcUser, 
                showUpdateDialog, 
                cardBuilder);
        return myWorkPanel;
    }

    private MyCreatedWorkPanel(
            @Autowired WorkflowConfig workflowConfig,
            @Autowired  FinApplicationService finappService,
            @Autowired  BadgeUtils badgeUtils
            ){
         this.finappService = finappService;
        this.badgeUtils = badgeUtils;
        this.workflowConfig=workflowConfig;
    }
        
    public void init(
        final Class workItemClass,  
            final OidcUser oidcUser,
            final TriConsumer<MyCreatedWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder
    ) {

        this.oidcUser = oidcUser;
        BizProcess bizProcess = workflowConfig.rootBizProcess();
       
         Map<String,String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);
        
        this.setWidth("-webkit-fill-available");

        List<StartEvent> startEvents = finappService.whatUserCanStart(oidcUser, bizProcess);
        if (!startEvents.isEmpty()) {
            myCreatedWork = buildDataPanel(
                    "Work items I've created",
                    "btnMyCreatedWork",
                    oidcUser,
                    bizProcess,
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


    }

    private void addPair(Pair<Grid<T>, PageNav> pair) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("40em");
        layout.getStyle().set("border","1px solid lightgrey");
        layout.getStyle().set("border-radius","25px");
        layout.add(pair.getSecond());
        layout.add(pair.getFirst());
        this.add(layout);
    }

    private Pair<Grid<T>, PageNav> buildDataPanel(
            final String title,
            final String btnIdDiscriminator,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final TriConsumer<MyCreatedWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder,
            final Map<String, String> sortableFields,
            final Function<String, Integer> counter,
            final BiFunction<String, PageNav, DataProvider> dataProviderCreator) {
        PageNav nav = new PageNav();
        Integer count = counter.apply(oidcUser.getPreferredUsername());//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = dataProviderCreator.apply(oidcUser.getPreferredUsername(), nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title,btnIdDiscriminator, oidcUser, bizProcess, dataProvider, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
        Pair<Grid<T>, PageNav> pair = Pair.of(grid, nav);
        return pair;
    }

    public void refresh() {
        if (myCreatedWork!=null){
            myCreatedWork.getFirst().getDataProvider().refreshAll();
            Integer countWorkByCreator = finappService.countWorkByCreator(oidcUser.getPreferredUsername());
            myCreatedWork.getSecond().refresh(countWorkByCreator);
        }
        
       
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final String btnIdDiscriminator,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final DataProvider dataProvider,
            final TriConsumer<MyCreatedWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H4 title = new H4(panelTitle);
        this.add(title);
        //grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work-> {
            VerticalLayout content = cardBuilder.apply(work);
            Span badge = badgeUtils.createStatusBadge(work.getStatus());
            Card card = new Card(work.getTitle(), badge);
            card.add(content);
            HorizontalLayout btnPanel = new HorizontalLayout();
            Button btnUpdate = new Button("See more", e -> {
                showUpdateDialog.accept(this,null,  work);
            });
            btnUpdate.setId(btnIdDiscriminator+work.getId());
            btnPanel.add(btnUpdate);
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(dataProvider);
        return grid;
    }


}*/
