package com.azrul.chenook.views.reference;


import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceMap;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;

public class ReferencePanel<R extends Reference, RS extends ReferenceService<R>> extends VerticalLayout{
    private final int COUNT_PER_PAGE = 3;
    private int maxSelection = 0;
    private String context;


    private ReferenceService<R> refService;

    private ReferencePanel(
        RS refService
    ){
        this.refService=refService;
                                              
    }

    private void init(Class<R> referenceClass, Long parentId, final OidcUser oidcUser, final Integer maxSelection , String context){
        setMaxSelection(maxSelection);
        this.context=context;

        Integer count = refService.countReferenceData(referenceClass, parentId);
        PageNav nav = new PageNav();
        
        DataProvider<R,Void> dataProvider = refService.getReferenceData(referenceClass, nav, parentId);
        Grid<R> grid = new Grid<>(referenceClass,false);
        grid.setItems(dataProvider);
        grid.setAllRowsVisible(true);

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry:fieldMap.entrySet()){
            grid.addColumn(fieldEntry.getKey())
                .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                .setHeader(fieldEntry.getValue());
        }
        if (maxSelection==1){
            nav.init(grid, count, 1);
            grid.setHeight("calc("+2+" * var(--lumo-size-l))");
        }else{
            nav.init(grid, count, COUNT_PER_PAGE);
            grid.setHeight("calc("+(COUNT_PER_PAGE+1)+" * var(--lumo-size-l))");
        }
        
       
        grid.setDataProvider(dataProvider);

        
        Button btnSelectDialog = new Button("Select", e->{
            Dialog dialog = buildDialog(referenceClass, parentId, grid, nav);
            dialog.open();
        });
        this.add(nav);
        this.add(btnSelectDialog);
        this.add(grid);
        this.getStyle().set("border", "1px solid lightgrey");
        this.getStyle().set("border-radius", "10px");
    }

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    private Dialog buildDialog(
        Class<R> referenceClass, 
        Long parentId, 
        Grid<R> gSelectedItems,
        PageNav navSelectedItems
    ){
        Dialog dialog = new Dialog();
        SearchPanel searchPanel = new SearchPanel();
        Integer count = refService.countAllReferenceData(referenceClass, searchPanel);
        PageNav nav = new PageNav();
        DataProvider<R,Void> dataProvider = refService.getAllReferenceData(referenceClass, searchPanel,nav);
        Grid<R> grid = new Grid<>(referenceClass, false);
        grid.setItems(dataProvider);
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry:fieldMap.entrySet()){
            grid.addColumn(fieldEntry.getKey())
                .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                .setHeader(fieldEntry.getValue());
            grid.setAllRowsVisible(true);
        }

        ReferenceMap<R> refMap = refService.getMap(parentId, referenceClass);
        for (int i=0;i<grid.getDataCommunicator().getItemCount();i++){
            R item = grid.getDataCommunicator().getItem(i);
            if (refMap.getReferences().contains(item)){
                grid.getSelectionModel().select(item);
            }
        }
        
        nav.init(grid, count, COUNT_PER_PAGE);

        grid.setDataProvider(dataProvider);
        if (maxSelection==1){ 
            grid.setSelectionMode(SelectionMode.SINGLE);
        }else{
            grid.setSelectionMode(SelectionMode.MULTI);
        }
        //grid.setHeight("calc("+COUNT_PER_PAGE+" * var(--lumo-size-m))");
        

        searchPanel.searchRunner(s->dataProvider.refreshAll());

        dialog.add(searchPanel,nav,grid);
        Button btnClose = new Button("Close", e->dialog.close());
        Button btnSelect = new Button("Select", e->{
            Set<R> selectedItems = grid.getSelectedItems();
            refService.saveMap(parentId, referenceClass, selectedItems, context);
            Integer countSelected = refService.countReferenceData(referenceClass, parentId);
            navSelectedItems.refresh(countSelected);
            gSelectedItems.getDataProvider().refreshAll();
            gSelectedItems.setAllRowsVisible(true);
            dialog.close();
        });
        dialog.getFooter().add(btnClose, btnSelect);
        return dialog;
    }

    public static <R extends Reference, RS extends ReferenceService<R>> ReferencePanel<R,RS> create(
        final Class<R> referenceClass,
        final RS refService,
        final Long dependencyId, 
        final OidcUser oidcUser,
        final Integer maxSelection,
        final String context){
        
        var refPanel = new ReferencePanel<R,RS>(refService);
        refPanel.init(referenceClass, dependencyId, oidcUser, maxSelection, context);
        return refPanel;
    }
}