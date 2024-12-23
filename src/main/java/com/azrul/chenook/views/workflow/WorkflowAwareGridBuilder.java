/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author azrulhasnimadisa
 */
public class WorkflowAwareGridBuilder<T>  {
    private static Integer COUNT_PER_PAGE=3;
    
    public static <T> GridMemento build(
            Class<T> clazz,
            WorkflowAwareGroup group,
            Function<GridMemento,Integer> counter, 
            Function<GridMemento,DataProvider<T,Void>> provider
    ){
        
         SearchPanel searchPanel = new SearchPanel();
         Grid<T> grid = new Grid<T>(clazz, false);
         PageNav nav = new PageNav();
         GridMemento<T> memento = GridMemento.build(clazz,searchPanel, nav, grid);

        Integer count = counter.apply(memento);
        
        DataProvider<T, Void> dataProvider = provider.apply(memento);

       

        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(clazz);
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, Boolean.valueOf(false));
        grid.setDataProvider(dataProvider);
        
        grid.setItems(dataProvider);
        
        Map<String, String> fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(clazz);
        for (var fieldEntry : fieldMap.entrySet()) {
            grid.addColumn(fieldEntry.getKey())
                    .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                    .setHeader(fieldEntry.getValue());
            grid.setAllRowsVisible(true);
        }

        searchPanel.searchRunner(s -> {
            Integer count2 = counter.apply(memento);
            nav.refresh(count2);
            grid.getDataProvider().refreshAll();
        });
        
        return memento; 
    }
    
}
