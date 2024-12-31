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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author azrulhasnimadisa
 */
public class WorkflowAwareGridBuilder<T> {

    private static Integer COUNT_PER_PAGE = 3;

    public static <T> GridMemento<T> build(
            Class<T> clazz,
            Function<GridMemento<T>, Integer> counter,
            Function<GridMemento<T>, DataProvider<T, Void>> provider
    ) {
        return build("PANEL_DISC", clazz, counter, provider, Optional.empty());
    }

    public static <T> GridMemento build(
            String panelIdDiscriminator,
            Class<T> clazz,
            Function<GridMemento<T>, Integer> counter,
            Function<GridMemento<T>, DataProvider<T, Void>> provider,
            Optional<Supplier<Grid<T>>> gridCreator
    ) {

        SearchPanel searchPanel = new SearchPanel(panelIdDiscriminator);
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(clazz);
        Map<String, String> fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(clazz);
            
            
        Grid<T> grid = gridCreator.map(gc->{
            return gc.get();
        }).orElseGet(()->{
            Grid<T> g = new Grid<T>(clazz, false);
            for (var fieldEntry : fieldMap.entrySet()) {
                g.addColumn(fieldEntry.getKey())
                        .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                        .setHeader(fieldEntry.getValue());
                g.setAllRowsVisible(true);
            }
            return g;
        });

        PageNav nav = new PageNav();
        GridMemento<T> memento = GridMemento.build(
                clazz,
                searchPanel,
                nav,
                grid,
                counter,
                provider
        );

        Integer count = counter.apply(memento);
        
        nav.init(grid, count, COUNT_PER_PAGE, "id", sortableFields, Boolean.valueOf(false));

        DataProvider<T, Void> dataProvider = provider.apply(memento);
        
        grid.setDataProvider(dataProvider);

        searchPanel.searchRunner(s -> {
            Integer count2 = counter.apply(memento);
            nav.refresh(count2);
            grid.getDataProvider().refreshAll();
        });

        return memento;
    }

}
