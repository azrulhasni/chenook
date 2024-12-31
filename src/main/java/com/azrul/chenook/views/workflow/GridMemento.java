/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.function.Function;

/**
 *
 * @author azrulhasnimadisa
 */
public class GridMemento<T> {

    private Class<T> clazz = null;
    private Grid<T> grid = null;
    private PageNav pageNav = null;
    private SearchPanel searchPanel = null;

    private Function<GridMemento<T>, Integer> counter;
    private Function<GridMemento<T>, DataProvider<T, Void>> provider;

    public static <T> GridMemento<T> build(
            Class<T> clazz,
            SearchPanel searchPanel,
            PageNav pageNav, Grid<T> grid,
            Function<GridMemento<T>, Integer> counter,
            Function<GridMemento<T>, DataProvider<T, Void>> provider
    ) {
        GridMemento<T> memento = new GridMemento<>();
        memento.setClazz(clazz);
        memento.setGrid(grid);
        memento.setPageNav(pageNav);
        memento.setSearchPanel(searchPanel);
        memento.setCounter(counter);
        memento.setProvider(provider);
        return memento;
    }

    /**
     * @return the grid
     */
    public Grid<T> getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(Grid<T> grid) {
        this.grid = grid;
    }

    /**
     * @return the pageNav
     */
    public PageNav getPageNav() {
        return pageNav;
    }

    /**
     * @param pageNav the pageNav to set
     */
    public void setPageNav(PageNav pageNav) {
        this.pageNav = pageNav;
    }

    /**
     * @return the searchPanel
     */
    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    /**
     * @param searchPanel the searchPanel to set
     */
    public void setSearchPanel(SearchPanel searchPanel) {
        this.searchPanel = searchPanel;
    }

    /**
     * @return the clazz
     */
    public Class<T> getClazz() {
        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public VerticalLayout getPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.add(searchPanel, pageNav, grid);
        return panel;
    }

    /**
     * @return the counter
     */
    public Function<GridMemento<T>, Integer> getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(Function<GridMemento<T>, Integer> counter) {
        this.counter = counter;
    }

    /**
     * @return the provider
     */
    public Function<GridMemento<T>, DataProvider<T, Void>> getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(Function<GridMemento<T>, DataProvider<T, Void>> provider) {
        this.provider = provider;
    }

    public void refresh() {
        Integer countMyCreatedWork = counter.apply(this);
        pageNav.refresh(countMyCreatedWork);
        grid.getDataProvider().refreshAll();
    }

}
