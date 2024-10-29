package com.azrul.chenook.views.reference;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.attachments.AttachmentsPanel;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

public class ReferencePanel<R extends Reference, RS extends ReferenceService<R>> extends VerticalLayout{
    private final int COUNT_PER_PAGE = 3;
    private int maxSelection = 0;


    private ReferenceService refService;

    private ReferencePanel(
        RS refService
    ){
        this.refService=refService;
                                              
    }

    private void init(Class<R> referenceClass, Long dependencyId, final OidcUser oidcUser, final Integer maxSelection ){
        setMaxSelection(maxSelection);

        Integer count = refService.countReferenceData(referenceClass, dependencyId);
        PageNav nav = new PageNav();
        
        DataProvider dataProvider = refService.getReferenceData(referenceClass, nav, dependencyId);
        Grid<R> grid = new Grid<>(referenceClass,false);
        grid.setItems(dataProvider);

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry:fieldMap.entrySet()){
            grid.addColumn(fieldEntry.getKey())
                .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                .setHeader(fieldEntry.getValue());
        }

        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);
       
        grid.setDataProvider(dataProvider);
        
        Button btnSelectDialog = new Button("Select", e->{
            Dialog dialog = buildDialog(referenceClass, dependencyId);
            dialog.open();
        });
        this.add(nav);
        this.add(btnSelectDialog);
        this.add(grid);
    }

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    private Dialog buildDialog(Class<R> referenceClass, Long dependencyId){
        Dialog dialog = new Dialog();
        SearchPanel searchPanel = new SearchPanel();
        Integer count = refService.countAllReferenceData(referenceClass, searchPanel);
        PageNav nav = new PageNav();
        
        DataProvider dataProvider = refService.getAllReferenceData(referenceClass, searchPanel,nav);
        Grid<R> grid = new Grid<>(referenceClass, false);
        grid.setItems(dataProvider);
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry:fieldMap.entrySet()){
            grid.addColumn(fieldEntry.getKey())
                .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                .setHeader(fieldEntry.getValue());
        }

        for (int i=0;i<grid.getDataCommunicator().getItemCount();i++){
            R item = grid.getDataCommunicator().getItem(i);
            if (item.getDependencies().contains(dependencyId)){
                grid.getSelectionModel().select(item);
            }
        }
        
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, false);

        grid.setDataProvider(dataProvider);
        if (maxSelection==1){ 
            grid.setSelectionMode(SelectionMode.SINGLE);
        }else{
            grid.setSelectionMode(SelectionMode.MULTI);
        }

        

        searchPanel.searchRunner(s->dataProvider.refreshAll());

        dialog.add(searchPanel,nav,grid);
        Button btnClose = new Button("Close", e->dialog.close());
        Button btnSelect = new Button("Select", e->{
            Set<R> selectedItems = grid.getSelectedItems();
            for (R selectedItem : selectedItems) {
                selectedItem.getDependencies().add(dependencyId);
                refService.save(selectedItem);
            }
        });
        dialog.getFooter().add();
        return dialog;
    }

    public static <R extends Reference, RS extends ReferenceService<R>> ReferencePanel<R,RS> create(
        final Class<R> referenceClass,
        final RS refService,
        final Long dependencyId, 
        final OidcUser oidcUser,
        final Integer maxSelection){
        
        var refPanel = new ReferencePanel<R,RS>(refService);
        refPanel.init(referenceClass, dependencyId, oidcUser, maxSelection);
        return refPanel;
    }
}