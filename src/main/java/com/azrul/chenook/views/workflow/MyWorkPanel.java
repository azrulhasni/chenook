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
import com.azrul.smefinancing.service.FinApplicationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class MyWorkPanel<T extends WorkItem>  extends VerticalLayout {
    private final int COUNT_PER_PAGE = 3;
    private final PageNav navWorkByCreator;
    private final Grid<T> gridWorkByCreator;
    private final FinApplicationService finappService;
    private final OidcUser oidcUser;
    private final Map<String,String> sortableFields;
    
    public MyWorkPanel(
             final OidcUser oidcUser,
            final BizProcess bizProcess,
            final Map<String,String> sortableFields,
            final FinApplicationService finappService,
            final BiConsumer<MyWorkPanel,StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel, StartEvent,T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder
            
    ){
        this.navWorkByCreator = new PageNav();
        this.oidcUser = oidcUser;
        this.sortableFields=sortableFields;
        this.finappService=finappService;
                
        Integer countWorkByCreator = finappService.countWorkByCreator(oidcUser.getPreferredUsername());
        DataProvider dpWorkByCreator = finappService.getWorkByCreator(oidcUser.getPreferredUsername(),navWorkByCreator);
        
        gridWorkByCreator = createDataPanel(
                oidcUser,
                bizProcess,
                dpWorkByCreator,
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
        Integer countWorkByCreator = finappService.countWorkByCreator(oidcUser.getPreferredUsername());
        navWorkByCreator.refreshPageNav(countWorkByCreator);
    }
    
    private Grid<T> createDataPanel(
            final OidcUser oidcUser,
            final BizProcess bizProcess,
            final DataProvider dataProvider,
            final BiConsumer<MyWorkPanel, StartEvent> showCreationDialog,
            final TriConsumer<MyWorkPanel,StartEvent, T> showUpdateDialog,
            final Function<T, VerticalLayout> cardBuilder) {
        Grid<T> grid = new Grid<>();

        grid.getStyle().set("max-width", "285px");
        grid.setAllRowsVisible(true);
        Set<StartEvent> startEvents = finappService.whatUserStart(oidcUser, bizProcess);
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
