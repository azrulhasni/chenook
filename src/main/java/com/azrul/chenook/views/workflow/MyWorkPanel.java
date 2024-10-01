/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.config.WorkflowConfig;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.Card;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.azrul.smefinancing.service.FinApplicationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
@SpringComponent
public class MyWorkPanel<T extends WorkItem> extends VerticalLayout {

    
    private       Pair<Grid<T>, PageNav> myCreatedWork;
    private       Pair<Grid<T>, PageNav> myOwnedWork;
    private       OidcUser oidcUser;
    
    private final FinApplicationService finappService;
    private final int COUNT_PER_PAGE = 3;
    private final BadgeUtils badgeUtils;
    private final WorkflowConfig workflowConfig;
    
    public static <T extends WorkItem> MyWorkPanel create( final Class<T> workItemClass,  
            final OidcUser oidcUser,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder){
        var myWorkPanel = ApplicationContextHolder.getBean(MyWorkPanel.class);
        myWorkPanel.init(
                workItemClass, 
                oidcUser, 
                showCreationDialog, 
                showUpdateDialog, 
                cardBuilder);
        return myWorkPanel;
    }

    private MyWorkPanel(
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
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder
    ) {

        this.oidcUser = oidcUser;
        BizProcess bizProcess = workflowConfig.rootBizProcess();
       
         Map<String,String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);
        
        this.setWidth("-webkit-fill-available");

        List<StartEvent> startEvents = finappService.whatUserCanStart(oidcUser, bizProcess);
        if (!startEvents.isEmpty()) {
            MenuBar menu = new MenuBar();

            for (var startEvent : startEvents) {
                MenuItem miAddNew = menu.addItem("Add new " + startEvent.getDescription(), e -> {
                    showCreationDialog.accept(this, startEvent);

                });
                miAddNew.setId("new_"+startEvent.getId());
            }
            menu.setWidth("-webkit-fill-available");
            this.add(menu);
            myCreatedWork = buildDataPanel(
                    "Work items I've created",
                    "btnMyCreatedWork",
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
                "btnMyWork",
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
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder,
            final Map<String, String> sortableFields,
            final Function<String, Integer> counter,
            final BiFunction<String, PageNav, DataProvider> dataProviderCreator) {
        PageNav nav = new PageNav();
        Integer count = counter.apply(oidcUser.getPreferredUsername());//finappService1.countWorkByCreator(oidcUser1.getPreferredUsername());
        DataProvider dataProvider = dataProviderCreator.apply(oidcUser.getPreferredUsername(), nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title,btnIdDiscriminator, oidcUser, bizProcess, dataProvider, showCreationDialog, showUpdateDialog, cardBuilder);
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
        
        if (myOwnedWork!=null){
            myOwnedWork.getFirst().getDataProvider().refreshAll();
            Integer countMyOwnedWork = finappService.countWorkByOwner(oidcUser.getPreferredUsername());
            myOwnedWork.getSecond().refresh(countMyOwnedWork);
        }
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final String btnIdDiscriminator,
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final DataProvider dataProvider,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent, T> showUpdateDialog,
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
                showUpdateDialog.accept(this, null, work);
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

}
