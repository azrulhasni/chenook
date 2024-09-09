/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.views.common.Card;
import com.azrul.chenook.views.common.PageNav;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class MyWorkPanel  extends VerticalLayout {
    private final int COUNT_PER_PAGE = 3;
    private final PageNav navWorkByCreator;
    private final Grid<WorkItem> gridWorkByCreator;
    private final WorkflowService workflowService;
    private final OidcUser oidcUser;
    private final Map<String,String> sortableFields;
    
    public MyWorkPanel(
             final OidcUser oidcUser,
            final BizProcess bizProcess,
            final WorkflowService workflowService,
            final Map<String,String> sortableFields,
            final BiConsumer<MyWorkPanel,StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent,WorkItem> showUpdateDialog,
            final Function<WorkItem,VerticalLayout> cardBuilder
            
    ){
        this.navWorkByCreator = new PageNav();
        this.oidcUser = oidcUser;
        this.workflowService = workflowService;
        this.sortableFields=sortableFields;
       
                
        Integer countWorkByCreator = workflowService.countWorkByCreator(oidcUser.getPreferredUsername());
        DataProvider dpWorkByCreator = workflowService.getWorkByCreator(oidcUser.getPreferredUsername(),navWorkByCreator);
        
        gridWorkByCreator = createDataPanel(
                oidcUser,
                bizProcess,
                dpWorkByCreator,
                workflowService, 
                showCreationDialog, 
                showUpdateDialog,
                cardBuilder);
        navWorkByCreator.init(
                gridWorkByCreator, 
                countWorkByCreator, 
                COUNT_PER_PAGE,
                "id", 
                sortableFields, 
                false);
        
        this.add(navWorkByCreator);
        this.add(gridWorkByCreator);
    }
    
    public void refresh(){
        gridWorkByCreator.getDataProvider().refreshAll();
        Integer countWorkByCreator = workflowService.countWorkByCreator(oidcUser.getPreferredUsername());
        navWorkByCreator.refreshPageNav(countWorkByCreator);
    }
    
    private Grid<WorkItem> createDataPanel(
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final DataProvider dataProvider,
            final WorkflowService workflowService,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel,StartEvent, WorkItem> showUpdateDialog,
            final Function<WorkItem,VerticalLayout> cardBuilder) {
        Grid<WorkItem> grid = new Grid<>(WorkItem.class, false);

        grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);
        Set<StartEvent> startEvents = workflowService.whatUserStart(oidcUser, bizProcess);
        MenuBar menu = new MenuBar();
        for (var startEvent:startEvents){
            menu.addItem("Add new "+startEvent.getDescription(), e -> {
                showCreationDialog.accept(this,startEvent);
                
            });
        }
        
        this.add(menu);
        this.add(grid);
        grid.addComponentColumn(work ->
        {
            VerticalLayout content = cardBuilder.apply(work); 
            Card card = new Card(work.getFields().get("TITLE"));
            card.add(content);
            HorizontalLayout btnPanel = new HorizontalLayout();
            btnPanel.add(new Button("See more", e -> {
                showUpdateDialog.accept(this,null,work);
               
            }));
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        
        grid.setItems(dataProvider);
        return grid;
    }

}
