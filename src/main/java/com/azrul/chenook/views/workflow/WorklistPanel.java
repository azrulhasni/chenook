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
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.model.StartEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
@SpringComponent
public class WorklistPanel<T extends WorkItem> extends VerticalLayout {

    private final int COUNT_PER_PAGE = 3;
    private final List<Triple<Grid<T>, PageNav,String>> myWorklists = new ArrayList<>();
    private       WorkflowService<T> workflowService;
    private final WorkflowConfig workflowConfig;
    private       Function<String, Integer> counter;
    private       BiFunction<String, PageNav, DataProvider<T,Void>> dataProviderCreator;
    
    public static <T extends WorkItem> WorklistPanel create(
            final WorkflowService<T> workflowService,
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final BiConsumer<WorklistPanel<T>,  T> showUpdateDialog,
            final Function<T, Card> cardBuilder){
       WorklistPanel<T> worklistPanel = ApplicationContextHolder.getBean(WorklistPanel.class);
        worklistPanel.init(workflowService,workItemClass, user, bizProcess, showUpdateDialog, cardBuilder);
        return worklistPanel;
    }

    public WorklistPanel(
            @Autowired  WorkflowConfig workflowConfig){
        this.workflowConfig=workflowConfig;
    }
        
    public void init(
            final WorkflowService<T> workflowService,
            final Class<T> workItemClass,
            final BizUser user,
            final BizProcess bizProcess,
            final BiConsumer<WorklistPanel<T>,  T> showUpdateDialog,
            final Function<T, Card> cardBuilder
    ) {

        //this.user = oidcUser;
        this.workflowService = workflowService;
        this.counter = (w) -> workflowService.countWorkByWorklist(w);
        this.dataProviderCreator = (w, nav) -> workflowService.getWorkByWorklist(workItemClass,w, nav);
        
        Map<String,String> sortableFields = WorkflowUtils.getSortableFields(workItemClass);
            
        this.setWidth("-webkit-fill-available");

        Set<String> roles = user.getClientRoles().stream().collect(Collectors.toSet());
        
        Map<String,String> worklists = workflowService.findWorklistsByRoles(roles, bizProcess);

        for (Map.Entry<String,String> worklist : worklists.entrySet()) {
            Triple<Grid<T>, PageNav, String> panel = buildDataPanel(
                    "Worklist:" + worklist.getValue(),
                    worklist.getKey(),
                    user,
                    showUpdateDialog,
                    cardBuilder,
                    sortableFields
            );
            myWorklists.add(panel);
            add(panel);
        }

    }

    private void add(Triple<Grid<T>, PageNav, String> pair) {
        VerticalLayout layout = new VerticalLayout();
        //layout.setWidth("30%");
        layout.setMaxWidth("40em");
        layout.getStyle().set("border","1px solid lightgrey");
        layout.getStyle().set("border-radius","25px");
        layout.add(pair.getMiddle());
        layout.add(pair.getLeft());
        this.add(layout);
    }

    private Triple<Grid<T>, PageNav, String> buildDataPanel(
            final String title,
            final String w,
            final BizUser user,
            final BiConsumer<WorklistPanel<T>, T> showUpdateDialog,
            final Function<T, Card> cardBuilder,
            final Map<String, String> sortableFields1) {
        PageNav nav = new PageNav();
        Integer count = counter.apply(w);
        DataProvider dataProvider = dataProviderCreator.apply(w, nav);//finappService1.getWorkByCreator(oidcUser1.getPreferredUsername(), nav);
        Grid<T> grid = createGrid(title, user, dataProvider, showUpdateDialog, cardBuilder);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields1, false);
        Triple<Grid<T>, PageNav, String> triple = Triple.of(grid, nav,w);
        return triple;
    }

    public void refresh() {
        for (var triple : myWorklists) {
            triple.getLeft().getDataProvider().refreshAll();
            Integer countWorkByCreator = workflowService.countWorkByWorklist(triple.getRight());
            triple.getMiddle().refresh(countWorkByCreator);
        }
    }

    private Grid<T> createGrid(
            final String panelTitle,
            final BizUser user,
            final DataProvider<T,Void> dataProvider,
            final BiConsumer<WorklistPanel<T>,  T> showUpdateDialog,
            final Function<T, Card> cardBuilder) {
        Grid<T> grid = new Grid<>();
        H4 title = new H4(panelTitle);
        this.add(title);
        grid.setAllRowsVisible(true);

        this.add(grid);
        grid.addComponentColumn(work -> {
            Card card = cardBuilder.apply(work);
            HorizontalLayout btnPanel = new HorizontalLayout();
            Button btnBookThis = new Button("Book this work", e -> {
                work.addOwner(user);
                T w = workflowService.save(work);
                showUpdateDialog.accept(this,  w);
            });
            btnBookThis.setId("btnBook"+work.getId());
            btnPanel.add(btnBookThis);
            card.add(btnPanel);
            return card;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(dataProvider);
        return grid;
    }

}
